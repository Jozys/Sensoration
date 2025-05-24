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
import de.schuettslaar.sensoration.application.data.WrappedSensorData
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

val MASTER_NAME = DeviceId("MASTER")

class Master : Device {
    private val rawSensorDataMap = mutableMapOf<DeviceId, CircularFifoQueue<WrappedSensorData>>()

    // Optional the master can also provide sensor data
    private val sensorManager: SensorManager
    private var masterDeviceProvidesData: Boolean = true
    private var sensorJob: Job? = null
    private val ptpHandler = MasterPTPHandler()

    // For periodic sending
    private var ptpJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private var sensorType: SensorType? = null

    constructor(
        context: Context,
        onConnectionResultCallback: (DeviceId, ConnectionResolution, NearbyStatus) -> Unit,
        onDisconnectedCallback: (DeviceId, NearbyStatus) -> Unit,
        onConnectionInitiatedCallback: (DeviceId, ConnectionInfo) -> Unit,
    ) : super() {
        this.ownDeviceId = MASTER_NAME
        this.isMaster = true
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

        when (message.messageType) {
            MessageType.SENSOR_DATA -> processSensorData(message as WrappedSensorData, endpointId)
            MessageType.PTP_MESSAGE -> processPTPMessage(message as PTPMessage, endpointId)
            MessageType.TEST_MESSAGE -> handleTestMessage(message as TestMessage)

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

        if (masterDeviceProvidesData) {
            startSensorCollectionOnMaster(sensorType, sensorType.processingDelay)

        }


    }

    fun stopMeasurement() {
        val stopMeasurementMessage = StopMeasurementMessage(
            messageTimeStamp = System.currentTimeMillis().toLong(),
            senderDeviceId = ownDeviceId!!,
            state = ApplicationStatus.DESTINATION,
        )
        broadcastMessage(stopMeasurementMessage)

        if (masterDeviceProvidesData) {
            stopSensorCollectionOnMaster()
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

    private fun startSensorCollectionOnMaster(sensorType: SensorType, intervalMs: Long = 100) {
        if (!masterDeviceProvidesData) {
            Log.d(this.javaClass.simpleName, "Master Device does not provide sensor data")
            return
        }

        if (!sensorManager.checkDeviceSupportsSensorType(sensorType.sensorId)) {
            Log.d(this.javaClass.simpleName, "Master Device does not has sensor type: $sensorType")
            return
        }

        sensorManager.registerSensor(sensorType.sensorId, sensorType.clientDataProcessing)
        sensorManager.startListening()

        sensorJob = coroutineScope.launch {
            while (isActive) {
                delay(intervalMs)

                val latestSensorData = sensorManager.getLatestSensorData()
                Log.d("SensorCollectionOnMaster-Routine", "Latest sensor data: $latestSensorData")
                if (latestSensorData != null) {

                    val wrappedSensorData = WrappedSensorData(
                        messageTimeStamp = ptpHandler.getAdjustedTime(),
                        ownDeviceId!!,
                        applicationStatus,
                        latestSensorData
                    )
                    processSensorData(wrappedSensorData, MASTER_NAME)

                } else {
                    Log.d("SensorCollectionOnMaster-Routine", "No sensor data available")
                }
            }
        }
    }

    fun stopSensorCollectionOnMaster() {
        if (!masterDeviceProvidesData) {
            Log.d(this.javaClass.simpleName, "Master Device does not provide sensor data")
        }

        sensorManager.stopListening()
        sensorJob?.cancel()
        sensorJob = null

    }

    fun getCurrentMasterTime(): Long {
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

    fun isMasterDeviceProvidesData(): Boolean {
        return masterDeviceProvidesData
    }

    fun setMasterDeviceProvidesData(value: Boolean) {
        if (masterDeviceProvidesData != value) {
            masterDeviceProvidesData = value

            // If we're currently measuring, we need to restart sensor collection
            if (sensorType != null) {
                if (value) {
                    startSensorCollectionOnMaster(sensorType!!, sensorType!!.processingDelay)
                } else {
                    stopSensorCollectionOnMaster()
                }
            }
        }
    }

    private fun handleTestMessage(message: TestMessage) {
        Log.i(this.javaClass.simpleName, "Test message received: ${message.content}")
        val sound = MediaActionSound()
        sound.play(MediaActionSound.START_VIDEO_RECORDING)
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
            "Master",
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