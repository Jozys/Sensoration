package de.schuettslaar.sensoration.presentation.core.data

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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

const val AXIS_STEP_SIZE = 5

@Composable
fun YChartDisplay(
    data: Map<String, Line>,
) {
    var xAxisLabel = stringResource(R.string.index)

    if (data.isEmpty()) {
        NoVisualizationAvailable()
        return
    }

    var lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = data.values.toList(),
        ),
        xAxisData = AxisData.Builder().steps(AXIS_STEP_SIZE)
            .labelData { i ->
                if (i > 0) {
                    i.toString()
                } else {
                    ""
                }
            }
            .axisLabelDescription {
                xAxisLabel
            }
            .axisLabelColor(MaterialTheme.colorScheme.onSurface)
            .labelAndAxisLinePadding(12.dp).build(),
        yAxisData = AxisData.Builder()
            .steps(AXIS_STEP_SIZE)
            .labelData {
                val maxValue = data.entries.first().value.dataPoints.maxOf { it.y }
                val minValue = data.entries.first().value.dataPoints.minOf { it.y }

                val scale = (maxValue - minValue) / AXIS_STEP_SIZE
                ((it * scale) + minValue).formatToSinglePrecision()
            }
            .axisLabelDescription {
                "db"
            }
            .axisLabelColor(MaterialTheme.colorScheme.onSurface)
            .labelAndAxisLinePadding(12.dp).build(),

        backgroundColor = MaterialTheme.colorScheme.surface,
    )

    Column() {
        Text(
            text = stringResource(R.string.sensor_data_unit),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )

        LineChart(
            lineChartData = lineChartData,
            modifier = Modifier
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