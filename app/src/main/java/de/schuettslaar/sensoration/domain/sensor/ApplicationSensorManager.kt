package de.schuettslaar.sensoration.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent

class ApplicationSensorManager(context: Context, onSensorChangeListener: (SensorEvent) -> Unit) : SensorManager(
    context,
    Sensor.TYPE_LIGHT
) {
//    val deviceSensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)


}