package de.schuettslaar.sensoration.nearby

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import de.schuettslaar.sensoration.R

class NearbyWrapper {
    private var context: Context
    private var serviceId: String
    private var status: NearbyStatus = NearbyStatus.STOPPED

    private var connectionLifecycleCallback: ConnectionLifecycleCallback? = null
    private var endpointDiscoveryCallback: EndpointDiscoveryCallback? = null
    private var onEndpointAddCallback: (value: Pair<String, DiscoveredEndpointInfo>) -> Unit
    private var onEndpointRemoveCallback: (id: String) -> Unit
    private var onConnectionResultCallback: (endpointId: String, connectionStatus: ConnectionResolution, nearbyStatus: NearbyStatus) -> Unit
    private var onConnectionInitiatedCallback: (endpointId: String, result: ConnectionInfo) -> Unit
    private var onDisconnectedCallback: (endpointId: String, status: NearbyStatus) -> Unit

    constructor(
        context: Context,
        onEndpointAddCallback: (value: Pair<String, DiscoveredEndpointInfo>) -> Unit,
        onEndpointRemoveCallback: (id: String) -> Unit,
        onConnectionResultCallback: (endpointId: String, connectionStatus: ConnectionResolution, status: NearbyStatus) -> Unit,
        onDisconnectedCallback: (endpointId: String, status: NearbyStatus) -> Unit,
        onConnectionInitiatedCallback: (endpointId: String, result: ConnectionInfo) -> Unit
    ) {
        this.context = context
        this.onEndpointAddCallback = onEndpointAddCallback
        this.onEndpointRemoveCallback = onEndpointRemoveCallback
        this.onConnectionResultCallback = onConnectionResultCallback
        this.onDisconnectedCallback = onDisconnectedCallback
        this.onConnectionInitiatedCallback = onConnectionInitiatedCallback

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            serviceId = context.packageName
        } else {
            serviceId = context.getString(R.string.packageName)
        }
    }

    private fun createEndpointLifecycleCallback() = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            logE("Found Endpoint: $endpointId ${info.endpointName}")
            onEndpointAddCallback(Pair(endpointId, info))
        }

        override fun onEndpointLost(endpointId: String) {
            logE("LOST Endpoint: $endpointId")
            onEndpointRemoveCallback(endpointId)
        }

    }

    private fun createConnectionLifecycleCallback() = object : ConnectionLifecycleCallback() {
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
            status = NearbyStatus.STOPPED
            onDisconnectedCallback(endpointId, status)
        }

    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }

    }

    fun startAdvertising(
        localEndpointName: String,
        callback: (text: String, status: NearbyStatus) -> Unit
    ) {
        if (status == NearbyStatus.ADVERTISING || status == NearbyStatus.DISCOVERING) {
            callback("Already advertising or discovering", status)
            return
        }

        val strategy = Strategy.P2P_STAR
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()

        connectionLifecycleCallback = createConnectionLifecycleCallback()

        Nearby.getConnectionsClient(context).startAdvertising(
            localEndpointName, serviceId, connectionLifecycleCallback!!, advertisingOptions
        ).addOnSuccessListener {
            status = NearbyStatus.ADVERTISING
            callback("successfully started advertising", status)
        }.addOnFailureListener {
            status = NearbyStatus.STOPPED
            callback("starting advertising failed", status)
        }

    }

    fun startDiscovery(callback: (text: String, status: NearbyStatus) -> Unit) {
        if (status == NearbyStatus.ADVERTISING || status == NearbyStatus.DISCOVERING) {
            callback("Already advertising or discovering", status)
            return
        }
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        endpointDiscoveryCallback = createEndpointLifecycleCallback()

        Nearby.getConnectionsClient(
            context
        ).startDiscovery(
            serviceId, endpointDiscoveryCallback!!, discoveryOptions
        )
            .addOnSuccessListener {
                status = NearbyStatus.DISCOVERING
                callback("SUCCESS: Start Discovery", status)
            }
            .addOnFailureListener {
                callback("FAILED: Start Discovery", status)
            }
    }

    fun stopDiscovery(callback: (text: String, status: NearbyStatus) -> Unit) {
        if (status == NearbyStatus.DISCOVERING) {
            Nearby.getConnectionsClient(context).stopDiscovery()
            status = NearbyStatus.STOPPED
            callback("Discovery stopped", status)
        }
    }

    fun stopAdvertising(callback: (text: String, status: NearbyStatus) -> Unit) {
        if (status == NearbyStatus.ADVERTISING) {
            Nearby.getConnectionsClient(context).stopAdvertising()
            status = NearbyStatus.STOPPED
            callback("Advertising stopped", status)
        }
    }

    fun connect(endpointId: String) {
        logE("Connecting to $endpointId")

        connectionLifecycleCallback = createConnectionLifecycleCallback()
        Nearby.getConnectionsClient(context).requestConnection(
            serviceId,
            endpointId,
            connectionLifecycleCallback!!
        )
    }

    fun logE(milf: String) {
        Log.e(this.javaClass.simpleName, milf)
    }
}