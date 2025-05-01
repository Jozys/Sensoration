package de.schuettslaar.sensoration.domain.sensor

import android.hardware.Sensor

enum class SensorType(val id: Int, val displayNameId: Int, val descriptionId: Int) {
    PRESSURE(
        Sensor.TYPE_PRESSURE,
        de.schuettslaar.sensoration.R.string.sensor_pressure,
        de.schuettslaar.sensoration.R.string.sensor_pressure_description
    ),
    GRAVITY(
        Sensor.TYPE_GRAVITY,
        de.schuettslaar.sensoration.R.string.sensor_gravity,
        de.schuettslaar.sensoration.R.string.sensor_gravity_description
    ), ;

    companion object {
        fun fromId(id: Int): SensorType? = SensorType.entries.find { it.id == id }

        fun getDisplayNames(): List<Int> = SensorType.entries.map { it.displayNameId }

        fun getIds(): List<Int> = SensorType.entries.map { it.id }
    }
}