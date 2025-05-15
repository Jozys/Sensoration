package de.schuettslaar.sensoration.domain.sensor

import android.content.Context
import android.util.Log
import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import de.schuettslaar.sensoration.application.data.RawClientDataProcessing
import de.schuettslaar.sensoration.domain.PTPHandler

class SensorManager(private val context: Context, private val ptpHandler: PTPHandler) {

    private var currentHandler: SensorHandler? = null
    private val TAG = "SensorManager"

    fun registerSensor(
        sensorType: Int,
        processor: ClientDataProcessing = RawClientDataProcessing()
    ) {
        // Clean up any existing handler
        currentHandler?.cleanup()

        // Create appropriate handler based on sensor type
        currentHandler = obtainHandlerForSensorType(sensorType)

        // Initialize the handler
        currentHandler?.initialize(processor)
    }

    private fun obtainHandlerForSensorType(sensorType: Int): SensorHandler = when (sensorType) {
        SensorType.SOUND_PRESSURE.sensorId -> MicrophoneSensorHandler(context, ptpHandler)
        else -> HardwareSensorHandler(context, sensorType, ptpHandler)
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

    fun checkDeviceSupportsSensorType(sensorType: Int): Boolean {
        val handler = obtainHandlerForSensorType(sensorType)
        return handler.checkDeviceSupportsSensorType(sensorType)
    }
}