package de.schuettslaar.sensoration.application.data

import android.util.Log
import com.paramsen.noise.Noise
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.RawSensorData

class PitchDetectionClientDataProcessing : ClientDataProcessing {
    private val fftSize = 4096 // Standard (must be power of 2) (FFT can optimise this)
    private val noise = Noise.real(fftSize)

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
        var maxMagnitude = 0.0f

        // Process only the meaningful frequencies (up to Nyquist frequency)
        for (i in 0 until fftSize / 2) {
            val re = fftOutput[i * 2]
            val im = fftOutput[i * 2 + 1]
            val magnitude = re * re + im * im

            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                maxMagnitudeIndex = i
            }
        }

        val dominantFrequency = maxMagnitudeIndex.toFloat() * sampleRate / fftSize
        val maxMagnitudeDb = 20 * kotlin.math.log10(maxMagnitude)

        Log.d("PitchDetection", "Dominant Frequency: $dominantFrequency Hz $maxMagnitudeDb")

        return ProcessedSensorData(
            timestamp = rawData.timestamp,
            sensorType = rawData.sensorType,
            processingStatus = "pitch_detection",
            value = arrayOf(dominantFrequency, maxMagnitudeDb)
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

}