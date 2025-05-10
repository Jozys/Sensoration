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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.line.config.LineChartColorConfig
import com.himanshoe.charty.line.model.LineData
import com.himanshoe.charty.line.model.MultiLineData
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.sensor.SensorType
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.utils.generateColorBasedOnName
import kotlin.math.roundToLong

@Composable
fun DataDisplay(
    sensorType: SensorType?,
    data: Map<String, DeviceInfo>,
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

            when (sensorType) {
                SensorType.PRESSURE -> StandardDisplay(
                    parseSensorData(
                        data, primaryColor
                    )
                )

                SensorType.ACCELEROMETER -> AccelerometerDisplay()
                SensorType.GRAVITY -> GravityDisplay()
                SensorType.SOUND_PRESSURE -> StandardDisplay(
                    parseSensorData(data, primaryColor)
                )

                else -> {
                    // Handle unknown sensor type

                    NoVisualizationAvailable()

                }

            }
            if (data.isNotEmpty()) {
                Legend(title = stringResource(R.string.data_display_legend), data = data)
            }
        }


    }
}

@Composable
fun GravityDisplay() {
    NoVisualizationAvailable()
}

@Composable
fun AccelerometerDisplay() {
    //TODO("Not yet implemented")
    NoVisualizationAvailable()
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
    data: Map<String, DeviceInfo>,
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
                            color = generateColorBasedOnName(it.key),
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

/**
 * We need to parse the data from the sensors
 * Therefore we need to put together a list of MultiLineData grouped by the endPointIds
 * */
fun parseSensorData(
    data: Map<String, DeviceInfo>,
    primaryColor: Color,
): Map<String, MultiLineData> {
    var list = mapOf<String, MultiLineData>()

    data.forEach {
        val entry = it.key
        val wrappedSensorData = it.value.sensorData

        var localLineData = mutableListOf<LineData>()
        wrappedSensorData.forEach { sensorDatas ->
            sensorDatas.sensorData.value.forEach { sensorData ->
                if (!sensorData.isNaN()) {
                    // Add the sensor data to the localLineData
                    // We need to round the value to 2 decimal places
                    localLineData = localLineData.plus(
                        LineData(
                            xValue = wrappedSensorData.indexOf(sensorDatas),
                            // Rounde that to 2 decimal places
                            yValue = (sensorData * 100).roundToLong() / 100.0f,
                        )
                    ).toMutableList()
                }
            }
        }

        val deviceColor = generateColorBasedOnName(entry)
        // Create a new MultiLineData object for each entry
        val multiLineData = MultiLineData(
            colorConfig = LineChartColorConfig(
                lineColor = ChartColor.Solid(color = deviceColor),
                axisColor = ChartColor.Solid(color = primaryColor),
                gridLineColor = ChartColor.Solid(color = primaryColor),
                lineFillColor = ChartColor.Solid(color = primaryColor),
                selectionBarColor = ChartColor.Solid(color = primaryColor),
            ),
            data = localLineData
        )
        list = list.plus(entry to multiLineData)

    }

    return list;
}

