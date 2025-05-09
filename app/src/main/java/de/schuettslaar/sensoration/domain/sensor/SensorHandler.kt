package de.schuettslaar.sensoration.domain.sensor

import de.schuettslaar.sensoration.application.data.ClientDataProcessing

interface SensorHandler {
    fun initialize(processor: ClientDataProcessing)
    fun startListening()
    fun stopListening()
    fun getLatestData(): ProcessedSensorData?
    fun cleanup()
}