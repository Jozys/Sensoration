package de.schuettslaar.sensoration.domain

import android.content.Context
import android.media.MediaActionSound
import android.util.Log
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionResolution
import de.schuettslaar.sensoration.adapter.nearby.AdvertiseNearbyWrapper
import de.schuettslaar.sensoration.adapter.nearby.NearbyStatus
import de.schuettslaar.sensoration.application.data.Message
import de.schuettslaar.sensoration.application.data.MessageType
import de.schuettslaar.sensoration.application.data.PTPMessage
import de.schuettslaar.sensoration.application.data.StartMeasurementMessage
import de.schuettslaar.sensoration.application.data.StopMeasurementMessage
import de.schuettslaar.sensoration.application.data.TestMessage
import de.schuettslaar.sensoration.application.data.UnavailableSensorMessage
import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.exception.MissingPermissionException
import de.schuettslaar.sensoration.domain.exception.SensorUnavailableException
import de.schuettslaar.sensoration.domain.exception.UnavailabilityType
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.SensorManager
import de.schuettslaar.sensoration.domain.sensor.SensorType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.Logger
import kotlin.math.abs

private const val RAW_BUFFER_VALUES_CAPACITY = 10

val MAIN_DEVICE_ID = DeviceId("MASTER")

/**
 * MainDevice class represents the data collecting device
 * It is responsible for administrate all connected devices,
 * collecting from all clients and processing sensor data
 * As well as providing the PTP synchronization
 *
 */
class MainDevice : Device {
    private val rawSensorDataMap = mutableMapOf<DeviceId, CircularFifoQueue<WrappedSensorData>>()

    // Optional the master can also provide sensor data
    private val sensorManager: SensorManager
    private var isMainProvidingSensorData: Boolean = true
    private var sensorJob: Job? = null
    private val ptpHandler = MainDevicePTPHandler()

    // For periodic sending
    private var ptpJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var sensorType: SensorType? = null

    // Update the internal status representation of a connected device
    private val onStatusUpdateCallback: (DeviceId, ApplicationStatus) -> Unit

    constructor(
        context: Context,
        onConnectionResultCallback: (DeviceId, ConnectionResolution, NearbyStatus) -> Unit,
        onDisconnectedCallback: (DeviceId, NearbyStatus) -> Unit,
        onConnectionInitiatedCallback: (DeviceId, ConnectionInfo) -> Unit,
        onStatusUpdateCallback: (DeviceId, ApplicationStatus) -> Unit
    ) : super() {
        this.ownDeviceId = MAIN_DEVICE_ID
        this.isMainDevice = true
        this.wrapper = AdvertiseNearbyWrapper(
            context = context,
            onConnectionResultCallback = onConnectionResultCallback,
            onDisconnectedCallback = onDisconnectedCallback,
            onConnectionInitiatedCallback = onConnectionInitiatedCallback,
            onPayloadReceivedCallback = { endPointId, payload ->
                if (payload != null && payload.asBytes() != null) {
                    messageReceived(endPointId, payload.asBytes()!!)
                } else {
                    Logger.getLogger(this.javaClass.simpleName).info("Payload is null")
                }

            })
        sensorManager = SensorManager(context, ptpHandler)
        this.onStatusUpdateCallback = onStatusUpdateCallback
    }

    override fun cleanUp() {
        if (connectedDeviceId == null) {
            Logger.getLogger(this.javaClass.simpleName)
                .warning("Disconnect called but no device is connected")
            return
        }

        stopMeasurement()
        sensorManager.cleanup()

        connectedDevices.forEach {
            wrapper?.disconnect(it)
        }
        wrapper?.stop { text, status ->
            Logger.getLogger(this.javaClass.simpleName)
                .info("Disconnected from all devices")
        }
    }

    override fun messageReceived(endpointId: DeviceId, payload: ByteArray) {
        val message: Message? = parseMessage(endpointId, payload)
        if (message == null) {
            Logger.getLogger(this.javaClass.simpleName).warning("Message is null")
            return
        }

        onStatusUpdateCallback(
            message.senderDeviceId,
            message.state
        )

        when (message.messageType) {
            MessageType.SENSOR_DATA -> processSensorData(message as WrappedSensorData, endpointId)
            MessageType.PTP_MESSAGE -> processPTPMessage(message as PTPMessage, endpointId)
            MessageType.TEST_MESSAGE -> handleTestMessage(message as TestMessage)
            MessageType.UNAVAILABLE_SENSOR -> handleUnavailableSensorMessage(
                message as UnavailableSensorMessage
            )

            else -> {
                Logger.getLogger(this.javaClass.simpleName).warning("Unknown message type received")
            }
        }
    }

    fun clearSensorData() {
        rawSensorDataMap.clear()
    }

    private fun processSensorData(
        sensorData: WrappedSensorData,
        endpointId: DeviceId
    ) {
        Logger.getLogger(this.javaClass.simpleName)
            .info("Sensor data received from ${sensorData.senderDeviceId} > ${sensorData.sensorData}")
        var data = rawSensorDataMap.getOrDefault(
            endpointId, CircularFifoQueue(
                RAW_BUFFER_VALUES_CAPACITY
            )
        )
        data.add(sensorData)
        rawSensorDataMap[endpointId] = data
    }

