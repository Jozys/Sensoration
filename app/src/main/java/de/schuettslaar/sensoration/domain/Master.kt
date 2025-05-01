package de.schuettslaar.sensoration.domain

import android.content.Context
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import de.schuettslaar.sensoration.adapter.nearby.AdvertiseNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.application.data.Message
import de.schuettslaar.sensoration.application.data.MessageType
import de.schuettslaar.sensoration.application.data.StartMeasurementMessage
import de.schuettslaar.sensoration.application.data.StopMeasurementMessage
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.sensor.SensorType
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.util.logging.Logger

private const val PROCESSED_VALUES_CAPACITY = 10

const val MASTER_NAME = "MASTER"

class Master : Device {
    private val sensorDataMap = mutableMapOf<String, CircularFifoQueue<WrappedSensorData>>()


    private var sensorType: SensorType? = null

    constructor(
        context: Context,
        onConnectionResultCallback: (String, ConnectionResolution, NearbyStatus) -> Unit,
        onDisconnectedCallback: (String, NearbyStatus) -> Unit,
        onConnectionInitiatedCallback: (String, ConnectionInfo) -> Unit
    ) : super() {
        this.ownDeviceId = MASTER_NAME
        this.isMaster = true
        this.wrapper = AdvertiseNearbyWrapper(
            context = context,
            onConnectionResultCallback = onConnectionResultCallback,
            onDisconnectedCallback = onDisconnectedCallback,
            onConnectionInitiatedCallback = onConnectionInitiatedCallback,
            onPayloadReceivedCallback = { endPointId, payload ->
                if (payload != null && payload.asBytes() != null) {
                    messageReceived(endPointId, payload.asBytes()!!)
                } else {
                    Logger.getLogger(this.javaClass.simpleName).info("Payload is null")
                }

            })
    }

    override fun cleanUp() {
        if (connectedDeviceId == null) {
            Logger.getLogger(this.javaClass.simpleName)
                .warning("Disconnect called but no device is connected")
            return
        }

        stopMeasurement()
        connectedDevices.forEach {
            wrapper?.disconnect(it)
        }
        wrapper?.stop { text, status ->
            Logger.getLogger(this.javaClass.simpleName)
                .info("Disconnected from all devices")
        }
    }

    override fun messageReceived(endpointId: String, payload: ByteArray) {
        val message: Message? = parseMessage(endpointId, payload)
        if (message == null) {
            Logger.getLogger(this.javaClass.simpleName).warning("Message is null")
            return
        }

        when (message.messageType) {
            MessageType.SENSOR_DATA -> {
                val sensorData = message as WrappedSensorData
                Logger.getLogger(this.javaClass.simpleName)
                    .info("Sensor data received from ${sensorData.senderDeviceId} > ${sensorData.sensorData}")
                sensorDataMap.getOrDefault(
                    endpointId, CircularFifoQueue(
                        PROCESSED_VALUES_CAPACITY
                    )
                ).add(sensorData)
            }

            else -> {
                Logger.getLogger(this.javaClass.simpleName).warning("Unknown message type received")
            }
        }
    }

    fun broadcastMessage(message: Message) {
        connectedDevices.forEach { deviceId ->
            sendMessage(deviceId, message)
        }
        Logger.getLogger(this.javaClass.simpleName)
            .info("Broadcasted message $message to ${connectedDevices.size} devices: $connectedDevices")
    }

    fun startMeasurement(sensorType: SensorType) {
        var startMeasurementMessage = StartMeasurementMessage(
            messageTimeStamp = System.currentTimeMillis().toLong(),
            senderDeviceId = ownDeviceId.toString(),
            state = ApplicationStatus.DESTINATION,
            sensorType = sensorType
        )
        broadcastMessage(startMeasurementMessage)
    }

    fun stopMeasurement() {
        val stopMeasurementMessage = StopMeasurementMessage(
            messageTimeStamp = System.currentTimeMillis().toLong(),
            senderDeviceId = ownDeviceId.toString(),
            state = ApplicationStatus.DESTINATION,
        )
        broadcastMessage(stopMeasurementMessage)
    }

    fun setSensor(sensor: SensorType) {
        this.sensorType = sensor
        // TODO: This needs to send the updated sensor to all connected devices
    }
}