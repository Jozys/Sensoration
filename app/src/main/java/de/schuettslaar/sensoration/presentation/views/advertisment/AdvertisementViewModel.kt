package de.schuettslaar.sensoration.presentation.views.advertisment

import android.app.Application
import de.schuettslaar.sensoration.domain.Master
import de.schuettslaar.sensoration.presentation.views.BaseNearbyViewModel
import java.util.logging.Logger

class AdvertisementViewModel(application: Application) : BaseNearbyViewModel(application) {

    init {
        Logger.getLogger(this.javaClass.simpleName).info("Starting AdvertisementViewModel")
        this.device = Master(
            application,
            onConnectionInitiatedCallback = { endpointId, connectionInfo ->
                this.onConnectionInitiatedCallback(endpointId, connectionInfo)
            },
            onConnectionResultCallback = { endpointId, connectionResolution, status ->
                this.onConnectionResultCallback(endpointId, connectionResolution, status)
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