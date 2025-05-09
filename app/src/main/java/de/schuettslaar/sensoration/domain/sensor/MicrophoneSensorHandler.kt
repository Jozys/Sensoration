package de.schuettslaar.sensoration.domain.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val AUDIO_SOURCE_TYPE = MediaRecorder.AudioSource.UNPROCESSED
private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT
private const val BUFFER_SIZE_FACTOR = 2
private const val SAMPLE_RATE = 44100

class MicrophoneSensorHandler(private val context: Context) : SensorHandler {
    private val TAG = "MicrophoneSensorHandler"

    private var processor: ClientDataProcessing? = null
    private var latestSensorData: ProcessedSensorData? = null

    // Audio recording properties
    private val audioScope = CoroutineScope(Dispatchers.IO)

    private var audioRecord: AudioRecord? = null
    private var audioRecordingJob: Job? = null
    private var isRecording = false


    override fun initialize(processor: ClientDataProcessing) {
        this.processor = processor
    }

    override fun startListening() {
        if (processor == null) {
            Log.e(TAG, "Processor not initialized")
            return
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "RECORD_AUDIO permission not granted")
            return
        }

        startRecordingAudio()
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecordingAudio() {
        Log.e(TAG, "startRecordingAudio() called")
        if (isRecording) return

        try {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
            ) * BUFFER_SIZE_FACTOR

            audioRecord = AudioRecord.Builder()
                .setAudioSource(AUDIO_SOURCE_TYPE)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG)
                        .setEncoding(AUDIO_FORMAT)
                        .build()
                )
//                .setPrivacySensitive(false)
                .setBufferSizeInBytes(bufferSize)
                .build()

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord initialization failed")
                return
            }

            audioRecord?.startRecording()
            isRecording = true

            audioRecordingJob = audioScope.launch {
                val buffer = FloatArray(bufferSize / 2)

                while (isActive && isRecording) {
                    val readResult = audioRecord!!.read(
                        buffer,
                        0,
                        buffer.size,
                        AudioRecord.READ_BLOCKING
                    ) // TODO: Maybe adjust to READ_NON_BLOCKING, but this must be tested

                    // Check if data was read successfully
                    if (readResult > 0) {
                        val rawData = RawSensorData(
                            timestamp = System.currentTimeMillis(),
                            sensorType = SensorType.SOUND_PRESSURE.sensorId,
                            value = buffer
                        )

                        processor?.let {
                            latestSensorData = it.processData(rawData)
                        }
                    }
                }

            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting audio recording: ${e.message}")
        }
    }

    override fun stopListening() {
        if (isRecording) {
            isRecording = false
            audioRecordingJob?.cancel()
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        }
        latestSensorData = null
    }

    override fun getLatestData(): ProcessedSensorData? {
        return latestSensorData
    }

    override fun cleanup() {
        stopListening()
    }
}