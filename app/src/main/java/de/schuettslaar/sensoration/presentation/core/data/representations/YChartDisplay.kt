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
import co.yml.charts.common.extensions.formatToSinglePrecision
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
    return LineChartData(
        linePlotData = LinePlotData(
            lines = data.values.toList(),
        ),
        xAxisData = AxisData.Builder().steps(AXIS_STEP_SIZE).labelData { i ->
            if (i > 0) {
                i.toString()
            } else {
                ""
            }
        }.axisLabelDescription {
            xAxisLabel
        }.axisLabelColor(MaterialTheme.colorScheme.onSurface).labelAndAxisLinePadding(12.dp)
            .build(),
        yAxisData = AxisData.Builder().steps(AXIS_STEP_SIZE).labelData {
            val maxValue = data.entries.first().value.dataPoints.maxOf { it.y }
            val minValue = data.entries.first().value.dataPoints.minOf { it.y }

            val scale = (maxValue - minValue) / AXIS_STEP_SIZE
            ((it * scale) + minValue).formatToSinglePrecision()
        }.axisLabelDescription {
            yAxisLabel
        }.axisLabelColor(MaterialTheme.colorScheme.onSurface).labelAndAxisLinePadding(12.dp)
            .build(),

        backgroundColor = MaterialTheme.colorScheme.surface,
    )
}