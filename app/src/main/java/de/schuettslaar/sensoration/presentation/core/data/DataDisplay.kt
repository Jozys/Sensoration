package de.schuettslaar.sensoration.presentation.core.data

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.yml.charts.ui.linechart.model.Line
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.core.data.representations.TableDisplay
import de.schuettslaar.sensoration.presentation.core.data.representations.YChartDisplay
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model.TimeBucket
import de.schuettslaar.sensoration.utils.generateColorBasedOnName

@Composable
fun DataDisplay(
    sensorType: SensorType?,
    devices: Map<DeviceId, DeviceInfo>,
    timeBuckets: List<TimeBucket>,
    activeDevices: List<DeviceId>,
) {
    val viewMode = remember { mutableStateMapOf<Int, Boolean>() }


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
            // Check if we have valid data to display
            if (sensorType == null || timeBuckets.isEmpty()) {
                Log.i(
                    "DataDisplay",
                    "No data available for Type: ${sensorType?.name} or TimeBuckets are empty: ${timeBuckets.size}"
                )
                Text(
                    text = stringResource(R.string.no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
                return@Column
            }

            // Array of a line with a color
            val parsedData: Array<Map<DeviceId, Line>> = DataToLineDataService.parseSensorData(
                sensorType.valueSize,
                timeBuckets,
                activeDevices,
            )
            if (parsedData.isNotEmpty()) {
                for ((index, dataMap) in parsedData.withIndex()) {
                    SensorValueDisplay(viewMode, index, timeBuckets, sensorType, dataMap, devices)
                }
            }
            if (devices.isNotEmpty()) {
                Legend(title = stringResource(R.string.data_display_legend), data = devices)
            }
        }


    }
}

@Composable
private fun SensorValueDisplay(
    viewMode: SnapshotStateMap<Int, Boolean>,
    index: Int,
    timeBuckets: List<TimeBucket>,
    sensorType: SensorType,
    dataMap: Map<DeviceId, Line>,
    devices: Map<DeviceId, DeviceInfo>
) {

    var isExpanded by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        Column {
            // Header with collapse button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { isExpanded = !isExpanded }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(sensorType.measurementInfos[index].valueDescriptionId),
                    style = MaterialTheme.typography.titleSmall
                )

                Icon(
                    imageVector = if (isExpanded)
                        Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded)
                        "Collapse" else "Expand"
                )
            }
            // Collapsible content
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    DisplayToggle(
                        isTableView = viewMode[index] == true,
                        onToggle = { viewMode[index] = it },
                    )

                    if (viewMode[index] == true) {
                        TableDisplay(
                            timeBuckets = timeBuckets,
                            dataValueIndex = index,
                            yAxisUnit = stringResource(sensorType.measurementInfos[index].unitId),
                            devices = devices// Assuming deviceInfo is available
                        )
                    } else {
                        YChartDisplay(
                            data = dataMap,
                            yAxisUnit = stringResource(sensorType.measurementInfos[index].unitId)
                        )
                    }
                }
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

