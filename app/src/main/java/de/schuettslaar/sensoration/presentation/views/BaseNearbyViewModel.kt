package de.schuettslaar.sensoration.presentation.views

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.domain.Device
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.exception.UnavailabilityType
import de.schuettslaar.sensoration.domain.sensor.SensorType
import java.util.logging.Logger

abstract class BaseNearbyViewModel(application: Application) : AndroidViewModel(application) {

    var thisDevice by mutableStateOf<Device?>(null)
    var status by mutableStateOf(NearbyStatus.STOPPED)
    var text by mutableStateOf("")
    var connectedDevices by mutableStateOf(mapOf<DeviceId, String>())
    var isLoading by mutableStateOf(false)
    var currentSensorType by mutableStateOf<SensorType?>(null)
    var currentSensorUnavailable = mutableStateOf<Pair<SensorType, UnavailabilityType>?>(null)

    fun callback(text: String, status: NearbyStatus) {
        this.text = text
        this.status = status
    }

    fun start(device: Device) {
        this.thisDevice = device
        if (this.thisDevice != null) {
            Logger.getLogger(this.javaClass.simpleName).info {
                "Starting device service"
            }
            device.start { text, status ->
                callback(text, status)
            }
        } else {
            Logger.getLogger(this.javaClass.simpleName).info {
                "No device selected"
            }
        }
    }

    fun stop() {
        if (thisDevice != null) {
            Logger.getLogger(this.javaClass.simpleName).info {
                "Stopping device service"
            }
            thisDevice!!.stop { text, status ->
                callback(text, status)
            }

        } else {
            Logger.getLogger(this.javaClass.simpleName).info {
                "No device selected"
            }
        }
    }

    fun connect(endpointId: DeviceId) {
        Logger.getLogger(this.javaClass.simpleName).info { "Connecting to $endpointId" }
        thisDevice?.connect(endpointId)
        this.isLoading = true
    }

    fun onConnectionInitiatedCallback(
        endpointId: DeviceId, info: ConnectionInfo
    ) {
        connectedDevices = connectedDevices.plus(Pair(endpointId, info.endpointName))
    }

    fun onConnectionResultCallback(
        endpointId: DeviceId, connectionStatus: ConnectionResolution, status: NearbyStatus
    ) {
        if (connectionStatus.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
            Logger.getLogger(this.javaClass.simpleName).info(
                "Connection successful with endpointId: $endpointId"
            )
        } else {
            if (connectedDevices.containsKey(endpointId)) {
                connectedDevices = connectedDevices.minus(endpointId)
            }
            Logger.getLogger("HomeView").info {
                "Connection failed with status code: ${connectionStatus.status.statusCode}"
            }
        }
        this.isLoading = false
        this.status = status
    }

    fun onDisconnectedCallback(endpointId: DeviceId, status: NearbyStatus) {
        if (connectedDevices.containsKey(endpointId)) {
            connectedDevices = connectedDevices.minus(endpointId)
        }
        this.thisDevice?.removeConnectedDevice(endpointId)
        Logger.getLogger(this.javaClass.simpleName).info(
            "Disconnected from endpointId: $endpointId"
        )
        this.status = status
    }

    override fun onCleared() {
        Logger.getLogger(this.javaClass.simpleName)
            .warning { "Clearing ViewModel and terminating Nearby connection" }

        super.onCleared()
        thisDevice?.cleanUp()
    }
}