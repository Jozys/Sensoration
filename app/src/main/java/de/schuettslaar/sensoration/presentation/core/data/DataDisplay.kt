package de.schuettslaar.sensoration.presentation.core.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.utils.generateColorBasedOnName

@Composable
fun DataDisplay(
    sensorType: SensorType?,
    data: Map<DeviceId, DeviceInfo>,
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),

        ) {
        Text(
            text = stringResource(R.string.data_display_title),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Array of a line with a color
            var parsedData = DataToLineDataService.parseSensorData(
                data,
                sensorType?.valueSize ?: 1
            )
            if (parsedData.isNotEmpty()) {
                when (sensorType) {
                    SensorType.ACCELEROMETER, SensorType.GRAVITY, SensorType.MIN_MAX_SOUND_AMPLITUDE -> parsedData.map { sensorData ->

                        YChartDisplay(sensorData)
                    }

                    SensorType.PRESSURE, SensorType.SOUND_PRESSURE -> YChartDisplay(
                        parsedData.first()
                    )

                    else -> {
                        // Handle unknown sensor type
                        NoVisualizationAvailable()
                    }

                }
            }
            if (data.isNotEmpty()) {
                Legend(title = stringResource(R.string.data_display_legend), data = data)
            }
        }


    }
}

@Composable
fun NoVisualizationAvailable() {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display a message or UI for unknown sensor type
            // For example:
            Icon(
                Icons.Filled.QuestionMark,
                contentDescription = stringResource(R.string.no_data_available_for_this_sensor_type),
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 0.dp)
            )

            Text(
                textAlign = TextAlign.Center,
                text = stringResource(R.string.no_data_available_for_this_sensor_type)
            )
        }
    }
}

@Composable
fun Legend(
    modifier: Modifier = Modifier,
    title: String,
    data: Map<DeviceId, DeviceInfo>,
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row {
            data.map {
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(16.dp)  // Fixed size for the circle
                        .background(
                            color = generateColorBasedOnName(it.key.name),
                            shape = CircleShape  // This makes it a perfect circle
                        )
                ) {
                    // Empty content
                }
                Text(
                    text = it.value.deviceName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

