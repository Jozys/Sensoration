package de.schuettslaar.sensoration.adapter.nearby

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy

class AdvertiseNearbyWrapper : NearbyWrapper {

    constructor(
        context: Context,
        onConnectionResultCallback: (endpointId: String, connectionStatus: ConnectionResolution, status: NearbyStatus) -> Unit,
        onDisconnectedCallback: (endpointId: String, status: NearbyStatus) -> Unit,
        onConnectionInitiatedCallback: (endpointId: String, result: ConnectionInfo) -> Unit,
        onPayloadReceivedCallback: (endPointId: String, payload: Payload) -> Unit
    ) : super(context) {
        this.onConnectionResultCallback = onConnectionResultCallback
        this.onDisconnectedCallback = onDisconnectedCallback
        this.onConnectionInitiatedCallback = onConnectionInitiatedCallback
        this.onPayloadReceivedCallback = onPayloadReceivedCallback
    }

    override fun start(
        callback: (text: String, status: NearbyStatus) -> Unit
    ) {
        var localEndpointName =
            android.provider.Settings.Global.getString(context.contentResolver, "device_name")
        if (status == NearbyStatus.ADVERTISING || status == NearbyStatus.DISCOVERING) {
            callback("Already advertising or discovering", status)
            return
        }

        val strategy = Strategy.P2P_STAR
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        payloadCallback = createPayloadCallback(onPayloadReceivedCallback)

        connectionLifecycleCallback = createConnectionLifecycleCallback()

        connectionsClient.startAdvertising(
            localEndpointName, serviceId, connectionLifecycleCallback!!, advertisingOptions
        ).addOnSuccessListener {
            logE("Advertising started")
            status = NearbyStatus.ADVERTISING
            callback("successfully started advertising", status)
        }.addOnFailureListener {
            status = NearbyStatus.STOPPED
            callback("starting advertising failed", status)
        }

    }

    override fun stop(callback: (text: String, status: NearbyStatus) -> Unit) {
        if (status == NearbyStatus.ADVERTISING) {
            connectionsClient.stopAllEndpoints()
            connectionsClient.stopAdvertising()
            status = NearbyStatus.STOPPED
            callback("Advertising stopped", status)
        }
    }


}