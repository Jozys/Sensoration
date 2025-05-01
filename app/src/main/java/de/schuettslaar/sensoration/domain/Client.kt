package de.schuettslaar.sensoration.domain

import android.content.Context
import android.hardware.Sensor
import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.adapter.nearby.DiscoverNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.application.data.Message
import de.schuettslaar.sensoration.application.data.MessageType
import de.schuettslaar.sensoration.application.data.RawClientDataProcessing
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.ObjectOutputStream
import java.util.logging.Logger

class Client : Device {
    private val sensorManager: SensorManager

    // For periodic sending
    private var sensorJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    constructor(
        context: Context,
        onEndpointAddCallback: (Pair<String, DiscoveredEndpointInfo>) -> Unit,
        onEndpointRemoveCallback: (String) -> Unit,
        onConnectionInitiatedCallback: (String, ConnectionInfo) -> Unit,
        onConnectionResultCallback: (String, ConnectionResolution, NearbyStatus) -> Unit,
        onDisconnectedCallback: (String, NearbyStatus) -> Unit
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
        sensorManager = SensorManager(context)
    }

    fun startSensorCollection(sensorType: GatherableDeviceDataTypes) {
        if (applicationStatus != ApplicationStatus.IDLE) {
            Log.d(this.javaClass.simpleName, "Device is not idle, Skipping sensor collection")
            return
        }

        if (!checkDeviceSupportsSensorType(sensorType)) {
            Log.d(this.javaClass.simpleName, "Device does not support sensor type: $sensorType")
            applicationStatus = ApplicationStatus.IDLE
            return
        }

        when (sensorType) {
            GatherableDeviceDataTypes.ACCELEROMETER -> sensorManager.registerSensor(Sensor.TYPE_ACCELEROMETER, RawClientDataProcessing())
            else -> {
                Log.d(this.javaClass.simpleName, "Sensor type not supported: $sensorType")
                applicationStatus = ApplicationStatus.IDLE
                return
            }
        }

        sensorManager.startListening()
        applicationStatus = ApplicationStatus.ACTIVE
    }

    fun stopSensorCollection(){
        if (applicationStatus != ApplicationStatus.ACTIVE) {
            Log.d(this.javaClass.simpleName, "Could not stop Device because Device was not active")
            return
        }

        sensorManager.stopListening()
        applicationStatus = ApplicationStatus.IDLE
    }

    fun startPeriodicSending(masterId: String, intervalMs: Long = 100) {
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


    private fun sendSensorData(masterId: String, sensorData: ProcessedSensorData){
        if (applicationStatus != ApplicationStatus.ACTIVE) {
            Log.d(this.javaClass.simpleName, "Could not send SensorData because Device was not active")
            return
        }

        try {
            val wrappedSensorData = WrappedSensorData(
                messageTimeStamp = System.currentTimeMillis().toLong(),
                deviceId.toString(),
                applicationStatus,
                sensorData
            )

            // Serialize and send
            ByteArrayOutputStream().use { bos ->
                ObjectOutputStream(bos).use { oos ->
                    oos.writeObject(wrappedSensorData)
                }
                val bytes = bos.toByteArray()
                sendData(masterId, DataInputStream(ByteArrayInputStream(bytes)))

            }
        } catch (e: Exception) {
            Log.d(this.javaClass.simpleName, "Failed to send sensor data: ${e.toString()}")
        }
    }


    private fun checkDeviceSupportsSensorType(sensorType: GatherableDeviceDataTypes): Boolean {
        return true; // TODO implement checks
    }

    override fun disconnect(endpointId: String) {
        super.disconnect(endpointId)
        stopSensorCollection()
    }

    override fun messageReceived(endpointId: String, payload: ByteArray) {
        val message: Message? = parseMessage(endpointId, payload)
        if (message == null) {
            Logger.getLogger(this.javaClass.simpleName).warning("Message is null")
            return
        }

        when(message.messageType){
            else -> {
                Logger.getLogger(this.javaClass.simpleName).warning("Unknown message type received")
            }
        }
    }
}

enum class GatherableDeviceDataTypes() {
    RANDOM_DATA(),
    ACCELEROMETER(),
    GYROSCOPE(),
    MAGNETIC_FIELD(),
    LIGHT(),
    PROXIMITY(),
    TEMPERATURE(),
    HUMIDITY(),
    PRESSURE()
}