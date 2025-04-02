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
import de.schuettslaar.sensoration.nearby.NearbyStatus
import de.schuettslaar.sensoration.nearby.NearbyWrapper
import java.util.logging.Logger

@SuppressLint("StaticFieldLeak", "MutableCollectionMutableState")
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val context: Context = (application as Context)

    private val nearbyWrapper: NearbyWrapper = NearbyWrapper(
        context = application, onEndpointAddCallback = {
            possibleConnections[it.first] = it.second
            Logger.getLogger(this.javaClass.simpleName)
                .info(possibleConnections.keys.joinToString(" ,"))
        },
        onEndpointRemoveCallback = {
            possibleConnections.remove(it)
        }
    )

    var status by mutableStateOf(NearbyStatus.STOPPED)
    var text by mutableStateOf("")
    var possibleConnections by mutableStateOf(
        mutableMapOf<String, DiscoveredEndpointInfo>()
    )

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
        nearbyWrapper.stopAdvertising({ newText, newStatus ->
            text = newText
            status = newStatus
        })
    }

    fun connect(endpointId: String) {
        Logger.getLogger(this.javaClass.simpleName, "Connecting to $endpointId")
    }

}
