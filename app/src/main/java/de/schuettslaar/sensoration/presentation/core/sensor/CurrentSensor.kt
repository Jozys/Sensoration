package de.schuettslaar.sensoration.presentation.core.sensor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.core.icon.SensorIcon

@Composable
fun CurrentSensor(
    sensorType: SensorType?, shouldDisableSensorChange: Boolean = false,
    onSensorChange: (SensorType) -> Unit = {},
    noSensorSelectedDescription: Int = R.string.sensor_type_unknown_description,
    trailingContent: @Composable () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Text(
            stringResource(R.string.current_sensor),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )
        SensorView(
            selectedSensorType = sensorType,
            sensorTypes = SensorType.entries,
            onSensorTypeSelected = {
                onSensorChange(it)
            },
            disabled = shouldDisableSensorChange,
            modifier = Modifier.padding(8.dp),
            content = {
                ListItem(
                    leadingContent = {

                        SensorIcon(
                            sensorType = sensorType,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .padding(4.dp)
                        )

                    },
                    headlineContent = {
                        Text(
                            text = stringResource(
                                id = sensorType?.displayNameId
                                    ?: R.string.sensor_type_unknown
                            ),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    supportingContent = {
                        Text(
                            text = stringResource(
                                id = sensorType?.descriptionId
                                    ?: noSensorSelectedDescription
                            ),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    trailingContent = {
                        trailingContent()

                    }
                )
            },
        )
    }
}