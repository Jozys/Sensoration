package de.schuettslaar.sensoration.views.home

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.Device
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.ObjectOutputStream
import java.util.logging.Logger


@SuppressLint("StaticFieldLeak", "MutableCollectionMutableState")
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    var status by mutableStateOf(NearbyStatus.STOPPED)
    var text by mutableStateOf("")
    var possibleConnections by mutableStateOf(
        mapOf<String, DiscoveredEndpointInfo>()
    )
    var connectedId by mutableStateOf("")
    var connectedDevices by mutableStateOf(mapOf<String, String>())
    var device by mutableStateOf<Device?>(null);


    fun callback(text: String, status: NearbyStatus) {
        this.text = text
        this.status = status
    }

    fun start(device: Device) {
        this.device = device;
        if (device != null) {
            Logger.getLogger(this.javaClass.simpleName).info {
                "Starting device service"
            }
            device.start { text, status ->
                callback(text, status);
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
                callback(text, status);
            }
        } else {
            Logger.getLogger(this.javaClass.simpleName).info {
                "No device selected"
            }
        }
    }

    fun connect(endpointId: String) {
        Logger.getLogger(this.javaClass.simpleName).info { "Connecting to $endpointId" }
        device?.connect(endpointId);
    }

    fun sendMessage() {
        // TODO: REMOVE MOCK VALUES
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


}
