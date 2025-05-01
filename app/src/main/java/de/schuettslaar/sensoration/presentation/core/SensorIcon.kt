package de.schuettslaar.sensoration.presentation.core

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LineWeight
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.domain.sensor.SensorType

@Composable()
fun SensorIcon(
    sensorType: SensorType?,
    modifier: Modifier = Modifier,
) {
    Icon(
        getIcon(sensorType),
        contentDescription = null,
        modifier = modifier
            .padding(4.dp),
        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
    )
}

fun getIcon(sensorType: SensorType?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (sensorType) {
        SensorType.GRAVITY -> Icons.Filled.LineWeight
        SensorType.PRESSURE -> Icons.Filled.Compress

        else -> {
            Icons.Default.Info
        }
    }
}