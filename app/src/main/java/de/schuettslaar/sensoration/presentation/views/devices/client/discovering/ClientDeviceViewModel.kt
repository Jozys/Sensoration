package de.schuettslaar.sensoration.presentation.views.devices.client.discovering

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.ClientDevice
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.presentation.views.BaseNearbyViewModel
import java.util.logging.Logger

class ClientDeviceViewModel(application: Application) : BaseNearbyViewModel(application) {

    val possibleConnections = mutableStateMapOf<DeviceId, DiscoveredEndpointInfo>()

    var thisApplicationStatus by mutableStateOf(ApplicationStatus.IDLE)

    init {
        this.thisDevice = ClientDevice(
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
            },
            onSensorTypeChanged = { sensorType ->
                this.currentSensorType = sensorType
            },
            onApplicationStatusChanged = { applicationStatus ->
                thisApplicationStatus = applicationStatus
            },
        )
        this.thisDevice?.start { text, status ->
            this.callback(text, status)
        }

    }

    fun onEndpointAddCallback(endpointId: DeviceId, info: DiscoveredEndpointInfo) {
        possibleConnections.put(endpointId, info)
    }

    fun onEndpointRemoveCallback(endpointId: DeviceId) {
        Logger.getLogger(this.javaClass.simpleName).info { "Endpoint removed: $endpointId" }
        possibleConnections.remove(endpointId)
    }

    fun disconnect() {
        thisDevice?.disconnect(connectedDevices.keys.first())
        thisDevice?.stop { text, status ->
            this.callback(text, status)
        }
        var clientDevice = thisDevice as? ClientDevice
        clientDevice?.stopSensorCollection()
        clientDevice?.stopPeriodicSending()
        cleanUp()
    }

    fun sendConnectionTestMessage() {
        if (thisDevice == null) {
            Logger.getLogger(this.javaClass.simpleName)
                .warning("No device initialized to send message")
            return
        }
        (thisDevice as ClientDevice).sendTestMessage()
    }

    fun cleanUp() {
        thisDevice?.cleanUp()
//        possibleConnections.clear()
        connectedDevices = mapOf()
        thisApplicationStatus = ApplicationStatus.IDLE
        currentSensorType = null
    }

}