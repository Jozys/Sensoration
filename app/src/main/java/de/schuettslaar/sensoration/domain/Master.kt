package de.schuettslaar.sensoration.domain

import android.content.Context
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import de.schuettslaar.sensoration.adapter.nearby.AdvertiseNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.domain.sensor.SensorType
import java.util.logging.Logger

class Master : Device {

    private var sensorType: SensorType? = null

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

    fun setSensor(sensor: SensorType) {
        this.sensorType = sensor
        // TODO: This needs to send the updated sensor to all connected devices
    }
}