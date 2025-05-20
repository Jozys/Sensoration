package de.schuettslaar.sensoration.presentation.views.advertisment

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import de.schuettslaar.sensoration.application.data.HandshakeMessage
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.Master
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.presentation.views.BaseNearbyViewModel
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.apache.commons.collections4.queue.CircularFifoQueue
import java.util.logging.Logger

private const val PROCESSED_VALUES_CAPACITY = 100

class MasterViewModel(application: Application) : BaseNearbyViewModel(application) {
    // processing data to match the sensor type and the correct time
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val cookedSensorDataMap =
        mutableStateMapOf<String, CircularFifoQueue<ProcessedSensorData>>()
    private var cookingDataJob: Job? = null


    val isDrawerOpen = mutableStateOf(false)

    var connectedDeviceInfos by mutableStateOf(mapOf<String, DeviceInfo>())

    var isReceiving by mutableStateOf(false)

    private val logger = Logger.getLogger(this.javaClass.simpleName)

    init {
        logger.info("Starting AdvertisementViewModel")
        this.thisDevice = Master(
            application,
            onConnectionInitiatedCallback = { endpointId, connectionInfo ->
                this.setConnectedDeviceInfo(
                    endpointId,
                    DeviceInfo(
                        deviceName = connectionInfo.endpointName,
                        sensorData = listOf(),
                        ApplicationStatus.INIT,
                    )
                )
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
                    // We need to update the application status here
                    var deviceInfo = this.connectedDeviceInfos.getValue(endpointId)
                    deviceInfo.applicationStatus = ApplicationStatus.IDLE
                    this.setConnectedDeviceInfo(endpointId, deviceInfo)

                    try {
                        master?.sendMessage(endpointId, handshakeMessage)
                    } catch (_: Exception) {
                        logger
                            .info { "Failed to send handshake message" }
                    }
                    master?.addConnectedDevice(
                        endpointId
                    )

                } else {
                    logger.info { "Connection failed" }
                }
            },
            onDisconnectedCallback = { endpointId, status ->
                this.connectedDeviceInfos = this.connectedDeviceInfos.minus(endpointId)
                this.onDisconnectedCallback(endpointId, status)

                if (connectedDeviceInfos.isEmpty()) {
                    this.stopReceiving()
                    isReceiving = false
                }
            }
        )
        this.thisDevice?.start { text, status ->
            this.callback(text, status)
        }
    }

    fun startReceiving() {
        Logger.getLogger(this.javaClass.simpleName).info { "Starting receiving" }
        val master = this.thisDevice as? Master
        if (master == null) {
            Logger.getLogger(this.javaClass.simpleName).info { "Master is null" }
            return
        }

        if (currentSensorType == null) {
            Logger.getLogger(this.javaClass.simpleName).info { "Sensor type is null" }
            return
        }
        master.clearSensorData()
        cookedSensorDataMap.clear()
        master.startMeasurement(currentSensorType!!)

        isReceiving = true

        // TODO: maybe adjust this processing delay
        val sensorTimeResolution: Long = currentSensorType!!.processingDelay //* 2

        suspend fun CoroutineScope.cookingSensorData() {
            // Needs time to retrieve the sensor data from all clients
            val proccessingDelay = sensorTimeResolution * 2
            delay(proccessingDelay)
            while (isActive) {
                val sensorTimeResolution: Long = sensorTimeResolution
                delay(sensorTimeResolution)

                val currentTime: Long =
                    ((master.getCurrentMasterTime() - proccessingDelay) / 10) * 10 // floor to 10ms resolution

                master.connectedDevices.forEach {
                    val sensorData =
                        master.getSensorDataForCurrentTime(
                            currentTime,
                            it,
                            currentSensorType!!,
                            sensorTimeResolution * 2
                        )

                    if (sensorData == null) {
                        Logger.getLogger(this.javaClass.simpleName)
                            .info { "No sensor data available for $it" }
                        return@forEach
                    }


                    var data = cookedSensorDataMap.getOrDefault(
                        it, CircularFifoQueue(
                            PROCESSED_VALUES_CAPACITY
                        )
                    )
                    data.add(sensorData)
                    cookedSensorDataMap[it] = data
                }

                // TODO REMOVE THE FOLLOWING DIRTY CODE
                // Replace the sensor data for the endpointId
                master.connectedDevices.forEach { endpointId ->

                    val sensorData: List<ProcessedSensorData> =
                        cookedSensorDataMap[endpointId]?.toList()
                            ?: emptyList<ProcessedSensorData>()
                    try {
                        var deviceInfo = connectedDeviceInfos.getValue(endpointId)
                        val newDeviceInfo = DeviceInfo(
                            deviceName = deviceInfo.deviceName,
                            sensorData = sensorData,
                            applicationStatus = deviceInfo.applicationStatus,
                        )
                        setConnectedDeviceInfo(endpointId, newDeviceInfo)
                    } catch (e: Exception) {
                        Logger.getLogger(this.javaClass.simpleName)
                            .info { "Failed to add sensor data: ${e.toString()}" }
                    }
                    logger.finer { "cookingSensorData: updated infos for $endpointId" }
                }
                logger.info { "Updated cooked sensor data!" }

            }
        }

        cookingDataJob = coroutineScope.launch {
            cookingSensorData()
        }

    }

    fun stopReceiving() {
        Logger.getLogger(this.javaClass.simpleName).info { "Stopping receiving" }
        val master = this.thisDevice as? Master
        if (master == null) {
            Logger.getLogger(this.javaClass.simpleName).info { "Master is null" }
            return
        }
        master.stopMeasurement()
        isReceiving = false
    }

    fun disconnect(endpointId: String) {
        Logger.getLogger(this.javaClass.simpleName).info { "Disconnecting from $endpointId" }
        thisDevice?.disconnect(endpointId)
    }

    fun setConnectedDeviceInfo(
        endpointId: String,
        deviceInfo: DeviceInfo
    ) {
        this.connectedDeviceInfos = this.connectedDeviceInfos.plus(
            Pair(endpointId, deviceInfo)
        )
    }

}