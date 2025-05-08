package de.schuettslaar.sensoration.presentation.views

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.domain.Client
import de.schuettslaar.sensoration.domain.Device
import de.schuettslaar.sensoration.domain.sensor.SensorType
import java.util.logging.Logger

abstract class BaseNearbyViewModel(application: Application) : AndroidViewModel(application) {

    var thisDevice by mutableStateOf<Device?>(null)
    var status by mutableStateOf(NearbyStatus.STOPPED)
    var text by mutableStateOf("")
    var connectedDevices by mutableStateOf(mapOf<String, String>())
    var isLoading by mutableStateOf(false)

    var isSending: Boolean = false

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

    fun connect(endpointId: String) {
        Logger.getLogger(this.javaClass.simpleName).info { "Connecting to $endpointId" }
        thisDevice?.connect(endpointId)
        this.isLoading = true
    }

    // TODO: Add data model for sensor data
    fun sendMessage(connectedId: String) {
        val client = thisDevice as? Client
        if (isSending) {
            Log.e("BaseNearbyViewModel", "Stopping sensor collection")
            client?.stopPeriodicSending()
            isSending = false
        } else {
            Log.e("BaseNearbyViewModel", "Starting sensor collection")
            client?.startSensorCollection(
                sensorType = SensorType.ACCELEROMETER
            )
            client?.startPeriodicSending(connectedId)
            isSending = true
        }

    }

    fun onConnectionInitiatedCallback(
        endpointId: String, info: ConnectionInfo
    ) {
        connectedDevices = connectedDevices.plus(Pair(endpointId, info.endpointName))
    }

    fun onConnectionResultCallback(
        endpointId: String, connectionStatus: ConnectionResolution, status: NearbyStatus
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

    fun onDisconnectedCallback(endpointId: String, status: NearbyStatus) {
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