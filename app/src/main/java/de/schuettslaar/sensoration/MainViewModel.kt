package de.schuettslaar.sensoration

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import java.util.logging.Logger

@SuppressLint("MutableCollectionMutableState")
class MainViewModel(private val application: Application) : AndroidViewModel(application) {

    private val nearbyWrapper : NearbyWrapper2 = NearbyWrapper2(
        context = application, onEndpointAddCallback = {

            possibleConnections[it.first] = it.second
            Logger.getLogger(this.javaClass.simpleName).info(possibleConnections.keys.joinToString(" ,"))
        },
        onEndpointRemoveCallback = {
            possibleConnections.remove(it)
        }
    )

    var text by mutableStateOf("")
    var possibleConnections by mutableStateOf(
        mutableMapOf<String, DiscoveredEndpointInfo>()
    )

    fun startDiscovering() {
        nearbyWrapper.startDiscovery {
            text = it
        }
    }

    fun startAdvertising() {
        val bootloaderName = Build.BOOTLOADER
        val name = android.provider.Settings.Global.getString(application.contentResolver,"device_name")
        nearbyWrapper.startAdvertising(name + " " + bootloaderName + Build.BRAND) {
            text = it
        }
    }

}