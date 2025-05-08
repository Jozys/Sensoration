package de.schuettslaar.sensoration.application.data

import de.schuettslaar.sensoration.domain.sensor.ProcessedSensorData
import de.schuettslaar.sensoration.domain.sensor.RawSensorData
import kotlin.math.log10

interface ClientDataProcessing {
    fun processData(rawData: RawSensorData): ProcessedSensorData
}

class RawClientDataProcessing : ClientDataProcessing {
    override fun processData(rawData: RawSensorData): ProcessedSensorData = ProcessedSensorData(
        timestamp = rawData.timestamp,
        sensorType = rawData.sensorType,
        processingStatus = "raw",
        value = rawData.value.toTypedArray()
    )
}

class MinMaxClientDataProcessing : ClientDataProcessing {
    override fun processData(rawData: RawSensorData): ProcessedSensorData {
        val value: Array<Float> = arrayOf(
            rawData.value.min(),
            rawData.value.max()
        )

        return ProcessedSensorData(
            timestamp = rawData.timestamp,
            sensorType = rawData.sensorType,
            processingStatus = "minmax",
            value = value
        )
    }
}

class SoundPressureClientDataProcessing : ClientDataProcessing {
    override fun processData(rawData: RawSensorData): ProcessedSensorData {

        val maxValue = rawData.value.max()
        val decibelValue = 20 * log10(maxValue / 1)//20e-6f) // Reference pressure in air is 20 µPa

        return ProcessedSensorData(
            timestamp = rawData.timestamp,
            sensorType = rawData.sensorType,
            processingStatus = "sound_pressure",
            value = arrayOf(decibelValue)
        )
    }
}

// open for other processing techniques such as DFT
