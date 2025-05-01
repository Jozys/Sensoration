package de.schuettslaar.sensoration.domain.sensor

class Sensor {
    var sensorType: SensorType? = null
    //TODO: This will later function as functionality for the sensor

    constructor(sensorType: SensorType) {
        this.sensorType = sensorType
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Sensor) {
            this.sensorType?.id == other.sensorType?.id
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return sensorType?.hashCode() ?: 0
    }
}