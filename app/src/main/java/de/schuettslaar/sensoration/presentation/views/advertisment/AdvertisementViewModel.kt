package de.schuettslaar.sensoration.presentation.views.advertisment

import android.app.Application
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import de.schuettslaar.sensoration.application.data.HandshakeMessage
import de.schuettslaar.sensoration.application.data.StartMeasurementMessage
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.Master
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.views.BaseNearbyViewModel
import java.util.logging.Logger

class AdvertisementViewModel(application: Application) : BaseNearbyViewModel(application) {

    init {
        Logger.getLogger(this.javaClass.simpleName).info("Starting AdvertisementViewModel")
        this.thisDevice = Master(
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
                    val master = this.thisDevice
                    var handshakeMessage = HandshakeMessage(
                        messageTimeStamp = System.currentTimeMillis().toLong(),
                        senderDeviceId = master?.ownDeviceId.toString(),
                        state = ApplicationStatus.DESTINATION,
                        clientId = endpointId,
                    )
                    try {
                        master?.sendMessage(endpointId, handshakeMessage)
                    } catch (_: Exception) {
                        Logger.getLogger(this.javaClass.simpleName)
                            .info { "Failed to send handshake message" }
                    }
                    master?.addConnectedDevice(
                        endpointId
                    )

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

    fun disconnect(endpointId: String) {
        Logger.getLogger(this.javaClass.simpleName).info { "Disconnecting from $endpointId" }
        thisDevice?.disconnect(endpointId)
    }


    fun startDebugMeasurement() {
        Logger.getLogger(this.javaClass.simpleName).info { "Starting debug measurement" }
        //TODO: rem debug implementation
        var startMeasurementMessage = StartMeasurementMessage(
            messageTimeStamp = System.currentTimeMillis().toLong(),
            senderDeviceId = this.thisDevice?.ownDeviceId.toString(),
            state = ApplicationStatus.DESTINATION,
            sensorType = SensorType.ACCELEROMETER
        )

        val master = this.thisDevice as? Master
        if (master == null) {
            Logger.getLogger(this.javaClass.simpleName).info { "Master is null" }
            return
        }
        master.broadcastMessage(startMeasurementMessage)
    }

}