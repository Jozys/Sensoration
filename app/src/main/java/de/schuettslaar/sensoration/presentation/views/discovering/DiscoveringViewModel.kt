package de.schuettslaar.sensoration.presentation.views.discovering

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.Client
import de.schuettslaar.sensoration.presentation.views.BaseNearbyViewModel
import java.util.logging.Logger

class DiscoveringViewModel(application: Application) : BaseNearbyViewModel(application) {

    var possibleConnections by mutableStateOf(
        mapOf<String, DiscoveredEndpointInfo>()
    )

    init {
        this.thisDevice = Client(
            context = application,
            onEndpointAddCallback = { it ->
                this.onEndpointAddCallback(it.first, it.second)
            },
            onEndpointRemoveCallback = {
                this.onEndpointRemoveCallback(it)
            },
            onConnectionInitiatedCallback = { endpointId, result ->
                this.onConnectionInitiatedCallback(
                    endpointId,
                    result
                )
            },
            onConnectionResultCallback = { endpointId, connectionStatus, status ->
                this.onConnectionResultCallback(
                    endpointId,
                    connectionStatus,
                    status
                )
                if (connectionStatus.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                    this.thisDevice?.applicationStatus = ApplicationStatus.IDLE
                } else {
                    Logger.getLogger(this.javaClass.simpleName).info { "Connection failed" }
                }
            },
            onDisconnectedCallback = { endpointId, status ->
                this.onDisconnectedCallback(endpointId, status)
            }
        )
        this.thisDevice?.start { text, status ->
            this.callback(text, status)
        }
    }

    fun onEndpointAddCallback(endpointId: String, info: DiscoveredEndpointInfo) {
        possibleConnections = possibleConnections.plus(Pair(endpointId, info))
    }

    fun onEndpointRemoveCallback(endpointId: String) {
        Logger.getLogger(this.javaClass.simpleName).info { "Endpoint removed: $endpointId" }
        possibleConnections = possibleConnections.minus(endpointId)
    }

    fun disconnect() {
        thisDevice?.disconnect(connectedDevices.keys.first())
        thisDevice?.stop { text, status ->
            this.callback(text, status)
        }
        connectedDevices = mapOf()
    }

    fun sendMessage() {
        this.sendMessage(connectedDevices.keys.first())
    }

}