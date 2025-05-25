package de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model

import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData

data class TimeBucket(
    val referenceTime: Long,
    val deviceData: Map<DeviceId, ProcessedSensorData>
)