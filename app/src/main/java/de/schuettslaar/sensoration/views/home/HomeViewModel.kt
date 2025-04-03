package de.schuettslaar.sensoration.views.home

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import de.schuettslaar.sensoration.ApplicationStatus
import de.schuettslaar.sensoration.data.WrappedSensorData
import de.schuettslaar.sensoration.nearby.NearbyStatus
import de.schuettslaar.sensoration.nearby.NearbyWrapper
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.ObjectOutputStream
import java.util.logging.Logger


@SuppressLint("StaticFieldLeak", "MutableCollectionMutableState")
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = (application as Context)

    private val nearbyWrapper: NearbyWrapper = NearbyWrapper(
        context = application, onEndpointAddCallback = {
            possibleConnections = possibleConnections.plus(it);
            Logger.getLogger(this.javaClass.simpleName)
                .info(possibleConnections.keys.joinToString(" ,"))
        },
        onEndpointRemoveCallback = {
            Logger.getLogger(this.javaClass.simpleName).info {
                it
            }
        },
        onConnectionResultCallback = { endpointId, status ->
            Logger.getLogger(this.javaClass.simpleName).info {
                "Connected to $endpointId"
            }
            connectedDevice = possibleConnections[endpointId]
            connectedId = endpointId
            Logger.getLogger(this.javaClass.simpleName).info {
                "Connected to ${connectedDevice?.endpointName} with $status"
            }
            this.status = status
        },
        onDisconnectedCallback = { endpointId, status ->
            Logger.getLogger(this.javaClass.simpleName).info {
                "Disconnected from $endpointId"
            }
            connectedDevice = null
            connectedId = ""
            this.status = status

        }
    )

    var status by mutableStateOf(NearbyStatus.STOPPED)
    var text by mutableStateOf("")
    var possibleConnections by mutableStateOf(
        mapOf<String, DiscoveredEndpointInfo>()
    )
    var connectedId by mutableStateOf("")
    var connectedDevice by mutableStateOf<DiscoveredEndpointInfo?>(null)

    fun startDiscovering() {
        nearbyWrapper.startDiscovery { text, status ->
            this.text = text
            this.status = status
        }
    }

    fun startAdvertising() {
        val bootloaderName = Build.BOOTLOADER
        val name =
            android.provider.Settings.Global.getString(context.contentResolver, "device_name")
        Logger.getLogger(this.javaClass.simpleName).info("Advertising as $name")
        nearbyWrapper.startAdvertising(name + " " + bootloaderName + Build.BRAND) { message, status ->
            text = message
            this.status = status
        }
    }

    fun stopDiscovering() {
        nearbyWrapper.stopDiscovery({ newText, newStatus ->
            text = newText
            status = newStatus
        })
    }

    fun stopAdvertising() {
        nearbyWrapper.stopAdvertising { newText, newStatus ->
            text = newText
            status = newStatus
        }
    }

    fun sendMessage() {
        // TODO: REMOVE MOCK VALUES
        val id: String = connectedId
        val data2: Array<Float> = arrayOf(0.0f)
        val wrappedSensorData = WrappedSensorData(1337, connectedId, ApplicationStatus.MISSING_SENSOR,
            data2
        )

        ByteArrayOutputStream().use { bos ->
            ObjectOutputStream(bos).use { oos ->
                oos.writeObject(wrappedSensorData)
            }

            val dataObjectAsByteArray = bos.toByteArray()
            nearbyWrapper.sendData(
                id,
                DataInputStream(ByteArrayInputStream(dataObjectAsByteArray))
            )
        }
    }

    fun connect(endpointId: String) {
        Logger.getLogger(this.javaClass.simpleName).info { "Connecting to $endpointId" }
        nearbyWrapper.connect(endpointId);
    }

}
