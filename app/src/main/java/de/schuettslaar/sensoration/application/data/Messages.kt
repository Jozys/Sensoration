package de.schuettslaar.sensoration.application.data

import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.SensorType
import java.io.Serializable

interface Message : Serializable {
    val messageTimeStamp: Long
    val senderDeviceId: DeviceId
    val messageType: MessageType
    val state: ApplicationStatus
}

/**
 * MessageType enum class to define the type of message
 * that is being sent
 */
enum class MessageType {
    SENSOR_DATA,
    HANDSHAKE,
    START_MEASUREMENT,
    STOP_MEASUREMENT,

    PTP_MESSAGE,
}

/**
 * Message designed to be sent from the client to the master
 * containing the sensor data
 */
data class WrappedSensorData(
    override val messageTimeStamp: Long,
    override val senderDeviceId: DeviceId, // ClientId
    override val state: ApplicationStatus,
    val sensorData: ProcessedSensorData
) : Message {
    override val messageType = MessageType.SENSOR_DATA
}

/**
 * Message designed to be sent from the master to the client
 * if the client is freshly connected
 */
data class HandshakeMessage(
    override val messageTimeStamp: Long, // Master
    override val senderDeviceId: DeviceId, // MasterId
    override val state: ApplicationStatus,
    val clientId: DeviceId // Generated ID for the client
) : Message {
    override val messageType = MessageType.HANDSHAKE
}

data class StartMeasurementMessage(
    override val messageTimeStamp: Long,
    override val senderDeviceId: DeviceId,
    override val state: ApplicationStatus,
    val sensorType: SensorType,
    val delay: Long
) : Message {
    override val messageType = MessageType.START_MEASUREMENT
}

data class StopMeasurementMessage(
    override val messageTimeStamp: Long,
    override val senderDeviceId: DeviceId,
    override val state: ApplicationStatus,
) : Message {
    override val messageType = MessageType.STOP_MEASUREMENT
}

data class PTPMessage(
    override val messageTimeStamp: Long,
    override val senderDeviceId: DeviceId,
    override val state: ApplicationStatus,
    val ptpType: PTPMessageType,
) : Message {
    enum class PTPMessageType {
        SYNC,
        FOLLOW_UP,
        DELAY_REQUEST,
        DELAY_RESPONSE,
    }

    override val messageType = MessageType.PTP_MESSAGE
}

