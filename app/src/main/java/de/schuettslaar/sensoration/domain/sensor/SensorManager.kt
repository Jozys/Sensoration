package de.schuettslaar.sensoration.domain.sensor
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

open class SensorManager {

    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private val TAG = "SensorManager"
    var onSensorChangedListener: (SensorEvent) -> Unit = {}

    constructor(context: Context, sensorType: Int) {
        this.sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        this.sensor = sensorManager?.getDefaultSensor(sensorType)
    }

    fun startListening() {
        val supportedAndEnabled = sensorManager?.registerListener(
            listener,
            sensor, SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        )
        Log.d(TAG, "Sensor listener registered: $supportedAndEnabled")
    }

    fun stopListening() {
        sensorManager?.unregisterListener(listener)
    }

    val listener: SensorEventListener by lazy {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                onSensorChangedListener(event)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.d(TAG, "Accuracy changed to: $accuracy")
            }
        }
    }

}