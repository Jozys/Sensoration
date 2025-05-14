package de.schuettslaar.sensoration.domain.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import de.schuettslaar.sensoration.domain.PTPHandler
import android.hardware.SensorManager as AndroidSensorManager

class HardwareSensorHandler(
    private val context: Context,
    private val sensorType: Int,
    private val ptpHandler: PTPHandler
) : SensorHandler {

    private var androidSensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager
    private var sensor: Sensor? = null
    private var processor: ClientDataProcessing? = null
    private var latestSensorData: ProcessedSensorData? = null
    private val TAG = "HardwareSensorHandler"

    override fun initialize(processor: ClientDataProcessing) {
        this.sensor = androidSensorManager.getDefaultSensor(sensorType)
        this.processor = processor
    }

    override fun startListening() {
        if (sensor == null || processor == null) {
            Log.e(TAG, "Sensor or processor not initialized")
            return
        }

        androidSensorManager.registerListener(
            listener,
            sensor,
            AndroidSensorManager.SENSOR_STATUS_ACCURACY_HIGH
        )
    }

    override fun stopListening() {
        androidSensorManager.unregisterListener(listener)
        latestSensorData = null
    }

    override fun getLatestData(): ProcessedSensorData? {
        return latestSensorData
    }

    override fun cleanup() {
        stopListening()
    }
    
    override fun checkDeviceSupportsSensorType(sensorType: Int): Boolean {
        this.sensor = androidSensorManager.getDefaultSensor(sensorType)
        return this.sensor != null
    }

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event == null) return

            val rawSensorData = RawSensorData(
                timestamp = ptpHandler.getAdjustedTime(),
                sensorType = event.sensor.type,
                value = event.values.clone()
            )

            processor?.let {
                latestSensorData = it.processData(rawSensorData)
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            Log.d(TAG, "Accuracy changed to: $accuracy")
        }
    }
}