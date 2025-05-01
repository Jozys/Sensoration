package de.schuettslaar.sensoration.domain.sensor

data class ProcessedSensorData(
    val timestamp: Long,
    val sensorType: Int,
    val processingStatus: String,
    val value: Array<Float>
): java.io.Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProcessedSensorData

        if (timestamp != other.timestamp) return false
        if (sensorType != other.sensorType) return false
        if (processingStatus != other.processingStatus) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + sensorType
        result = 31 * result + processingStatus.hashCode()
        result = 31 * result + value.contentHashCode()
        return result
    }

}

data class RawSensorData(
    val timestamp: Long,
    val sensorType: Int,
    val value: FloatArray
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RawSensorData

        if (timestamp != other.timestamp) return false
        if (sensorType != other.sensorType) return false
        if (!value.contentEquals(other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + sensorType
        result = 31 * result + value.contentHashCode()
        return result
    }

}