package de.schuettslaar.sensoration.application.data

import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import java.io.Serializable

interface Message: Serializable {
    val messageTimeStamp: Long
    val deviceId: String
    val messageType: MessageType
    val state: ApplicationStatus
}

enum class MessageType {
    SENSOR_DATA,
    SYNCHRONIZE
}

data class WrappedSensorData(
    override val messageTimeStamp: Long,
    override val deviceId: String,
    override val state: ApplicationStatus,
    val sensorData: ProcessedSensorData
): Message {
    override val messageType = MessageType.SENSOR_DATA
}