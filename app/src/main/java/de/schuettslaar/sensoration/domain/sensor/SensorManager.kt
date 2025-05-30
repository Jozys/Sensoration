package de.schuettslaar.sensoration.domain.sensor

import android.content.Context
import android.util.Log
import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import de.schuettslaar.sensoration.application.data.RawClientDataProcessing
import de.schuettslaar.sensoration.domain.PTPHandler
import de.schuettslaar.sensoration.domain.exception.MissingPermissionException
import kotlin.jvm.Throws

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
        SensorType.DECIBEL_FULL_SCALE.sensorId -> MicrophoneSensorHandler(
            context,
            ptpHandler,
            sensorType
        )

        SensorType.SIGNIFICANT_PITCH.sensorId -> MicrophoneSensorHandler(
            context,
            ptpHandler,
            sensorType
        )

        else -> HardwareSensorHandler(context, sensorType, ptpHandler)
    }

    @Throws( MissingPermissionException::class)
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

    /**
     * Checks if the device supports the specified sensor type.
     *
     * This function determines whether the device is capable of handling the given
     * sensor type by delegating the check to the appropriate sensor handler.
     *
     * @param sensorType The type of sensor to check, represented as an integer.
     * @return `true` if the device supports the specified sensor type, `false` otherwise.
     */
    fun checkDeviceSupportsSensorType(sensorType: Int): Boolean {
        val handler = obtainHandlerForSensorType(sensorType)
        return handler.checkDeviceSupportsSensorType(sensorType)
    }
}