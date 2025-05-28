package de.schuettslaar.sensoration.domain.sensor

import android.hardware.Sensor
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.application.data.ClientDataProcessing
import de.schuettslaar.sensoration.application.data.DecibelFullScaleClientDataProcessing
import de.schuettslaar.sensoration.application.data.MinMaxClientDataProcessing
import de.schuettslaar.sensoration.application.data.PitchDetectionClientDataProcessing
import de.schuettslaar.sensoration.application.data.RawClientDataProcessing

/**
 * A enum class representing different types of sensors used in the application.
 * */
enum class SensorType(
    /**
     * The ID of the sensor, used for identifying the sensor in the Android system.
     */
    val sensorId: Int,
    /**
     * The display name of the sensor, used for displaying in the UI.
     */
    val displayNameId: Int,
    /**
     * The description of the sensor, used for displaying in the UI.
     */
    val descriptionId: Int,
    /**
     * The client data processing strategy used for processing the sensor data.
     */
    val clientDataProcessing: ClientDataProcessing,
    /**
     * The size of the value array, defined by Androids sensor API.
     */
    val valueSize: Int = 1,
    /**
     *  The list of measurement information associated with the sensor.
     */
    val measurementInfos: List<MeasurementInfo> = emptyList(),
    /**
     * The delay in milliseconds between sensor updates.
     */
    val processingDelay: Long = 100L, // Delay in milliseconds
) {
    PRESSURE(
        Sensor.TYPE_PRESSURE,
        R.string.sensor_pressure,
        R.string.sensor_pressure_description,
        RawClientDataProcessing(),
        measurementInfos = listOf(
            MeasurementInfo(
                unitId = R.string.unit_hpa,
                valueDescriptionId = R.string.sensor_pressure_value_description
            )
        )
    ),
    GRAVITY(
        Sensor.TYPE_GRAVITY,
        R.string.sensor_gravity,
        R.string.sensor_gravity_description,
        RawClientDataProcessing(),
        valueSize = 3,
        measurementInfos = listOf(
            MeasurementInfo(
                unitId = R.string.unit_m_s2,
                valueDescriptionId = R.string.sensor_gravity_value_x
            ),
            MeasurementInfo(
                unitId = R.string.unit_m_s2,
                valueDescriptionId = R.string.sensor_gravity_value_y
            ),
            MeasurementInfo(
                unitId = R.string.unit_m_s2,
                valueDescriptionId = R.string.sensor_gravity_value_z
            )
        )
    ),
    ACCELEROMETER(
        Sensor.TYPE_ACCELEROMETER,
        R.string.sensor_accelerometer,
        R.string.sensor_accelerometer_description,
        RawClientDataProcessing(),
        processingDelay = 500L,
        valueSize = 3,
        measurementInfos = listOf(
            MeasurementInfo(
                unitId = R.string.unit_m_s2,
                valueDescriptionId = R.string.sensor_accelerometer_value_x
            ),
            MeasurementInfo(
                unitId = R.string.unit_m_s2,
                valueDescriptionId = R.string.sensor_accelerometer_value_y
            ),
            MeasurementInfo(
                unitId = R.string.unit_m_s2,
                valueDescriptionId = R.string.sensor_accelerometer_value_z
            )
        )
    ),
    MIN_MAX_SOUND_AMPLITUDE(
        -1001, // Custom ID for microphone
        R.string.sensor_microphone,
        R.string.sensor_microphone_description,
        MinMaxClientDataProcessing(),
        processingDelay = 100L,
        valueSize = 2,
        measurementInfos = listOf(
            MeasurementInfo(
                unitId = R.string.unit_decibel,
                valueDescriptionId = R.string.sensor_microphone_value_min
            ),
            MeasurementInfo(
                unitId = R.string.unit_decibel,
                valueDescriptionId = R.string.sensor_microphone_value_max
            )
        )
    ),
    DECIBEL_FULL_SCALE(
        -1000, // Custom ID for microphone
        R.string.sensor_sound_pressure,
        R.string.sensor_sound_pressure_description,
        DecibelFullScaleClientDataProcessing(),
        processingDelay = 100L,
        measurementInfos = listOf(
            MeasurementInfo(
                unitId = R.string.unit_decibel,
                valueDescriptionId = R.string.sensor_sound_pressure_value_description
            )
        )
    ),
    SIGNIFICANT_PITCH(
        -1000, // Custom ID for significant pitch
        R.string.sensor_significant_pitch,
        R.string.sensor_significant_pitch_description,
        PitchDetectionClientDataProcessing(),
        processingDelay = 100L,
        valueSize = 2,
        measurementInfos = listOf(
            MeasurementInfo(
                unitId = R.string.unit_hertz,
                valueDescriptionId = R.string.sensor_significant_pitch_value_description
            ),
            MeasurementInfo(
                unitId = R.string.unit_decibel,
                valueDescriptionId = R.string.sensor_significant_pitch_value_amplitude_description
            )
        )
    )
    ;

    companion object {
        fun fromId(id: Int): SensorType? = SensorType.entries.find { it.sensorId == id }

        fun getDisplayNames(): List<Int> = SensorType.entries.map { it.displayNameId }

        fun getIds(): List<Int> = SensorType.entries.map { it.sensorId }
    }
}