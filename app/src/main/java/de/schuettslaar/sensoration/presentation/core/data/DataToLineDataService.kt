package de.schuettslaar.sensoration.presentation.core.data

import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineStyle
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.presentation.views.advertisment.TimeBucket
import de.schuettslaar.sensoration.utils.generateColorBasedOnName
import kotlin.math.roundToLong

class DataToLineDataService {

    companion object {
        /**
         * Parses time-synchronized sensor data into chart-ready format.
         * Creates a separate line for each value dimension of each device.
         */
        fun parseSensorData(
            amountOfValuesPerDevice: Int,
            timeBuckets: Collection<TimeBucket>,
            activeDevices: List<DeviceId>
        ): Array<Map<DeviceId, Line>> {
            if (timeBuckets.isEmpty() || activeDevices.isEmpty()) {
                return arrayOf()
            }

            // Initialize array of maps to hold lines for each value dimension
            val result = Array(amountOfValuesPerDevice) { mapOf<DeviceId, Line>() }

            // Sort time buckets chronologically for proper sequencing
            val sortedBuckets = timeBuckets.sortedBy { it.referenceTime }

            // For each device, extract and plot its data
            for (deviceId in activeDevices) {
                // Initialize point arrays for each value dimension
                val pointArrays = Array(amountOfValuesPerDevice) { mutableListOf<Point>() }

                // Process each time bucket to extract data points
                sortedBuckets.forEachIndexed { index, bucket ->
                    // Get data for this device at this time point (if available)
                    val sensorData = bucket.deviceData[deviceId] ?: return@forEachIndexed

                    // For each value dimension, add a point
                    for (i in 0 until minOf(sensorData.value.size, amountOfValuesPerDevice)) {
                        val value = sensorData.value[i]
                        if (!value.isNaN()) {
                            pointArrays[i].add(
                                Point(
                                    // X-coordinate is either index or normalized timestamp
//                                bucket.referenceTime, // TODO: use time as x-axis
                                    index.toFloat(),
                                    // Round value to 2 decimal places
                                    ((value * 100).roundToLong() / 100.0f)
                                )
                            )
                        }
                    }
                }

                // Create a Line for each value dimension
                val deviceColor = generateColorBasedOnName(deviceId.name)
                for (i in 0 until amountOfValuesPerDevice) {
                    if (pointArrays[i].isNotEmpty()) {
                        val line = Line(
                            dataPoints = pointArrays[i],
                            LineStyle(color = deviceColor)
                        )
                        result[i] = result[i] + (deviceId to line)
                    }
                }
            }

            return result
        }
    }
}