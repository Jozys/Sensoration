package de.schuettslaar.sensoration.application.data

import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import java.io.Serializable

data class WrappedSensorData(
    val messageTimeStamp: Long,
    val deviceId: String,
    val state: ApplicationStatus,
    val sensorData: ProcessedSensorData
): Serializable {

}