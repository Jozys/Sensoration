package de.schuettslaar.sensoration.domain.sensor

import android.hardware.Sensor
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import de.schuettslaar.sensoration.application.data.RawClientDataProcessing

enum class SensorType(
    val sensorId: Int,
    val displayNameId: Int,
    val descriptionId: Int,
    val clientDataProcessing: ClientDataProcessing,
    val processingDelay: Long = 100L, // Delay in milliseconds
) {
    PRESSURE(
        Sensor.TYPE_PRESSURE,
        R.string.sensor_pressure,
        R.string.sensor_pressure_description,
        RawClientDataProcessing()
    ),
    GRAVITY(
        Sensor.TYPE_GRAVITY,
        R.string.sensor_gravity,
        R.string.sensor_gravity_description,
        RawClientDataProcessing()
    ),
    ACCELEROMETER(
        Sensor.TYPE_ACCELEROMETER,
        R.string.sensor_accelerometer,
        R.string.sensor_accelerometer_description,
        RawClientDataProcessing(),
        processingDelay = 500L
    )
    ;

    companion object {
        fun fromId(id: Int): SensorType? = SensorType.entries.find { it.sensorId == id }

        fun getDisplayNames(): List<Int> = SensorType.entries.map { it.displayNameId }

        fun getIds(): List<Int> = SensorType.entries.map { it.sensorId }
    }
}