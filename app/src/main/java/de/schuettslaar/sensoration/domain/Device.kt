package de.schuettslaar.sensoration.domain

import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.adapter.nearby.NearbyWrapper
import java.io.DataInputStream
import java.util.logging.Logger

abstract class Device {
    internal var wrapper: NearbyWrapper? = null
    internal var isMaster = false
    internal var applicationStatus: ApplicationStatus = ApplicationStatus.INIT

    fun start(callback: (text: String, status: NearbyStatus) -> Unit) {
        wrapper?.start(callback)
    }

    fun stop(callback: (text: String, status: NearbyStatus) -> Unit) {
        wrapper?.stop(callback)
    }

    fun connect(deviceId: String) {
        wrapper?.connect(deviceId)
    }

    fun disconnect(deviceId: String) {
        wrapper?.disconnect(deviceId)
    }

    fun sendData(toEndpointId: String, stream: DataInputStream) {
        wrapper?.sendData(toEndpointId, stream)
    }

    fun messageReceived(
        endpointId: String,
        payload: ByteArray
    ) {
        Logger.getLogger(this.javaClass.simpleName).warning("Message received from $endpointId")
    }

}
