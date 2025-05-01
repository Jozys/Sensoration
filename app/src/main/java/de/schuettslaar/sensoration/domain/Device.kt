package de.schuettslaar.sensoration.domain

import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.adapter.nearby.NearbyWrapper
import de.schuettslaar.sensoration.application.data.Message
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.logging.Logger

abstract class Device {
    internal var wrapper: NearbyWrapper? = null
    internal var isMaster = false
    internal var applicationStatus: ApplicationStatus = ApplicationStatus.INIT
    internal var connectedDeviceId: String? = null
    internal var ownDeviceId: String? = null

    internal var connectedDevices = mutableSetOf<String>()

    fun start(callback: (text: String, status: NearbyStatus) -> Unit) {
        wrapper?.start(callback)
    }

    fun stop(callback: (text: String, status: NearbyStatus) -> Unit) {
        wrapper?.stop(callback)
    }

    fun connect(deviceIdToConnect: String) {
        wrapper?.connect(deviceIdToConnect)
        this.connectedDeviceId = deviceIdToConnect
        Logger.getLogger(this.javaClass.simpleName)
            .severe("Device is connected to $deviceIdToConnect")
    }

    open fun disconnect(connectedDeviceId: String) {
        wrapper?.disconnect(connectedDeviceId)
        this.connectedDeviceId = null
    }

    private fun sendData(toEndpointId: String, bytes: ByteArray) {
        wrapper?.sendData(toEndpointId, bytes)
    }

    abstract fun messageReceived(
        endpointId: String,
        payload: ByteArray
    )

    fun sendMessage(
        endpointId: String,
        message: Message
    ) {
        Logger.getLogger(this.javaClass.simpleName).info("Sending message to $endpointId")
        // Serialize and send
        ByteArrayOutputStream().use { bos ->
            ObjectOutputStream(bos).use { oos ->
                oos.writeObject(message)
            }
            val bytes = bos.toByteArray()
            Logger.getLogger(this.javaClass.simpleName)
                .info("Sending message of size ${bytes.size} to $endpointId")

            sendData(endpointId, bytes)
        }
    }


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
            Logger.getLogger(this.javaClass.simpleName)
                .warning("Error while reading message: ${e.message}")
            message = null
        }
        return message
    }

    fun addConnectedDevice(endpointId: String) {
        connectedDevices.add(endpointId)
    }

    fun removeConnectedDevice(endpointId: String) {
        connectedDevices.remove(endpointId)
    }
}
