package de.schuettslaar.sensoration.domain.sensor

import android.content.Context
import android.util.Log
import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import de.schuettslaar.sensoration.application.data.RawClientDataProcessing

class SensorManager(private val context: Context) {

    private var currentHandler: SensorHandler? = null
    private val TAG = "SensorManager"

    fun registerSensor(
        sensorType: Int,
        processor: ClientDataProcessing = RawClientDataProcessing()
    ) {
        // Clean up any existing handler
        currentHandler?.cleanup()

        // Create appropriate handler based on sensor type
        currentHandler = when (sensorType) {
            SensorType.SOUND_PRESSURE.sensorId -> MicrophoneSensorHandler(context)
            else -> HardwareSensorHandler(context, sensorType)
        }

        // Initialize the handler
        currentHandler?.initialize(processor)
    }

    fun startListening() {
        if (currentHandler == null) {
            Log.e(TAG, "No sensor registered. Call registerSensor() first.")
            return
        }

        currentHandler?.startListening()
    }

    fun stopListening() {
        currentHandler?.stopListening()
    }

    fun getLatestSensorData(): ProcessedSensorData? {
        return currentHandler?.getLatestData()
    }

    fun cleanup() {
        currentHandler?.cleanup()
        currentHandler = null
    }
}