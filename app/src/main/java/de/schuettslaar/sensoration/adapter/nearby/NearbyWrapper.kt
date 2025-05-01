package de.schuettslaar.sensoration.adapter.nearby

import android.content.Context
import android.media.MediaActionSound
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import java.io.DataInputStream

abstract class NearbyWrapper {
    internal var context: Context
    internal var serviceId: String
    internal var status: NearbyStatus = NearbyStatus.STOPPED

    internal var connectionLifecycleCallback: ConnectionLifecycleCallback? = null
    internal lateinit var payloadCallback: PayloadCallback

    internal lateinit var onConnectionResultCallback: (endpointId: String, connectionStatus: ConnectionResolution, nearbyStatus: NearbyStatus) -> Unit
    internal lateinit var onConnectionInitiatedCallback: (endpointId: String, result: ConnectionInfo) -> Unit
    internal lateinit var onDisconnectedCallback: (endpointId: String, status: NearbyStatus) -> Unit

    internal lateinit var onPayloadReceivedCallback: (endPointId: String, payload: Payload) -> Unit

    constructor(
        context: Context,
    ) {
        this.context = context
        serviceId = context.packageName

    }


    fun logE(milf: String) {
        Log.e(this.javaClass.simpleName, milf)
    }

    fun sendData(toEndpointId: String, stream: DataInputStream) {
        val payload: Payload = Payload.fromStream(stream)
        Nearby.getConnectionsClient(context).sendPayload(toEndpointId, payload)
    }


    abstract fun start(callback: (text: String, status: NearbyStatus) -> Unit)
    abstract fun stop(callback: (text: String, status: NearbyStatus) -> Unit)

    fun connect(endpointId: String) {
        logE("Connecting to $endpointId")
        var deviceName =
            android.provider.Settings.Global.getString(context.contentResolver, "device_name")
        connectionLifecycleCallback = createConnectionLifecycleCallback()
        Nearby.getConnectionsClient(context).requestConnection(
            deviceName,
            endpointId,
            connectionLifecycleCallback!!
        )
    }

    fun disconnect(endpointId: String) {
        logE("Disconnecting from $endpointId")
        Nearby.getConnectionsClient(context).disconnectFromEndpoint(endpointId)
    }

    internal fun createConnectionLifecycleCallback() = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, result: ConnectionInfo) {
            onConnectionInitiatedCallback(endpointId, result)
            Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback)
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
            onConnectionResultCallback(endpointId, result, status)
        }

        override fun onDisconnected(endpointId: String) {
            logE("CONNECTION DISCONNECTED")
            onDisconnectedCallback(endpointId, status)
        }

    }
}

internal fun createPayloadCallback(onPayloadReceivedCallback: (endPointId: String, payload: Payload) -> Unit): PayloadCallback =
    object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {val sound = MediaActionSound()
            sound.play(MediaActionSound.START_VIDEO_RECORDING)
            onPayloadReceivedCallback(endpointId, payload)
            Log.d(this.javaClass.simpleName, "Got message from" + endpointId + payload.asStream())
        }

        override fun onPayloadTransferUpdate(
            endpointId: String,
            update: PayloadTransferUpdate
        ) {

        }
    }