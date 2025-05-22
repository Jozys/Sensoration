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
    internal var connectedDeviceId: DeviceId? = null
    internal var ownDeviceId: DeviceId? = null

    internal var connectedDevices = mutableSetOf<DeviceId>()

    fun start(callback: (text: String, status: NearbyStatus) -> Unit) {
        wrapper?.start(callback)
    }

    fun stop(callback: (text: String, status: NearbyStatus) -> Unit) {
        wrapper?.stop(callback)
    }

    fun connect(deviceIdToConnect: DeviceId) {
        wrapper?.connect(deviceIdToConnect)
        this.connectedDeviceId = deviceIdToConnect
        this.connectedDevices = if(connectedDevices.contains(deviceIdToConnect)) {
            connectedDevices
        } else {
            connectedDevices.apply { add(deviceIdToConnect) }
        }
        Logger.getLogger(this.javaClass.simpleName)
            .severe("Device is connected to $deviceIdToConnect")
    }

    open fun disconnect(connectedDeviceId: DeviceId) {
        wrapper?.disconnect(connectedDeviceId)
        this.connectedDeviceId = null
    }

    abstract fun cleanUp()

    private fun sendData(toEndpointId: DeviceId, bytes: ByteArray) {
        wrapper?.sendData(toEndpointId, bytes)
    }

    abstract fun messageReceived(
        endpointId: DeviceId,
        payload: ByteArray
    )

    fun sendMessage(
        endpointId: DeviceId,
        message: Message
    ) {
        Logger.getLogger(this.javaClass.simpleName).info("Sending message $message to $endpointId")
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
        endpointId: DeviceId,
        payload: ByteArray
    ): Message? {
        Logger.getLogger(this.javaClass.simpleName).info("Message received from $endpointId")
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

    fun addConnectedDevice(endpointId: DeviceId) {
        connectedDevices.add(endpointId)
    }

    fun removeConnectedDevice(endpointId: DeviceId) {
        connectedDevices.remove(endpointId)
    }
}

data class DeviceId(
    val name: String
) : java.io.Serializable {
    init {
        if (name.isEmpty()) {
            throw IllegalArgumentException("DeviceId cannot be empty")
        }
    }
}