package de.schuettslaar.sensoration.domain

import android.content.Context
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import de.schuettslaar.sensoration.adapter.nearby.AdvertiseNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.application.data.Message
import de.schuettslaar.sensoration.application.data.MessageType
import de.schuettslaar.sensoration.application.data.PTPMessage
import de.schuettslaar.sensoration.application.data.StartMeasurementMessage
import de.schuettslaar.sensoration.application.data.StopMeasurementMessage
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.sensor.SensorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.util.logging.Logger

private const val PROCESSED_VALUES_CAPACITY = 10

const val MASTER_NAME = "MASTER"

class Master : Device {
    private val sensorDataMap = mutableMapOf<String, CircularFifoQueue<WrappedSensorData>>()

    // For periodic sending
    private var ptpJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val onSensorDataChangedCallback: (String, List<WrappedSensorData>, ApplicationStatus) -> Unit

    private var sensorType: SensorType? = null

    constructor(
        context: Context,
        onConnectionResultCallback: (String, ConnectionResolution, NearbyStatus) -> Unit,
        onDisconnectedCallback: (String, NearbyStatus) -> Unit,
        onConnectionInitiatedCallback: (String, ConnectionInfo) -> Unit,
        onSensorDataChangedCallback: (String, List<WrappedSensorData>, ApplicationStatus) -> Unit,
    ) : super() {
        this.ownDeviceId = MASTER_NAME
        this.isMaster = true
        this.onSensorDataChangedCallback = onSensorDataChangedCallback
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
            MessageType.SENSOR_DATA -> processSensorData(message as WrappedSensorData, endpointId)
            MessageType.PTP_MESSAGE -> processPTPMessage(message as PTPMessage, endpointId)

            else -> {
                Logger.getLogger(this.javaClass.simpleName).warning("Unknown message type received")
            }
        }
    }


    private fun processSensorData(
        sensorData: WrappedSensorData,
        endpointId: String
    ) {
        Logger.getLogger(this.javaClass.simpleName)
            .info("Sensor data received from ${sensorData.senderDeviceId} > ${sensorData.sensorData}")
        var data = sensorDataMap.getOrDefault(
            endpointId, CircularFifoQueue(
                PROCESSED_VALUES_CAPACITY
            )
        )
        data.add(sensorData)
        sensorDataMap[endpointId] = data


        onSensorDataChangedCallback(
            endpointId,
            sensorDataMap[endpointId]?.toList() ?: emptyList(),
            sensorData.state
        )
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
            sensorType = sensorType,
            delay = sensorType.processingDelay
        )
        startPTP(1000)
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

    fun startPTP(intervalMs: Long = 1000) {
        ptpJob = coroutineScope.launch {
            while (isActive) {
                delay(intervalMs)
                ptpSync()
            }
        }
    }

    private fun ptpSync() {
        var t1 = System.currentTimeMillis().toLong()

        var ptpSync = PTPMessage(
            messageTimeStamp = t1,
            senderDeviceId = ownDeviceId.toString(),
            state = ApplicationStatus.DESTINATION,
            ptpType = PTPMessage.PTPMessageType.SYNC,
        )
        broadcastMessage(ptpSync)

        var ptpFollowup = PTPMessage(
            messageTimeStamp = t1,
            senderDeviceId = ownDeviceId.toString(),
            state = ApplicationStatus.DESTINATION,
            ptpType = PTPMessage.PTPMessageType.FOLLOW_UP,
        )
        broadcastMessage(ptpFollowup)
    }

    private fun processPTPMessage(
        ptpMessage: PTPMessage,
        endPointId: String
    ) {
        Logger.getLogger(this.javaClass.simpleName)
            .info("PTP message received from ${ptpMessage.senderDeviceId} > ${ptpMessage.ptpType}")
        if (ptpMessage.ptpType == PTPMessage.PTPMessageType.DELAY_REQUEST) {
            var t4 = System.currentTimeMillis().toLong()
            var ptpDelayResponse = PTPMessage(
                messageTimeStamp = t4,
                senderDeviceId = ownDeviceId.toString(),
                state = ApplicationStatus.DESTINATION,
                ptpType = PTPMessage.PTPMessageType.DELAY_RESPONSE,
            )
            sendMessage(endPointId, ptpDelayResponse)
        } else {
            Logger.getLogger(this.javaClass.simpleName)
                .info("PTP message type not supported")
        }
    }
}