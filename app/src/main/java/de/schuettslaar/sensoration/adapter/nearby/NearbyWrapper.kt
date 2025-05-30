package de.schuettslaar.sensoration.adapter.nearby

import android.content.Context
import android.media.MediaActionSound
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import de.schuettslaar.sensoration.domain.DeviceId

abstract class NearbyWrapper {
    internal var context: Context
    internal var serviceId: String
    internal var status: NearbyStatus = NearbyStatus.STOPPED

    internal var connectionLifecycleCallback: ConnectionLifecycleCallback? = null
    internal lateinit var payloadCallback: PayloadCallback

    internal lateinit var onConnectionResultCallback: (endpointId: DeviceId, connectionStatus: ConnectionResolution, nearbyStatus: NearbyStatus) -> Unit
    internal lateinit var onConnectionInitiatedCallback: (endpointId: DeviceId, result: ConnectionInfo) -> Unit
    internal lateinit var onDisconnectedCallback: (endpointId: DeviceId, status: NearbyStatus) -> Unit

    internal lateinit var onPayloadReceivedCallback: (endPointId: DeviceId, payload: Payload) -> Unit

    internal val connectionsClient: ConnectionsClient

    constructor(
        context: Context,
    ) {
        this.context = context
        serviceId = context.packageName
        connectionsClient = Nearby.getConnectionsClient(context)


    }


    fun logE(milf: String) {
        Log.e(this.javaClass.simpleName, milf)
    }

    fun sendData(toEndpointId: DeviceId, bytes: ByteArray) {
        val payload: Payload = Payload.fromBytes(bytes)
        connectionsClient.sendPayload(toEndpointId.name, payload)
    }


    abstract fun start(callback: (text: String, status: NearbyStatus) -> Unit)
    abstract fun stop(callback: (text: String, status: NearbyStatus) -> Unit)

    fun connect(endpointId: DeviceId) {
        logE("Connecting to $endpointId")
        var deviceName =
            android.provider.Settings.Global.getString(context.contentResolver, "device_name")
        connectionLifecycleCallback = createConnectionLifecycleCallback()
        connectionsClient.requestConnection(
            deviceName,
            endpointId.name,
            connectionLifecycleCallback!!
        )
    }

    fun disconnect(endpointId: DeviceId) {
        logE("Disconnecting from $endpointId")
        connectionsClient.disconnectFromEndpoint(endpointId.name)
    }

    internal fun createConnectionLifecycleCallback() = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, result: ConnectionInfo) {
            val deviceId = DeviceId(endpointId)
            onConnectionInitiatedCallback(deviceId, result)
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    logE("CONNECTION OK")

                }
                // We're connected! Can now start sending and receiving data.
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    logE("CONNECTION REJECTED")
                }
                // The connection was rejected by one or both sides.
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    logE("CONNECTION ERROR")
                }
                // The connection broke before it was able to be accepted.
                else -> {
                    logE("CONNECTION UNKNOWN")
                }
                // Unknown status code
            }
            onConnectionResultCallback(DeviceId(endpointId), result, status)
        }

        override fun onDisconnected(endpointId: String) {
            logE("CONNECTION DISCONNECTED")
            onDisconnectedCallback(DeviceId(endpointId), status)
        }

    }
}

internal fun createPayloadCallback(onPayloadReceivedCallback: (endPointId: DeviceId, payload: Payload) -> Unit): PayloadCallback =
    object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            MediaActionSound()
//            sound.play(MediaActionSound.STOP_VIDEO_RECORDING)
            Log.d(this.javaClass.simpleName, "Got message from $endpointId > ${payload.asBytes()}")
            onPayloadReceivedCallback(DeviceId(endpointId), payload)
        }

        override fun onPayloadTransferUpdate(
            endpointId: String,
            update: PayloadTransferUpdate
        ) {

        }
    }