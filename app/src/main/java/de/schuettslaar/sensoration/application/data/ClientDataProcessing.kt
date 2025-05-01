package de.schuettslaar.sensoration.application.data

import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.sensor.RawSensorData
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData

interface ClientDataProcessing {
    fun processData(rawData: RawSensorData, applicationStatus: ApplicationStatus, deviceId: String): ProcessedSensorData
}

class RawClientDataProcessing : ClientDataProcessing {
    override fun processData(rawData: RawSensorData, applicationStatus: ApplicationStatus, deviceId: String): ProcessedSensorData =
        ProcessedSensorData(
            timestamp = rawData.timestamp,
            deviceId = deviceId,
            appStatus = applicationStatus,
            sensorType = rawData.sensorType,
            processingStatus = "raw",
            value = rawData.value.toTypedArray()
        )
}

// open for other processing techniques such as DFT
