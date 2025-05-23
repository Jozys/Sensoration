package de.schuettslaar.sensoration.presentation.views.advertisment

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import de.schuettslaar.sensoration.application.data.HandshakeMessage
import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.Master
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.views.BaseNearbyViewModel
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.logging.Logger

private const val PROCESSED_VALUES_CAPACITY = 100

class MasterViewModel(application: Application) : BaseNearbyViewModel(application) {
    // processing data to match the sensor type and the correct time
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    val synchronizedData = mutableStateListOf<TimeBucket>()

    private var dataSynchronizingJob: Job? = null


    val isDrawerOpen = mutableStateOf(false)

    var connectedDeviceInfos by mutableStateOf(mapOf<DeviceId, DeviceInfo>())

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
                        senderDeviceId = master?.ownDeviceId!!,
                        state = ApplicationStatus.DESTINATION,
                        clientId = endpointId,
                    )
                    // We need to update the application status here
                    var deviceInfo = this.connectedDeviceInfos.getValue(endpointId)
                    deviceInfo.applicationStatus = ApplicationStatus.IDLE
                    this.setConnectedDeviceInfo(endpointId, deviceInfo)

                    try {
                        master.sendMessage(endpointId, handshakeMessage)
                    } catch (_: Exception) {
                        logger
                            .info { "Failed to send handshake message" }
                    }
                    master.addConnectedDevice(
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
        synchronizedData.clear()

        master.startMeasurement(currentSensorType!!)

        isReceiving = true

        // TODO: maybe adjust this processing delay
        val sensorTimeResolution: Long = currentSensorType!!.processingDelay //* 2



        dataSynchronizingJob = getJobForCookingData(sensorTimeResolution, master, currentSensorType)
    }

    private fun getJobForCookingData(
        sensorTimeResolution: Long,
        master: Master,
        currentSensorType: SensorType?
    ): Job {
        fun getActiveDevices(): List<DeviceId> {
            val devices = master.connectedDevices.toList().toMutableList()
            // If master also provides data, include its device ID
            if (master.isMasterDeviceProvidesData() && master.ownDeviceId != null) {
                devices.add(master.ownDeviceId!!)
            }
            return devices.toList()
        }

        return coroutineScope.launch {
            // Needs time to retrieve the sensor data from all clients
            val processingDelay = sensorTimeResolution
            val activeDevices = getActiveDevices()

            delay(processingDelay * 2)

            while (isActive) {
                val currentBucketTime: Long =
                    ((master.getCurrentMasterTime() - processingDelay) / 10) * 10 // floor to 10ms resolution
                val bucketData = mutableMapOf<DeviceId, ProcessedSensorData>()



                activeDevices.forEach { deviceId ->
                    val sensorData = master.getSensorDataForCurrentTime(
                        currentBucketTime, deviceId, currentSensorType!!, sensorTimeResolution * 10
                    )

                    if (sensorData != null) {
                        bucketData.put(
                            deviceId,
                            sensorData
                        )
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
        val master = this.thisDevice as? Master
        if (master == null) {
            Logger.getLogger(this.javaClass.simpleName).info { "Master is null" }
            return
        }
        master.stopMeasurement()
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
        this.connectedDeviceInfos = this.connectedDeviceInfos.plus(
            Pair(endpointId, deviceInfo)
        )
    }

}


data class TimeBucket(
    val referenceTime: Long,
    val deviceData: Map<DeviceId, ProcessedSensorData>
)