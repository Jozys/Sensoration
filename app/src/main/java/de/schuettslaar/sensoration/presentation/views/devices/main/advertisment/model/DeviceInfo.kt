package de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model

import de.schuettslaar.sensoration.domain.ApplicationStatus

/**
 * Representation of a device's information.
 * */
data class DeviceInfo(
    val deviceName: String,
    var applicationStatus: ApplicationStatus
)