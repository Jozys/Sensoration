package de.schuettslaar.sensoration.presentation.core.data

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.sensor.SensorType

@Composable
fun DataDisplay(
    sensorType: SensorType?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Sensor Data",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )

        when (sensorType) {
            SensorType.PRESSURE -> PressureDisplay()
            SensorType.ACCELEROMETER -> AccelerometerDisplay()
            SensorType.GRAVITY -> GravityDisplay()

            else -> {
                // Handle unknown sensor type

                NoData()
            }
        }
    }
}

@Composable
fun GravityDisplay() {
    //TODO("Not yet implemented")
    NoData()
}

@Composable
fun AccelerometerDisplay() {
    //TODO("Not yet implemented")
    NoData()
}

@Composable
fun PressureDisplay() {
    //TODO("Not yet implemented")
    NoData()
}

@Composable
fun NoData() {

    Surface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
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

            Text(stringResource(R.string.no_data_available_for_this_sensor_type))
        }
    }
}