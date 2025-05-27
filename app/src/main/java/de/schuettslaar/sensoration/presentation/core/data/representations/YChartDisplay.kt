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
import androidx.compose.ui.unit.Dp
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

    val minX = 0f
    val maxX = data.values.firstOrNull()?.dataPoints?.size?.toFloat() ?: 0f

    return LineChartData(
         linePlotData = LinePlotData(
            lines = data.values.toList(),
        ),
        xAxisData = AxisData.Builder().steps(((maxX- minX) / AXIS_STEP_SIZE).toInt()).labelData { i ->
           value( i, minX, maxX)
        }.axisLabelDescription {
            xAxisLabel
        }.axisLabelColor(MaterialTheme.colorScheme.onSurface).axisStepSize(calculateStepSize(minX, maxX)).labelAndAxisLinePadding(12.dp)
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

/**
 * Should show the x-axis labels in a readable format.
 * This should be dependent on the data range and the number of data points.
 * */
private val value : (Int, Float, Float) -> String = { i,minX, maxX ->
    if (minX == maxX) {
        "0"
    } else {
        val range = maxX - minX
        val divider = range / AXIS_STEP_SIZE
        if (i > 0 && i.toFloat() % divider == 0f) {
            i.toString()
        } else {
            ""
        }
    }
}

/**
 * Calculate the dp size between the minimum and maximum values for the x-axis.
 * The more data points, the smaller the spacing to ensure all points fit.
 */
private val calculateStepSize: (Float, Float) -> Dp = { minX, maxX ->
    val range = maxX - minX
    if (range <= 0) {
        1.dp // Avoid division by zero
    } else {
        // Inversely scale step size with data point count
        // Base value of 100.dp divided by range with a minimum of 2.dp
        val calculatedDp = 200f / range
        maxOf(calculatedDp, 3f).dp
    }
}