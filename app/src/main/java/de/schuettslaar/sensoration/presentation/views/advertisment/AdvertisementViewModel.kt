package de.schuettslaar.sensoration.presentation.views.advertisment

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import de.schuettslaar.sensoration.application.data.HandshakeMessage
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.Master
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.views.BaseNearbyViewModel
import java.util.logging.Logger

class AdvertisementViewModel(application: Application) : BaseNearbyViewModel(application) {

    val isDrawerOpen = mutableStateOf(false)
    var connectionDevicesStatus by mutableStateOf(
        mapOf<String, ApplicationStatus>()
    )
    var currentSensorType: SensorType? by mutableStateOf(null)

    init {
        Logger.getLogger(this.javaClass.simpleName).info("Starting AdvertisementViewModel")
        this.device = Master(
            application,
            onConnectionInitiatedCallback = { endpointId, connectionInfo ->
                this.onConnectionInitiatedCallback(endpointId, connectionInfo)
            },
            onConnectionResultCallback = { endpointId, connectionStatus, status ->
                this.onConnectionResultCallback(
                    endpointId,
                    connectionStatus, status
                )
                if (connectionStatus.status.statusCode == ConnectionsStatusCodes.STATUS_OK) {
                    // We need to send the id of the device to the client
                    var handshakeMessage = HandshakeMessage(
                        messageTimeStamp = System.currentTimeMillis().toLong(),
                        senderDeviceId = this.device?.ownDeviceId.toString(),
                        state = ApplicationStatus.IDLE,
                        clientId = endpointId,
                    )
                    this.connectionDevicesStatus =
                        this.connectionDevicesStatus.plus(Pair(endpointId, ApplicationStatus.IDLE))

                    try {
                        this.device?.sendMessage(endpointId, handshakeMessage)
                    } catch (_: Exception) {
                        Logger.getLogger(this.javaClass.simpleName)
                            .info { "Failed to send handshake message" }
                    }

                } else {
                    Logger.getLogger(this.javaClass.simpleName).info { "Connection failed" }
                }
            },
            onDisconnectedCallback = { endpointId, status ->
                this.onDisconnectedCallback(endpointId, status)

            }
        )
        this.device?.start { text, status ->
            this.callback(text, status)
        }
    }

    fun disconnect(endpointId: String) {
        Logger.getLogger(this.javaClass.simpleName).info { "Disconnecting from $endpointId" }
        device?.disconnect(endpointId)
    }


}