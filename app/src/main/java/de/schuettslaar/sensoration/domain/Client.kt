package de.schuettslaar.sensoration.domain

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.adapter.nearby.DiscoverNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.application.data.HandshakeMessage
import de.schuettslaar.sensoration.application.data.Message
import de.schuettslaar.sensoration.application.data.MessageType
import de.schuettslaar.sensoration.application.data.PTPMessage
import de.schuettslaar.sensoration.application.data.StartMeasurementMessage
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.SensorManager
import de.schuettslaar.sensoration.domain.sensor.SensorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.logging.Logger

class Client : Device {
    private val sensorManager: SensorManager
    private val clientPtpHandler: ClientPTPHandler = ClientPTPHandler()

    // For periodic sending
    private var sensorJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val onSensorTypeChangedCallback: (SensorType) -> Unit
    private val onApplicationStatusChangedCallback: (ApplicationStatus) -> Unit

    constructor(
        context: Context,
        onEndpointAddCallback: (Pair<DeviceId, DiscoveredEndpointInfo>) -> Unit,
        onEndpointRemoveCallback: (DeviceId) -> Unit,
        onConnectionInitiatedCallback: (DeviceId, ConnectionInfo) -> Unit,
        onConnectionResultCallback: (DeviceId, ConnectionResolution, NearbyStatus) -> Unit,
        onDisconnectedCallback: (DeviceId, NearbyStatus) -> Unit,
        onSensorTypeChanged: (SensorType) -> Unit,
        onApplicationStatusChanged: (ApplicationStatus) -> Unit,
    ) : super() {
        this.isMaster = false
        this.wrapper = DiscoverNearbyWrapper(
            context = context,
            onEndpointAddCallback = onEndpointAddCallback,
            onEndpointRemoveCallback = onEndpointRemoveCallback,
            onConnectionInitiatedCallback = onConnectionInitiatedCallback,
            onConnectionResultCallback = onConnectionResultCallback,
            onDisconnectedCallback = onDisconnectedCallback,
            onPayloadReceivedCallback = { endPointId, payload ->
                if (payload != null && payload.asBytes() != null) {
                    messageReceived(endPointId, payload.asBytes()!!)
                } else {
                    Log.d(this.javaClass.simpleName, "Payload is null")
                }
            }
        )
        sensorManager = SensorManager(context, clientPtpHandler)

        onSensorTypeChangedCallback = onSensorTypeChanged
        onApplicationStatusChangedCallback = onApplicationStatusChanged
    }

    fun startSensorCollection(sensorType: SensorType) {
        if (applicationStatus != ApplicationStatus.IDLE) {
            Log.d(this.javaClass.simpleName, "Device is not idle, Skipping sensor collection")
            return
        }

        if (!sensorManager.checkDeviceSupportsSensorType(sensorType.sensorId)) {
            Log.d(this.javaClass.simpleName, "Device does not support sensor type: $sensorType")
            applicationStatus = ApplicationStatus.IDLE
            return
        }

        sensorManager.registerSensor(sensorType.sensorId, sensorType.clientDataProcessing)
        sensorManager.startListening()
        applicationStatus = ApplicationStatus.ACTIVE
        onApplicationStatusChangedCallback(
            applicationStatus,
        )
        onSensorTypeChangedCallback(sensorType)
    }

    fun stopSensorCollection() {
        if (applicationStatus != ApplicationStatus.ACTIVE) {
            Log.d(this.javaClass.simpleName, "Could not stop Device because Device was not active")
            return
        }

        sensorManager.stopListening()
        applicationStatus = ApplicationStatus.IDLE
    }

    fun startPeriodicSending(masterId: DeviceId, intervalMs: Long = 100) {
        stopPeriodicSending()

        sensorJob = coroutineScope.launch {
            while (isActive) {
                delay(intervalMs)

                val latestSensorData = sensorManager.getLatestSensorData()
                Log.d(this.javaClass.simpleName, "Latest sensor data: $latestSensorData")
                if (latestSensorData != null) {
                    sendSensorData(masterId, latestSensorData)
                } else {
                    Log.d(this.javaClass.simpleName, "No sensor data available")
                }
            }
        }
    }

    fun stopPeriodicSending() {
        sensorJob?.cancel()
        sensorJob = null
    }


    private fun sendSensorData(masterId: DeviceId, sensorData: ProcessedSensorData) {
        if (applicationStatus != ApplicationStatus.ACTIVE) {
            Log.d(
                this.javaClass.simpleName,
                "Could not send SensorData because Device was not active"
            )
            return
        }

        try {
            val wrappedSensorData = WrappedSensorData(
                messageTimeStamp = clientPtpHandler.getAdjustedTime(),
                ownDeviceId!!,
                applicationStatus,
                sensorData
            )
            sendMessage(masterId, wrappedSensorData)

        } catch (e: Exception) {
            Log.d(this.javaClass.simpleName, "Failed to send sensor data: ${e.toString()}")
        }
    }


    override fun cleanUp() {
        if (connectedDeviceId == null) {
            Logger.getLogger(this.javaClass.simpleName)
                .warning("Disconnect called but no device is connected")
            return
        }

        stopPeriodicSending()
        // Maybe send a disconnect request message to the master before disconnecting

        sensorManager.cleanup()

        wrapper?.disconnect(connectedDeviceId!!)
        this.connectedDeviceId = null
        stopSensorCollection()
    }

    override fun messageReceived(endpointId: DeviceId, payload: ByteArray) {
        val message: Message? = parseMessage(endpointId, payload)
        if (message == null) {
            Logger.getLogger(this.javaClass.simpleName).warning("Message is null")
            return
        }
        Logger.getLogger(this.javaClass.simpleName)
            .info("Message received from $endpointId of type ${message.messageType}")

        when (message.messageType) {
            MessageType.HANDSHAKE -> handleHandshakeMessage(message)
            MessageType.START_MEASUREMENT -> handleMeasurementMessage(message)
            MessageType.STOP_MEASUREMENT -> stopMeasurement(message)
            MessageType.PTP_MESSAGE -> clientPtpHandler.handleMessage(message as PTPMessage, this)
            else -> {
                Logger.getLogger(this.javaClass.simpleName).warning("Unknown message type received")
            }
        }
    }

    private fun stopMeasurement(message: Message) {
        stopPeriodicSending()
        stopSensorCollection()
    }

    private fun handleMeasurementMessage(message: Message) {
        if (message.senderDeviceId != MASTER_NAME || connectedDeviceId == null) {
            Logger.getLogger(this.javaClass.simpleName)
                .warning("Received measurement message from unknown device: ${message.senderDeviceId}")
            return
        }

        val startMeasurementMessage = message as StartMeasurementMessage
        Logger.getLogger(this.javaClass.simpleName)
            .info("Start measurement message received from ${startMeasurementMessage.senderDeviceId}")
        val sensorType = startMeasurementMessage.sensorType
        startSensorCollection(sensorType)
        startPeriodicSending(connectedDeviceId!!, sensorType.processingDelay)
    }

    private fun handleHandshakeMessage(message: Message) {
        val handshakeMessage = message as HandshakeMessage
        Logger.getLogger(this.javaClass.simpleName)
            .info("Handshake message received from ${handshakeMessage.senderDeviceId}")
        ownDeviceId = handshakeMessage.clientId
    }

    fun sendDelayRequest(delayRequestMessage: PTPMessage) {
        Log.d(
            javaClass.simpleName, "sendDelayRequest: $delayRequestMessage"
        )
        sendMessage(connectedDeviceId!!, delayRequestMessage)
    }
}
