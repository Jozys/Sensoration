package de.schuettslaar.sensoration.domain

import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.adapter.nearby.NearbyWrapper
import de.schuettslaar.sensoration.application.data.Message
import de.schuettslaar.sensoration.application.data.MessageType
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.logging.Logger

abstract class Device {
    internal var wrapper: NearbyWrapper? = null
    internal var isMaster = false
    internal var applicationStatus: ApplicationStatus = ApplicationStatus.INIT
    internal var deviceId: String? = null

    fun start(callback: (text: String, status: NearbyStatus) -> Unit) {
        wrapper?.start(callback)
    }

    fun stop(callback: (text: String, status: NearbyStatus) -> Unit) {
        wrapper?.stop(callback)
    }

    fun connect(deviceId: String) {
        wrapper?.connect(deviceId)
        this.deviceId = deviceId
    }

    open fun disconnect(deviceId: String) {
        wrapper?.disconnect(deviceId)
        this.deviceId = null
    }

    fun sendData(toEndpointId: String, stream: DataInputStream) {
        wrapper?.sendData(toEndpointId, stream)
    }

    abstract fun messageReceived(
        endpointId: String,
        payload: ByteArray
    )

    fun parseMessage(
        endpointId: String,
        payload: ByteArray
    ): Message? {
        Logger.getLogger(this.javaClass.simpleName).warning("Message received from $endpointId")
        var message: Message?
        try {
            val inputStream = ByteArrayInputStream(payload)
            val objectInputStream = ObjectInputStream(inputStream)
            message = objectInputStream.readObject() as Message
            objectInputStream.close()
        } catch (e: Exception) {
            Logger.getLogger(this.javaClass.simpleName).warning("Error while reading message: ${e.message}")
            message = null
        }
        return message
    }

}
