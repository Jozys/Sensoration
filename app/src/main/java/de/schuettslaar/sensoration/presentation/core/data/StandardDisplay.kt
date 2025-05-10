package de.schuettslaar.sensoration.presentation.core.data

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.line.MultiLineChart
import com.himanshoe.charty.line.model.MultiLineData

@Composable
fun StandardDisplay(
    data: Map<String, MultiLineData>
) {
    if (data.isEmpty()) {
        NoVisualizationAvailable()
        return
    }
    MultiLineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        labelConfig = LabelConfig(
            showXLabel = true,
            showYLabel = true,
            textColor = ChartColor.Solid(color = MaterialTheme.colorScheme.primary),
            xAxisCharCount = 10,
            labelTextStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.labelMedium.fontSize,
            ),
        ),
        data = {
            data.map {
                it.value
            }
        }
    )
}