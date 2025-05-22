package de.schuettslaar.sensoration.presentation.core.data

import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineStyle
import de.schuettslaar.sensoration.domain.DeviceId
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.utils.generateColorBasedOnName
import java.util.logging.Logger
import kotlin.math.roundToLong

class DataToLineDataService {

    companion object {

        /**
         * We need to parse the data from the sensors
         * Therefore we need to put together a list of MultiLineData grouped by the endPointIds
         *
         *  {List<ProcessedSensorData> -> (Array of data rows for each sensor)} for each client
         * */
        fun parseSensorData(
            data: Map<DeviceId, DeviceInfo>,
            valueSize: Int
        ): Array<Map<DeviceId, Line>> {
            if (data.isEmpty()) {
                return arrayOf()
            }
            // Is there any reason to create this array in here?
            var allSensorValues = Array<Map<DeviceId, Line>>(valueSize) {
                mapOf()
            }

            // We need a list of maps of the Lines
            // That is because the different sensors have different values, all provided in an array
            // But we want to create a graph for each value of them

            for (deviceData in data) {
                val processedSensorData = deviceData.value.sensorData

                if (processedSensorData.isEmpty()) {
                    continue
                }

                // So we iterate over every device
                allSensorValues = parseDeviceEntry(deviceData, allSensorValues)
            }

            return allSensorValues
        }

        /**
         * Parse the sensor data for a single device
         * @return an array of the sensor data in the format of MultiLineData
         * */
        private fun parseDeviceEntry(
            deviceData: Map.Entry<DeviceId, DeviceInfo>,
            initialValues: Array<Map<DeviceId, Line>>,
        ): Array<Map<DeviceId, Line>> {
            val values = initialValues.copyOf()
            // So we iterate over every device
            val entry = deviceData.key
            val processedSensorDataList = deviceData.value.sensorData

            // multiple Datasets for each sensor value
            var pointListArray = Array<List<co.yml.charts.common.model.Point>>(values.size) {
                mutableListOf()
            }
            // So now we need to iterate over the sensor data
            processedSensorDataList.forEach { processedSensorData ->

                // Iterate over each sensor value for one datapoint e.g. x, y, z
                for (dataIndex in 0..processedSensorData.value.size - 1) {
                    val sensorValue = processedSensorData.value[dataIndex]

                    if (sensorValue.isNaN()) {
                        // If the value is null, we skip it
                        continue
                    }

                    // Add the sensor data to the localLineData
                    // We need to round the value to 2 decimal places
                    var valueIndex = processedSensorDataList.indexOf(processedSensorData)
                    try {
                        pointListArray[dataIndex] = pointListArray[dataIndex].plus(
                            Point(
                                // TODO: Change index to actual timestamp
//                                    processedSensorData.timestamp.toFloat(),
                                valueIndex.toFloat(),
                                // Round that to 2 decimal places
                                ((sensorValue * 100).roundToLong() / 100.0f)
                            )
                        ).toMutableList()
                    } catch (
                        e: IndexOutOfBoundsException
                    ) {
                        Logger.getLogger("DataDisplay")
                            .warning("Index out of bounds: $dataIndex")
                    }

                }
            }

            // Now we need to create a new MultiLineData object for each entry
            val deviceColor = generateColorBasedOnName(entry.name)
            for (i in pointListArray.indices) {
                var line = Line(
                    dataPoints = pointListArray[i],
                    LineStyle(
                        color = deviceColor
                    )
                )
                values[i] = values[i].plus(entry to line)
            }
            return values
        }
    }

}