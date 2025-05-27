package de.schuettslaar.sensoration.adapter.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import de.schuettslaar.sensoration.domain.DeviceId

class DiscoverNearbyWrapper : NearbyWrapper {

    private var onEndpointAddCallback: ((value: Pair<DeviceId, DiscoveredEndpointInfo>) -> Unit);
    private var onEndpointRemoveCallback: ((id: DeviceId) -> Unit)
    private var endpointDiscoveryCallback: EndpointDiscoveryCallback? = null


    constructor(
        context: Context,
        onEndpointAddCallback: (value: Pair<DeviceId, DiscoveredEndpointInfo>) -> Unit,
        onEndpointRemoveCallback: (id: DeviceId) -> Unit,
        onConnectionInitiatedCallback: (endpointId: DeviceId, result: ConnectionInfo) -> Unit,
        onDisconnectedCallback: (endpointId: DeviceId, status: NearbyStatus) -> Unit,
        onConnectionResultCallback: (endpointId: DeviceId, connectionStatus: ConnectionResolution, status: NearbyStatus) -> Unit,
        onPayloadReceivedCallback: (endPointId: DeviceId, payload: Payload) -> Unit
    ) : super(
        context = context,
    ) {
        this.onEndpointAddCallback = onEndpointAddCallback
        this.onEndpointRemoveCallback = onEndpointRemoveCallback
        this.onConnectionInitiatedCallback = onConnectionInitiatedCallback
        this.onDisconnectedCallback = onDisconnectedCallback
        this.onConnectionResultCallback = onConnectionResultCallback
        this.onPayloadReceivedCallback = onPayloadReceivedCallback
    }


    private fun createEndpointLifecycleCallback() = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            logE("Found Endpoint: $endpointId ${info.endpointName}")
            onEndpointAddCallback(Pair(DeviceId(endpointId), info))
        }

        override fun onEndpointLost(endpointId: String) {
            logE("LOST Endpoint: $endpointId")
            onEndpointRemoveCallback(DeviceId(endpointId))
        }
    }

    override fun start(callback: (text: String, status: NearbyStatus) -> Unit) {
        if (status == NearbyStatus.ADVERTISING || status == NearbyStatus.DISCOVERING) {
            callback("Already advertising or discovering", status)
            return
        }
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build()
        payloadCallback = createPayloadCallback(onPayloadReceivedCallback)
        endpointDiscoveryCallback = createEndpointLifecycleCallback()

        connectionsClient.startDiscovery(
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

    override fun stop(callback: (text: String, status: NearbyStatus) -> Unit) {
        if (status == NearbyStatus.DISCOVERING) {
            Nearby.getConnectionsClient(context).stopDiscovery()
            status = NearbyStatus.STOPPED
            callback("Discovery stopped", status)
        }
    }


}