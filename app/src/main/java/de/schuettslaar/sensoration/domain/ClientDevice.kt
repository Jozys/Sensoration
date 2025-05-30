package de.schuettslaar.sensoration.domain

import android.content.Context
import android.media.MediaActionSound
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
import de.schuettslaar.sensoration.application.data.StopMeasurementMessage
import de.schuettslaar.sensoration.application.data.TestMessage
import de.schuettslaar.sensoration.application.data.UnavailableSensorMessage
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.exception.MissingPermissionException
import de.schuettslaar.sensoration.domain.exception.SensorUnavailableException
import de.schuettslaar.sensoration.domain.exception.UnavailabilityType
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

class ClientDevice : Device {
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
        onSensorUnavailableCallback: (Pair<SensorType, UnavailabilityType>) -> Unit
    ) : super() {
        this.isMainDevice = false
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
        this.sensorManager = SensorManager(context, clientPtpHandler)

        this.onSensorTypeChangedCallback = onSensorTypeChanged
        this.onSensorUnavailableCallback = onSensorUnavailableCallback
        this.onApplicationStatusChangedCallback = onApplicationStatusChanged
    }

    fun startSensorCollection(sensorType: SensorType) {
        if (applicationStatus != ApplicationStatus.IDLE) {
            Log.d(this.javaClass.simpleName, "Device is not idle, Skipping sensor collection")
            return
        }

        onSensorTypeChangedCallback(sensorType)
        if (!sensorManager.checkDeviceSupportsSensorType(sensorType.sensorId)) {
            Log.d(this.javaClass.simpleName, "Device does not support sensor type: $sensorType")
            applicationStatus = ApplicationStatus.IDLE
            throw SensorUnavailableException(sensorType)

        }

        sensorManager.registerSensor(sensorType.sensorId, sensorType.clientDataProcessing)

        // Check permission before starting the sensor
        try {
            sensorManager.startListening()
        } catch (e: MissingPermissionException) {
            Log.d(this.javaClass.simpleName, "Sensor initialization failed: ${e.message}")
            onSensorUnavailableCallback(
                Pair(sensorType, UnavailabilityType.SENSOR_PERMISSION_DENIED)
            )
            applicationStatus = ApplicationStatus.IDLE
            sendUnavailableSensorMessage(sensorType)
            return
        }
        applicationStatus = ApplicationStatus.ACTIVE
        onApplicationStatusChangedCallback(
            applicationStatus,
        )
    }

    fun stopSensorCollection() {
        if (applicationStatus != ApplicationStatus.ACTIVE) {
            Log.d(this.javaClass.simpleName, "Could not stop Device because Device was not active")
            return
        }

        sensorManager.stopListening()
        applicationStatus = ApplicationStatus.IDLE
    }

    fun startPeriodicSending(mainDeviceId: DeviceId, intervalMs: Long = 100) {
        stopPeriodicSending()

        sensorJob = coroutineScope.launch {
            while (isActive) {
                delay(intervalMs)

                val latestSensorData = sensorManager.getLatestSensorData()
                Log.d(this.javaClass.simpleName, "Latest sensor data: $latestSensorData")
                if (latestSensorData != null) {
                    sendSensorData(mainDeviceId, latestSensorData)
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


    private fun sendSensorData(mainDeviceId: DeviceId, sensorData: ProcessedSensorData) {
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
            sendMessage(mainDeviceId, wrappedSensorData)

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
        // Maybe send a disconnect request message to the main device before disconnecting

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
            MessageType.HANDSHAKE -> handleHandshakeMessage(message as HandshakeMessage)
            MessageType.START_MEASUREMENT -> handleMeasurementMessage(message as StartMeasurementMessage)
            MessageType.STOP_MEASUREMENT -> stopMeasurement(message as StopMeasurementMessage)
            MessageType.PTP_MESSAGE -> clientPtpHandler.handleMessage(message as PTPMessage, this)
            MessageType.TEST_MESSAGE -> handleTestMessage(message as TestMessage)
            else -> {
                Logger.getLogger(this.javaClass.simpleName).warning("Unknown message type received")
            }
        }
    }

    private fun handleTestMessage(message: TestMessage) {
        Log.i(this.javaClass.simpleName, "Test message received: ${message.content}")
        val sound = MediaActionSound()
        sound.play(MediaActionSound.STOP_VIDEO_RECORDING)
    }

    private fun stopMeasurement(message: StopMeasurementMessage) {
        stopPeriodicSending()
        stopSensorCollection()
    }


    @Throws(SensorUnavailableException::class)
    private fun handleMeasurementMessage(startMeasurementMessage: StartMeasurementMessage) {
        if (startMeasurementMessage.senderDeviceId != MAIN_DEVICE_ID || connectedDeviceId == null) {
            Logger.getLogger(this.javaClass.simpleName)
                .warning("Received measurement message from unknown device: ${startMeasurementMessage.senderDeviceId}")
            return
        }

        Logger.getLogger(this.javaClass.simpleName)
            .info("Start measurement message received from ${startMeasurementMessage.senderDeviceId}")
        val sensorType = startMeasurementMessage.sensorType
        try {
            startSensorCollection(sensorType)
            startPeriodicSending(connectedDeviceId!!, sensorType.processingDelay)
        } catch (e: SensorUnavailableException) {
            onSensorUnavailableCallback(
                Pair(e.getSensorType(), e.getUnavailabilityType())
            )
            applicationStatus = ApplicationStatus.IDLE
            sendUnavailableSensorMessage(sensorType)
            return
        }

    }

    private fun handleHandshakeMessage(handshakeMessage: HandshakeMessage) {
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

    fun sendTestMessage() {
        Log.d(javaClass.simpleName, "Sending test message")
        if (connectedDeviceId == null) {
            Logger.getLogger(this.javaClass.simpleName)
                .warning("Could not send test message because no device is connected")
            return
        }

        sendMessage(
            connectedDeviceId!!,
            TestMessage(
                messageTimeStamp = clientPtpHandler.getAdjustedTime(),
                senderDeviceId = ownDeviceId!!,
                state = applicationStatus,
                content = "Test message from Client"
            )
        )
    }

    private fun sendUnavailableSensorMessage(
        sensorType: SensorType,
    ) {
        Log.d(
            this.javaClass.simpleName,
            "Sending unavailable sensor message for $sensorType"
        )
        sendMessage(
            connectedDeviceId!!,
            UnavailableSensorMessage(
                messageTimeStamp = clientPtpHandler.getAdjustedTime(),
                senderDeviceId = ownDeviceId!!,
                state = applicationStatus,
                sensorType = sensorType,
            )
        )
    }
}
