package de.schuettslaar.sensoration

import android.content.Context
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

class NearbyWrapper2 {
    private var context: Context
    private val SERVICE_ID: String = "1234"

    private var connectionLifecycleCallback: ConnectionLifecycleCallback? = null
    private var endpointDiscoveryCallback: EndpointDiscoveryCallback? = null
    private var onEndpointAddCallback : (value: Pair<String, DiscoveredEndpointInfo>) -> Unit
    private var onEndpointRemoveCallback : (id: String) -> Unit

    constructor(context: Context, onEndpointAddCallback: (value: Pair<String, DiscoveredEndpointInfo>) -> Unit, onEndpointRemoveCallback: (id: String) -> Unit) {
        this.context = context
        this.onEndpointAddCallback = onEndpointAddCallback
        this.onEndpointRemoveCallback = onEndpointRemoveCallback
    }

    private fun createEndpointLifecycleCallback() = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            logE("Found Enpoint: $endpointId ${info.endpointName}")
            onEndpointAddCallback(Pair(endpointId, info))
        }

        override fun onEndpointLost(endpointId: String) {
            logE("LOST Enpoint: $endpointId")
            onEndpointRemoveCallback(endpointId);
        }

    }

    private fun createConnectionLifecycleCallback() = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, result: ConnectionInfo) {
            Nearby.getConnectionsClient(context).acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            when (result.getStatus().getStatusCode()) {
                ConnectionsStatusCodes.STATUS_OK  -> {
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
                    logE("CONNECTION SIMP")
                }
                // Unknown status code
            }
        }

        override fun onDisconnected(p0: String) {
            logE("CONNECTION DISCONNECTED")
        }

    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {

        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {

        }

    }

    fun startAdvertising(localEndpointName: String, callback: (text: String) -> Unit) {
        val strategy = Strategy.P2P_STAR
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()

        connectionLifecycleCallback = createConnectionLifecycleCallback()

        Nearby.getConnectionsClient(context).startAdvertising(
                localEndpointName, SERVICE_ID, connectionLifecycleCallback!!, advertisingOptions
            ).addOnSuccessListener {
                callback("successfully started advertising")
            }.addOnFailureListener {
                callback("starting advertising failed")
            }

    }

    fun startDiscovery(callback: (text: String) -> Unit) {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        endpointDiscoveryCallback = createEndpointLifecycleCallback()

        Nearby.getConnectionsClient(
            context
        ).startDiscovery(
            SERVICE_ID, endpointDiscoveryCallback!!, discoveryOptions
        )
            .addOnSuccessListener { callback("Start Discovery") }
            .addOnFailureListener { callback("FAILED: Start Discovery") }
    }

    fun logE(milf: String){
        Log.e(this.javaClass.simpleName, milf)
    }
}