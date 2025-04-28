package de.schuettslaar.sensoration.application.data

import de.schuettslaar.sensoration.domain.ApplicationStatus
import java.io.Serializable

class WrappedSensorData(
    time: Int,
    deviceId: String,
    state: ApplicationStatus,
    value: Array<Float>
): Serializable {

}