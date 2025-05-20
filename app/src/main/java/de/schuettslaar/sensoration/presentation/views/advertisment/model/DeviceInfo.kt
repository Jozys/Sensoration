package de.schuettslaar.sensoration.presentation.views.advertisment.model

import de.schuettslaar.sensoration.domain.ApplicationStatus
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData

/**
 * Representation of a device's information.
 * */
data class DeviceInfo(
    val deviceName: String,
    var sensorData: List<ProcessedSensorData>,
    var applicationStatus: ApplicationStatus
)