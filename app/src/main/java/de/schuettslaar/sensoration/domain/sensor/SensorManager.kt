package de.schuettslaar.sensoration.domain.sensor
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager as AndroidSensorManager
import android.util.Log
import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import de.schuettslaar.sensoration.application.data.RawClientDataProcessing
import de.schuettslaar.sensoration.domain.ApplicationStatus

open class SensorManager {

    private var androidSensorManager: AndroidSensorManager? = null
    private var sensor: Sensor? = null
    private var processor: ClientDataProcessing? = null

    private var latestSensorData: ProcessedSensorData? = null

    private val TAG = "SensorManager"

    constructor(context: Context) {
        this.androidSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as AndroidSensorManager
    }

    fun registerSensor(sensorType: Int, processor: ClientDataProcessing = RawClientDataProcessing()) {
        this.sensor = androidSensorManager?.getDefaultSensor(sensorType)
        this.processor = processor
    }

    fun startListening() {
        if (sensor == null || processor == null) {
            Log.e(TAG, "Sensor or processor not initialized. Call registerSensor() first.")
            return
        }

        val supportedAndEnabled = androidSensorManager?.registerListener(
            listener,
            sensor,
            AndroidSensorManager.SENSOR_STATUS_ACCURACY_HIGH
        )
        Log.d(TAG, "Sensor listener registered: $supportedAndEnabled")
    }

    fun stopListening() {
        androidSensorManager?.unregisterListener(listener)
        latestSensorData = null
    }

    fun getLatestSensorData(): ProcessedSensorData? {
        return latestSensorData
    }

    private val listener: SensorEventListener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return

                val rawSensorData = RawSensorData(
                    timestamp = System.currentTimeMillis(),
                    sensorType = event.sensor.type,
                    value = event.values.clone()
                )

                val processedSensorData = processor!!.processData(rawSensorData)
                latestSensorData = processedSensorData
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Accuracy changed to: $accuracy")
            }
        }
    }

}