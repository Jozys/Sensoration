package de.schuettslaar.sensoration.presentation.core.data

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TableDisplay(
    data: Map<DeviceId, DeviceInfo>,
    dataValueIndex: Int,
    diagramName: String,
    xAxisUnit: String,
    yAxisUnit: String
) {

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(300.dp),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 1.dp
    ) {
        val deviceInfoList = data.values.toList()

        if (deviceInfoList.isEmpty() || deviceInfoList.all { it.sensorData.isEmpty() }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val text = stringResource(R.string.no_data)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            Column {

                Header(diagramName, deviceInfoList)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    val maxDataPoints = deviceInfoList.maxOfOrNull { it.sensorData.size } ?: 0
                    items(maxDataPoints) { timeIndex ->
                        RowItem(deviceInfoList, timeIndex, dataValueIndex)

                    }

                }
            }
        }
    }
}

@Composable
private fun RowItem(
    deviceInfoList: List<DeviceInfo>,
    index: Int,
    dataValueIndex: Int
) {
    Row {
        for (deviceInfo in deviceInfoList) {
            Column(modifier = Modifier.weight(1f)) {
                if (index < deviceInfo.sensorData.size) {
                    val sensorData = deviceInfo.sensorData[index]
                    val timestamp = sensorData.timestamp

                    Text(
                        text = formatTimestamp(timestamp),
                        style = MaterialTheme.typography.bodySmall
                    )

                    if (dataValueIndex < sensorData.value.size) {
                        val sensorValue = sensorData.value[dataValueIndex]
                        Text(
                            text = "%.2f".format(sensorValue),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Log.e(
                            "TableDisplay",
                            "False content in data at $timestamp: Detected because to little data values provided"
                        )
                        Text(text = "N/A")
                    }

                    // No data from this device
                } else {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun Header(
    diagramName: String,
    data: List<DeviceInfo>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        Text(
            text = diagramName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Device columns
        data.forEach { deviceInfo ->
            Text(
                text = deviceInfo.deviceName,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Helper function to format timestamp
private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val format = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    return format.format(date)
}
