package de.schuettslaar.sensoration.presentation.views.devices.main.advertisment

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import de.schuettslaar.sensoration.application.data.HandshakeMessage
import de.schuettslaar.sensoration.application.data.TestMessage
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.MainDevice
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.views.BaseNearbyViewModel
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model.TimeBucket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.logging.Logger

private const val PROCESSED_VALUES_CAPACITY = 100

class MainDeviceViewModel(application: Application) : BaseNearbyViewModel(application) {
    // processing data to match the sensor type and the correct time
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    val synchronizedData = mutableStateListOf<TimeBucket>()

    private var dataSynchronizingJob: Job? = null

    val isDrawerOpen = mutableStateOf(false)

    val connectedDeviceInfos = mutableStateMapOf<DeviceId, DeviceInfo>()

    var isReceiving by mutableStateOf(false)

    private val logger = Logger.getLogger(this.javaClass.simpleName)

    // Track whether the master device should provide its own sensor data
    var mainDeviceIsProvidingData by mutableStateOf(true)
        private set

    init {
        logger.info("Starting AdvertisementViewModel")
        this.thisDevice = MainDevice(
            application,
            onConnectionInitiatedCallback = { endpointId, connectionInfo ->
                this.setConnectedDeviceInfo(
                    endpointId,
                    DeviceInfo(
                        deviceName = connectionInfo.endpointName,
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
                    val mainDevice = this.thisDevice
                    var handshakeMessage = HandshakeMessage(
                        messageTimeStamp = System.currentTimeMillis().toLong(),
                        senderDeviceId = mainDevice?.ownDeviceId!!,
                        state = ApplicationStatus.DESTINATION,
                        clientId = endpointId,
                    )
                    // We need to update the application status here
                    var deviceInfo = this.connectedDeviceInfos.getValue(endpointId)
                    deviceInfo.applicationStatus = ApplicationStatus.IDLE
                    this.setConnectedDeviceInfo(endpointId, deviceInfo)

                    try {
                        mainDevice.sendMessage(endpointId, handshakeMessage)
                    } catch (_: Exception) {
                        logger
                            .info { "Failed to send handshake message" }
                    }
                    mainDevice.addConnectedDevice(
                        endpointId
                    )

                } else {
                    logger.info { "Connection failed" }
                }
            },
            onDisconnectedCallback = { endpointId, status ->
                this.connectedDeviceInfos.remove(endpointId)
                this.onDisconnectedCallback(endpointId, status)

                if (connectedDeviceInfos.isEmpty()) {
                    this.stopReceiving()
                    isReceiving = false
                }
            },
            onStatusUpdateCallback = { endpointId, newApplicationStatus ->
                this.updateDeviceInfoStatus(endpointId, newApplicationStatus)
            },
        )
        this.thisDevice?.start { text, status ->
            this.callback(text, status)
        }

        // Initialize masterProvidesData from the Master instance
        (thisDevice as? MainDevice)?.let {
            mainDeviceIsProvidingData = it.isMainDeviceProvidingData()
        }
    }

    fun startReceiving() {
        Logger.getLogger(this.javaClass.simpleName).info { "Starting receiving" }
        val mainDevice = this.thisDevice as? MainDevice
        if (mainDevice == null) {
            Logger.getLogger(this.javaClass.simpleName).info { "Master is null" }
            return
        }

        if (currentSensorType == null) {
            Logger.getLogger(this.javaClass.simpleName).info { "Sensor type is null" }
            return
        }

        mainDevice.clearSensorData()
        synchronizedData.clear()

        mainDevice.startMeasurement(currentSensorType!!)

        isReceiving = true

        // TODO: maybe adjust this processing delay
        val sensorTimeResolution: Long = currentSensorType!!.processingDelay //* 2

        dataSynchronizingJob =
            getJobForCookingData(sensorTimeResolution, mainDevice, currentSensorType)
    }

    fun getActiveDevices(): List<DeviceId> {
        val mainDevice = this.thisDevice as? MainDevice
        if (mainDevice == null) {
            Log.e(this.javaClass.simpleName, "Master is null")
            return emptyList()
        }

        val devices = mainDevice.connectedDevices.toList().toMutableList()
        // If master also provides data, include its device ID
        if (mainDevice.isMainDeviceProvidingData() && mainDevice.ownDeviceId != null) {
            devices.add(mainDevice.ownDeviceId!!)
        }
        return devices.toList()
    }

    private fun getJobForCookingData(
        sensorTimeResolution: Long,
        mainDevice: MainDevice,
        currentSensorType: SensorType?
    ): Job {

        return coroutineScope.launch {
            // Needs time to retrieve the sensor data from all clients
            val processingDelay = sensorTimeResolution
            val activeDevices = getActiveDevices()

            delay(processingDelay * 2)

            while (isActive) {
                val currentBucketTime: Long =
                    ((mainDevice.getCurrentTimeOfMainDevice() - processingDelay) / 10) * 10 // floor to 10ms resolution
                val bucketData = mutableMapOf<DeviceId, ProcessedSensorData>()

                activeDevices.forEach { deviceId ->
                    val sensorData = mainDevice.getSensorDataForCurrentTime(
                        currentBucketTime, deviceId, currentSensorType!!, sensorTimeResolution * 10
                    )

                    if (sensorData != null) {
                        bucketData[deviceId] = sensorData
                    }
                }

                val bucket = TimeBucket(
                    referenceTime = currentBucketTime,
                    deviceData = bucketData
                )

                addTimeBucket(bucket)

                delay(sensorTimeResolution * 2)
                logger.info { "Updated cooked sensor data!" }

            }
        }
    }

    private fun addTimeBucket(newBucket: TimeBucket) {
        synchronizedData.add(newBucket)
        if (synchronizedData.size > PROCESSED_VALUES_CAPACITY) {
            synchronizedData.removeAt(0)
        }
    }

    fun stopReceiving() {
        Logger.getLogger(this.javaClass.simpleName).info { "Stopping receiving" }
        val mainDevice = this.thisDevice as? MainDevice
        if (mainDevice == null) {
            Logger.getLogger(this.javaClass.simpleName).info { "Master is null" }
            return
        }
        mainDevice.stopMeasurement()
        isReceiving = false
        dataSynchronizingJob?.cancel()
    }

    fun disconnect(endpointId: DeviceId) {
        Logger.getLogger(this.javaClass.simpleName).info { "Disconnecting from $endpointId" }
        thisDevice?.disconnect(endpointId)
    }

    fun setConnectedDeviceInfo(
        endpointId: DeviceId,
        deviceInfo: DeviceInfo
    ) {
        this.connectedDeviceInfos.put(endpointId, deviceInfo)
    }

    fun toggleMainDeviceProvidingData() {
        val mainDevice = thisDevice as? MainDevice ?: return
        val newValue = !mainDeviceIsProvidingData
        mainDeviceIsProvidingData = newValue
        mainDevice.setMainDeviceToProvidingData(newValue)

        // If we're currently measuring, restart the measurement with the new setting
        if (isReceiving && currentSensorType != null) {
            stopReceiving()
            startReceiving()
        }
    }

    fun sendTestMessage(deviceId: DeviceId) {
        val mainDevice = thisDevice as? MainDevice ?: return
        val testMessage = TestMessage(
            messageTimeStamp = System.currentTimeMillis(),
            senderDeviceId = mainDevice.ownDeviceId ?: return,
            state = ApplicationStatus.DESTINATION,
            content = "Test message from ${mainDevice.ownDeviceId?.name}"
        )

        try {
            mainDevice.sendMessage(deviceId, testMessage)
            Logger.getLogger(this.javaClass.simpleName).info("Test message sent to $deviceId")
        } catch (e: Exception) {
            Logger.getLogger(this.javaClass.simpleName)
                .warning("Failed to send test message: ${e.message}")
        }
    }

    fun updateDeviceInfoStatus(
        endpointId: DeviceId,
        newApplicationStatus: ApplicationStatus
    ) {
        val existingDeviceInfo = connectedDeviceInfos[endpointId] ?: return
        val updatedDeviceInfo = existingDeviceInfo.copy(applicationStatus = newApplicationStatus)
        // IMPORTANT: Force update by removing and re-adding
        connectedDeviceInfos.remove(endpointId) // DO NOT REMOVE THIS LINE
        connectedDeviceInfos[endpointId] = updatedDeviceInfo
    }

}


