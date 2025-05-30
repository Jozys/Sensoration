package de.schuettslaar.sensoration.domain.exception

import de.schuettslaar.sensoration.domain.sensor.SensorType

class SensorUnavailableException(private val sensorType: SensorType, private val unavailabilityType: UnavailabilityType = UnavailabilityType.SENSOR_NOT_SUPPORTED ) :Exception() {
    override val message: String
        get() = "Sensor is unavailable. Please check if the sensor is connected and functioning properly."

    fun getSensorType(): SensorType {
        return sensorType
    }

    fun getUnavailabilityType(): UnavailabilityType {
        return unavailabilityType
    }
}

enum class UnavailabilityType {
    SENSOR_NOT_SUPPORTED,
    SENSOR_PERMISSION_DENIED,
}