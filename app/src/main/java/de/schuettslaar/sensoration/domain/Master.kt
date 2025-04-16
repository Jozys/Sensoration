package de.schuettslaar.sensoration.domain

import android.content.Context
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import de.schuettslaar.sensoration.adapter.nearby.AdvertiseNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import java.util.logging.Logger

class Master : Device {

    constructor(
        context: Context,
        onConnectionResultCallback: (String, ConnectionResolution, NearbyStatus) -> Unit,
        onDisconnectedCallback: (String, NearbyStatus) -> Unit,
        onConnectionInitiatedCallback: (String, ConnectionInfo) -> Unit
    ) : super() {
        this.isMaster = true
        this.wrapper = AdvertiseNearbyWrapper(
            context = context,
            onConnectionResultCallback = onConnectionResultCallback,
            onDisconnectedCallback = onDisconnectedCallback,
            onConnectionInitiatedCallback = onConnectionInitiatedCallback,
            onPayloadReceivedCallback = { endPointId, payload ->
                if (payload != null && payload.asBytes() != null) {
                    messageReceived(endPointId, payload.asBytes()!!)
                } else {
                    Logger.getLogger(this.javaClass.simpleName).info("Payload is null")
                }

            }
        );
    }
}