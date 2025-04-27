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
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.Device
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.ObjectOutputStream
import java.util.logging.Logger

abstract class BaseNearbyViewModel(application: Application) : AndroidViewModel(application) {

    var device by mutableStateOf<Device?>(null)
    var status by mutableStateOf(NearbyStatus.STOPPED)
    var text by mutableStateOf("")
    var connectedDevices by mutableStateOf(mapOf<String, String>())
    var isLoading by mutableStateOf(false)

    fun callback(text: String, status: NearbyStatus) {
        this.text = text
        this.status = status
    }

    fun start(device: Device) {
        this.device = device
        if (this.device != null) {
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
        if (device != null) {
            Logger.getLogger(this.javaClass.simpleName).info {
                "Stopping device service"
            }
            device!!.stop { text, status ->
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
        device?.connect(endpointId)
        this.isLoading = true
    }

    // TODO: Add data model for sensor data
    fun sendMessage(connectedId: String) {
        val id: String = connectedId
        val data2: Array<Float> = arrayOf(0.0f)
        val wrappedSensorData = WrappedSensorData(
            1337, connectedId, ApplicationStatus.ERROR,
            data2
        )

        ByteArrayOutputStream().use { bos ->
            ObjectOutputStream(bos).use { oos ->
                oos.writeObject(wrappedSensorData)
            }

            val dataObjectAsByteArray = bos.toByteArray()
            device?.sendData(
                id,
                DataInputStream(ByteArrayInputStream(dataObjectAsByteArray))
            )
        }
    }

    fun onConnectionInitiatedCallback(
        endpointId: String,
        info: ConnectionInfo
    ) {
        connectedDevices = connectedDevices.plus(Pair(endpointId, info.endpointName))
    }

    fun onConnectionResultCallback(
        endpointId: String,
        connectionStatus: ConnectionResolution,
        status: NearbyStatus
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
        Logger.getLogger(this.javaClass.simpleName).info(
            "Disconnected from endpointId: $endpointId"
        )
        this.status = status
    }

}