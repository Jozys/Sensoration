package de.schuettslaar.sensoration.application.data

import android.util.Log
import com.paramsen.noise.Noise
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.RawSensorData

class PitchDetectionClientDataProcessing : ClientDataProcessing {
    private val fftSize = 4096 // Standard (must be power of 2) (FFT can optimise this)
    private val noise = Noise.real(fftSize)
    private val minFrequency = 80f  // Lower limit (most human voices/instruments)
    private val maxFrequency = 5000f // Upper limit (typical music range)

    override fun processData(rawData: RawSensorData): ProcessedSensorData {
        val sampleRate = 44100

        // Window the data
        val windowedData = applyHannWindow(rawData.value)

        // Pad or truncate to match FFT size
        val paddedData = if (windowedData.size >= fftSize) {
            windowedData.copyOf(fftSize)
        } else {
            FloatArray(fftSize).also { windowedData.copyInto(it) }
        }
        val dst = FloatArray(fftSize + 2)

        // Perform FFT using the noise library
        val fftOutput = noise.fft(paddedData, dst)

        // Find dominant frequency
        var maxMagnitudeIndex = 0
        var maxMagnitude = 0.1f // Start with a small threshold to avoid noise

        // Convert frequency bounds to bin indices
        val minBoundary = (minFrequency * fftSize / sampleRate).toInt().coerceAtLeast(1)
        //  Process only the meaningful frequencies (up to Nyquist frequency)
        val maxBoundary = (maxFrequency * fftSize / sampleRate).toInt().coerceAtMost(fftSize / 2)

        for (i in minBoundary until maxBoundary) {
            val magnitude = getMagnitude(fftOutput, i)

            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                maxMagnitudeIndex = i
            }
        }


        // Filter out spikes
        val interpolatedDominantFrequency =
            if (maxMagnitudeIndex > 0 && maxMagnitudeIndex < fftSize / 2 - 1) {
                val prev = getMagnitude(fftOutput, maxMagnitudeIndex - 1)
                val curr = getMagnitude(fftOutput, maxMagnitudeIndex)
                val next = getMagnitude(fftOutput, maxMagnitudeIndex + 1)

                val delta = 0.5f * (next - prev) / (2 * curr - next - prev)
                (maxMagnitudeIndex + delta) * sampleRate / fftSize
            } else {
                maxMagnitudeIndex.toFloat() * sampleRate / fftSize
            }
        val maxMagnitudeDb = 20 * kotlin.math.log10(maxMagnitude)

        Log.d(
            "PitchDetection",
            "Dominant Frequency: $interpolatedDominantFrequency Hz $maxMagnitudeDb"
        )

        return ProcessedSensorData(
            timestamp = rawData.timestamp,
            sensorType = rawData.sensorType,
            processingStatus = "pitch_detection",
            value = arrayOf(interpolatedDominantFrequency, maxMagnitudeDb)
        )
    }

    private fun applyHannWindow(data: FloatArray): FloatArray {
        val windowed = FloatArray(data.size)
        for (i in data.indices) {
            val multiplier = 0.5f * (1 - kotlin.math.cos(2 * Math.PI * i / (data.size - 1)))
            windowed[i] = data[i] * multiplier.toFloat()
        }
        return windowed
    }

    private fun getMagnitude(fftOutput: FloatArray, index: Int): Float {
        val re = fftOutput[index * 2]
        val im = fftOutput[index * 2 + 1]
        return re * re + im * im
    }
}