    private fun getCurrentRawData(endpointId: DeviceId): List<WrappedSensorData> =
        rawSensorDataMap[endpointId]?.toList() ?: emptyList()

    fun broadcastMessage(message: Message) {
        connectedDevices.forEach { deviceId ->
            sendMessage(deviceId, message)
        }
        Logger.getLogger(this.javaClass.simpleName)
            .info("Broadcasted message $message to ${connectedDevices.size} devices: $connectedDevices")
    }

    @Throws(SensorUnavailableException::class)
    fun startMeasurement(sensorType: SensorType) {
        var startMeasurementMessage = StartMeasurementMessage(
            messageTimeStamp = System.currentTimeMillis().toLong(),
            senderDeviceId = ownDeviceId!!,
            state = ApplicationStatus.DESTINATION,
            sensorType = sensorType,
            delay = sensorType.processingDelay
        )
        startPTP(1000)
        broadcastMessage(startMeasurementMessage)

        if (isMainProvidingSensorData) {
            startSensorCollectionOnMainDevice(sensorType, sensorType.processingDelay)
        }


    }

    fun stopMeasurement() {
        val stopMeasurementMessage = StopMeasurementMessage(
            messageTimeStamp = System.currentTimeMillis().toLong(),
            senderDeviceId = ownDeviceId!!,
            state = ApplicationStatus.DESTINATION,
        )
        broadcastMessage(stopMeasurementMessage)

        if (isMainProvidingSensorData) {
            stopSensorCollectionOnMainDevice()
        }
    }

    fun setSensor(sensor: SensorType) {
        this.sensorType = sensor
        // TODO: This needs to send the updated sensor to all connected devices
    }

    fun startPTP(intervalMs: Long = 1000) {
        ptpJob = coroutineScope.launch {
            while (isActive) {
                delay(intervalMs)
                ptpSync()
            }
        }
    }

    private fun ptpSync() {
        var t1 = System.currentTimeMillis().toLong()

        var ptpSync = PTPMessage(
            messageTimeStamp = t1,
            senderDeviceId = ownDeviceId!!,
            state = ApplicationStatus.DESTINATION,
            ptpType = PTPMessage.PTPMessageType.SYNC,
        )
        broadcastMessage(ptpSync)

        var ptpFollowup = PTPMessage(
            messageTimeStamp = t1,
            senderDeviceId = ownDeviceId!!,
            state = ApplicationStatus.DESTINATION,
            ptpType = PTPMessage.PTPMessageType.FOLLOW_UP,
        )
        broadcastMessage(ptpFollowup)
    }

    private fun processPTPMessage(
        ptpMessage: PTPMessage,
        endPointId: DeviceId
    ) {
        Logger.getLogger(this.javaClass.simpleName)
            .info("PTP message received from ${ptpMessage.senderDeviceId} > ${ptpMessage.ptpType}")
        if (ptpMessage.ptpType == PTPMessage.PTPMessageType.DELAY_REQUEST) {
            var t4 = System.currentTimeMillis().toLong()
            var ptpDelayResponse = PTPMessage(
                messageTimeStamp = t4,
                senderDeviceId = ownDeviceId!!,
                state = ApplicationStatus.DESTINATION,
                ptpType = PTPMessage.PTPMessageType.DELAY_RESPONSE,
            )
            sendMessage(endPointId, ptpDelayResponse)
        } else {
            Logger.getLogger(this.javaClass.simpleName)
                .info("PTP message type not supported")
        }
    }

    @Throws(SensorUnavailableException::class)
    private fun startSensorCollectionOnMainDevice(sensorType: SensorType, intervalMs: Long = 100) {
        if (!isMainProvidingSensorData) {
            Log.d(this.javaClass.simpleName, "Main Device does not provide sensor data")
            return
        }

        if (!sensorManager.checkDeviceSupportsSensorType(sensorType.sensorId)) {
            Log.d(this.javaClass.simpleName, "Main Device does not has sensor type: $sensorType")
            throw SensorUnavailableException(sensorType)
        }

        sensorManager.registerSensor(sensorType.sensorId, sensorType.clientDataProcessing)
        try {
            sensorManager.startListening()
        } catch (e: MissingPermissionException) {
            Log.e(
                this.javaClass.simpleName,
                "Missing permission for sensor: ${sensorType.sensorId}",
                e
            )
            throw SensorUnavailableException(
                sensorType,
                UnavailabilityType.SENSOR_PERMISSION_DENIED
            )
        }

        sensorJob = coroutineScope.launch {
            while (isActive) {
                delay(intervalMs)

                val latestSensorData = sensorManager.getLatestSensorData()
                Log.d(
                    "SensorCollectionOnMainDevice-Routine",
                    "Latest sensor data: $latestSensorData"
                )
                if (latestSensorData != null) {

                    val wrappedSensorData = WrappedSensorData(
                        messageTimeStamp = ptpHandler.getAdjustedTime(),
                        ownDeviceId!!,
                        applicationStatus,
                        latestSensorData
                    )
                    processSensorData(wrappedSensorData, MAIN_DEVICE_ID)

                } else {
                    Log.d("SensorCollectionOnMainDevice-Routine", "No sensor data available")
                }
            }
        }
    }

