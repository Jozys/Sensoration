package de.schuettslaar.sensoration.presentation.core.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import de.schuettslaar.sensoration.domain.sensor.SensorType

@Composable
fun SensorIcon(
    sensorType: SensorType?,
    modifier: Modifier = Modifier,
) {
    BasicIcon<SensorType>(
        enumValue = sensorType,
        modifier = modifier,
        getIcon = {
            getIcon(sensorType)
        }
    )
}

/**
 * Returns the icon associated with the given [SensorType].
 *
 * @param sensorType The application status for which to get the icon.
 * @return The icon associated with the given application status.
 */
fun getIcon(sensorType: SensorType?): ImageVector {
    return when (sensorType) {
        SensorType.GRAVITY -> Icons.Filled.LineWeight
        SensorType.PRESSURE -> Icons.Filled.Compress
        SensorType.ACCELEROMETER -> Icons.Filled.Speed
        SensorType.SOUND_PRESSURE -> Icons.Filled.Mic
        SensorType.MIN_MAX_SOUND_AMPLITUDE -> Icons.Filled.Mic

        else -> {
            Icons.Default.Info
        }
    }
}