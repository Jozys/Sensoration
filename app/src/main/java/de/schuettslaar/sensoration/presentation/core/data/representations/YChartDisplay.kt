package de.schuettslaar.sensoration.presentation.core.data.representations

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.presentation.core.data.NoVisualizationAvailable

const val AXIS_STEP_SIZE = 5

@Composable
fun YChartDisplay(
    data: Map<DeviceId, Line>, xAxisUnit: String, yAxisUnit: String
) {
    val xAxisLabel = "${stringResource(R.string.index)} $xAxisUnit"
    val yAxisLabel = "in $xAxisUnit"

    if (data.isEmpty()) {
        NoVisualizationAvailable()
        return
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(300.dp),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        val lineChartData = createLineChartData(data, xAxisLabel, yAxisLabel)
        Column {

            Text(
                text = stringResource(R.string.sensor_data_unit),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Text(
                text = yAxisUnit,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )

            LineChart(
                lineChartData = lineChartData, modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )

            Text(
                text = stringResource(R.string.index),
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun createLineChartData(
    data: Map<DeviceId, Line>,
    xAxisLabel: String,
    yAxisLabel: String,
): LineChartData {
    // Find the minimum and maximum values across all data points
    val allDataPoints = data.values.flatMap { it.dataPoints }
    val minValue = allDataPoints.minOfOrNull { it.y } ?: 0f
    val maxValue = allDataPoints.maxOfOrNull { it.y } ?: 1f

    // Calculate the range of values
    val range = maxValue - minValue

    // Choose the formatting function based on the range of values
    val formatFunc: (Float) -> String = when {
        range < 0.1f -> { value -> "%.3f".format(value) }  // Sehr kleine Werte (0.001)
        range < 1f -> { value -> "%.2f".format(value) }    // Kleine Werte (0.01)
        range < 10f -> { value -> "%.1f".format(value) }   // Mittlere Werte (0.1)
        else -> { value -> "%.0f".format(value) }          // Große Werte (1+)
    }

    // Find max X value across all data points
    val maxX = allDataPoints.maxOfOrNull { it.x }?.toInt() ?: 0

    // Ensure we always have at least 10 steps on X-axis
    val xAxisSteps = maxOf(10, maxX + 1)

    return LineChartData(
        linePlotData = LinePlotData(
            lines = data.values.toList(),
        ),
        xAxisData = AxisData.Builder().steps(xAxisSteps).labelData { i ->
            if (i >0 && i % 2 == 0) {
                // TODO: Show the time since the start of the measurement
                i.toString()
            } else {
                ""
            }
        }.axisLabelDescription {
            xAxisLabel
        }.axisLabelColor(MaterialTheme.colorScheme.onSurface).labelAndAxisLinePadding(12.dp)
            .build(),
        yAxisData = AxisData.Builder().steps(AXIS_STEP_SIZE).labelData {
            val stepValue = minValue + (range / AXIS_STEP_SIZE) * it
            formatFunc(stepValue)
        }.axisLabelDescription {
            yAxisLabel
        }.axisLabelColor(MaterialTheme.colorScheme.onSurface).labelAndAxisLinePadding(12.dp)
            .build(),

        backgroundColor = MaterialTheme.colorScheme.surface,
    )
}