    fun stopSensorCollectionOnMainDevice() {
        if (!isMainProvidingSensorData) {
            Log.d(this.javaClass.simpleName, "Main device does not provide sensor data")
        }

        sensorManager.stopListening()
        sensorJob?.cancel()
        sensorJob = null

    }

    fun getCurrentTimeOfMainDevice(): Long {
        return ptpHandler.getAdjustedTime()
    }

    fun getSensorDataForCurrentTime(
        currentTime: Long,
        endpointId: DeviceId,
        sensorType: SensorType,
        maxTimeThreshold: Long
    ): ProcessedSensorData? {
        val sensorDataList: Collection<WrappedSensorData> = rawSensorDataMap[endpointId]
            ?: emptyList<WrappedSensorData>() // Handle the null case appropriately

        val closestData =
            getClosestSensorData(currentTime, sensorDataList, sensorType, maxTimeThreshold)

        if (closestData != null) {
            Logger.getLogger(this.javaClass.simpleName)
                .info { "Cooking SensorDataForCurrentTime: delta=${abs(closestData.timestamp - currentTime)} endpoint=${endpointId} closedData=${closestData} threshold=${maxTimeThreshold}" }
        }

        return closestData
    }

    fun isMainDeviceProvidingData(): Boolean {
        return isMainProvidingSensorData
    }

    fun setMainDeviceToProvidingData(enable: Boolean) {
        if (isMainProvidingSensorData != enable) {
            isMainProvidingSensorData = enable

            // If we're currently measuring, we need to restart sensor collection
            if (sensorType != null) {
                if (enable) {
                    startSensorCollectionOnMainDevice(sensorType!!, sensorType!!.processingDelay)
                } else {
                    stopSensorCollectionOnMainDevice()
                }
            }
        }
    }

    /**
     * Handles a test message by logging its content and playing a sound.
     * This is used for identifying and locating the main device
     *
     * @param message The test message received.
     */
    private fun handleTestMessage(message: TestMessage) {
        Log.i(this.javaClass.simpleName, "Test message received: ${message.content}")
        val sound = MediaActionSound()
        sound.play(MediaActionSound.START_VIDEO_RECORDING)
    }

    /**
     * Handles an unavailable sensor message by logging the sensor type and updating the status.
     *
     * @param message The unavailable sensor message received.
     */
    private fun handleUnavailableSensorMessage(message: UnavailableSensorMessage) {
        Log.w(this.javaClass.simpleName, "Sensor unavailable: ${message.sensorType}")
        // Update the status of the device to indicate the sensor is unavailable
        onStatusUpdateCallback(message.senderDeviceId, message.state)
    }

}

/**
 * Get the closest data out of the collection compared to the reference time
 */
fun getClosestSensorData(
    referenceTime: Long,
    items: Collection<WrappedSensorData>,
    sensorType: SensorType,
    maxTimeThreshold: Long
): ProcessedSensorData? {
    if (items.isEmpty()) {
        return null
    }

    var selectedCandidate: ProcessedSensorData? = null
    try {
        val candidates = items
            .asSequence()
            .filter { it.state != ApplicationStatus.IDLE }
            .filter { it.sensorData.sensorType == sensorType.sensorId }
            .map { it.sensorData }  // Explicitly filter out nulls
            .toList()

        candidates
            .minByOrNull { abs(it.timestamp - referenceTime) }
            ?.let { closest ->
                // Apply threshold check
                if (abs(closest.timestamp - referenceTime) <= maxTimeThreshold) {
                    Log.d(
                        "SensorData",
                        "Selected closest: ${closest.timestamp}, diff=${abs(closest.timestamp - referenceTime)}"
                    )
                    selectedCandidate = closest
                } else {
                    Log.d(
                        "SensorData",
                        "Closest value exceeded threshold: diff=${abs(closest.timestamp - referenceTime)}, max=$maxTimeThreshold, " +
                                "referenceTime=${formatTimestamp(referenceTime)} " +
                                "-> candidates: ${candidates.map { formatTimestamp(it.timestamp) }}"
                    )
                }
            }
    } catch (e: Exception) {
        Log.e("getClosestSensorData", "Error finding closest sensor data: ${e.message}", e)
        null
    }
    return selectedCandidate
}

/**
 * Returns the item from the collection with the property value closest to the reference value.
 *
 * @param referenceValue The reference value to compare against
 * @param items The collection of items to search
 * @param selector A function that extracts the comparable numeric value from each item
 * @return The closest item, or null if the collection is empty
 */
fun <T> getClosest(referenceValue: Long, items: Collection<T>, selector: (T) -> Long): T? {
    items.forEach {
        Log.d(
            "getClosest",
            "getClosest: abs=${abs(selector(it) - referenceValue)} $referenceValue, ${selector(it)}"
        );
    }
    return items.minByOrNull { abs(selector(it) - referenceValue) }
}

// Helper function to format timestamp
private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return format.format(date)
}