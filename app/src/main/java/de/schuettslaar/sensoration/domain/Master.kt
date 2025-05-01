package de.schuettslaar.sensoration.domain

import android.content.Context
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import de.schuettslaar.sensoration.adapter.nearby.AdvertiseNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus

import de.schuettslaar.sensoration.application.data.Message
import de.schuettslaar.sensoration.application.data.MessageType
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import org.apache.commons.collections4.queue.CircularFifoQueue
import de.schuettslaar.sensoration.domain.sensor.SensorType

import java.util.logging.Logger

private const val PROCESSED_VALUES_CAPACITY = 10

class Master : Device {
    private val sensorDataMap = mutableMapOf<String, CircularFifoQueue<WrappedSensorData>>()


    private var sensorType: SensorType? = null

    constructor(
        context: Context,
        onConnectionResultCallback: (String, ConnectionResolution, NearbyStatus) -> Unit,
        onDisconnectedCallback: (String, NearbyStatus) -> Unit,
        onConnectionInitiatedCallback: (String, ConnectionInfo) -> Unit
    ) : super() {
        this.ownDeviceId = "MASTER"
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

            });
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
                    .info("Sensor data received from ${sensorData.senderDeviceId}")
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

    fun setSensor(sensor: SensorType) {
        this.sensorType = sensor
        // TODO: This needs to send the updated sensor to all connected devices
    }
}