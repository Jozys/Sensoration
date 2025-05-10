package de.schuettslaar.sensoration.presentation.views.advertisment.model

import de.schuettslaar.sensoration.application.data.WrappedSensorData
import de.schuettslaar.sensoration.domain.ApplicationStatus

/**
 * Representation of a device's information.
 * */
data class DeviceInfo(
    val deviceName: String,
    var sensorData: List<WrappedSensorData>,
    var applicationStatus: ApplicationStatus
)