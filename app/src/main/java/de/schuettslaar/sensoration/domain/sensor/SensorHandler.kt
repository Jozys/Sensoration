package de.schuettslaar.sensoration.domain.sensor

import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import de.schuettslaar.sensoration.domain.exception.MissingPermissionException
import de.schuettslaar.sensoration.domain.exception.SensorUnavailableException
import kotlin.jvm.Throws

interface SensorHandler {
    fun initialize(processor: ClientDataProcessing)
    @Throws(MissingPermissionException::class, SensorUnavailableException::class)
    fun startListening()
    fun stopListening()
    fun getLatestData(): ProcessedSensorData?
    fun cleanup()
    fun checkDeviceSupportsSensorType(sensorType: Int): Boolean
}