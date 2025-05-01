package de.schuettslaar.sensoration.domain.sensor

import de.schuettslaar.sensoration.domain.ApplicationStatus

class ProcessedSensorData(
    val timestamp: Long,
    val deviceId: String,
    val sensorType: Int,
    val appStatus: ApplicationStatus,
    val processingStatus: String,
    val value: Array<Float>
){

}

class RawSensorData(
    val timestamp: Long,
    val sensorType: Int,
    val value: FloatArray
){

}