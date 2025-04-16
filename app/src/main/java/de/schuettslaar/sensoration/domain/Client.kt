package de.schuettslaar.sensoration.domain

import android.content.Context
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.adapter.nearby.DiscoverNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import java.util.logging.Logger

class Client : Device {

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
                    Logger.getLogger(this.javaClass.simpleName).info("Payload is null")
                }
            }
        )
    }


}