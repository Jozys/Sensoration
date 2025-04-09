package de.schuettslaar.sensoration.data

import de.schuettslaar.sensoration.ApplicationStatus
import java.io.Serializable

class WrappedSensorData(
    time: Int,
    deviceId: String,
    state: ApplicationStatus,
    value: Array<Float>
): Serializable {

}