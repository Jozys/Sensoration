package de.schuettslaar.sensoration.presentation.core.data.representations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import de.schuettslaar.sensoration.presentation.views.devices.main.advertisment.model.TimeBucket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TableDisplay(
    timeBuckets: Collection<TimeBucket>,
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
        if (timeBuckets.isEmpty()) {
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
            // Get all unique device IDs from all buckets
            val allDeviceIds = timeBuckets.flatMap { it.deviceData.keys }.toSet().toList()

            // Sort buckets by reference time
            val sortedBuckets = timeBuckets.sortedBy { it.referenceTime }

            Column {
                // Display header with device names
                Header(diagramName, allDeviceIds)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(sortedBuckets) { bucket ->
                        BucketRow(bucket, allDeviceIds, dataValueIndex)
                    }
                }
            }
        }
    }
}

@Composable
private fun BucketRow(
    bucket: TimeBucket,
    deviceIds: List<DeviceId>,
    dataValueIndex: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Time column
        Text(
            text = formatTimestamp(bucket.referenceTime),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )

        // Data column for each device
        deviceIds.forEach { deviceId ->
            val sensorData = bucket.deviceData[deviceId]

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Value display
                Text(
                    text = if (sensorData != null && dataValueIndex < sensorData.value.size) {
                        "%.2f".format(sensorData.value[dataValueIndex])
                    } else "—",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )

                // Individual timestamp display (smaller text)
                Text(
                    text = if (sensorData != null) formatTimestamp(sensorData.timestamp) else "—",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.8),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun Header(
    diagramName: String,
    deviceIds: List<DeviceId>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        // Time column header
        Text(
            text = diagramName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Device column headers
        deviceIds.forEach { deviceId ->
            Text(
                text = deviceId.name,
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