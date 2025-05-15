package de.schuettslaar.sensoration.presentation.core.data

import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineStyle
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.utils.generateColorBasedOnName
import java.util.logging.Logger
import kotlin.math.roundToLong

class DataToLineDataService {

    companion object {

        /**
         * We need to parse the data from the sensors
         * Therefore we need to put together a list of MultiLineData grouped by the endPointIds
         * */
        fun parseSensorData(
            data: Map<String, DeviceInfo>,
            valueSize: Int
        ): Array<Map<String, Line>> {
            if (data.isEmpty()) {
                return arrayOf()
            }
            var allSensorValues = Array<Map<String, Line>>(valueSize) {
                mapOf()
            }

            // We need a list of maps of the Lines
            // That is because the different sensors have different values, all provided in an array
            // But we want to create a graph for each value of them

            for (device in data) {
                val wrappedSensorData = device.value.sensorData

                if (wrappedSensorData.isEmpty()) {
                    continue
                }

                // So we iterate over every device
                allSensorValues = parseDeviceEntry(device, allSensorValues)
            }

            return allSensorValues
        }

        /**
         * Parse the sensor data for a single device
         * @return an array of the sensor data in the format of MultiLineData
         * */
        private fun parseDeviceEntry(
            device: Map.Entry<String, DeviceInfo>,
            initialValues: Array<Map<String, Line>>,
        ): Array<Map<String, Line>> {
            val values = initialValues.copyOf()
            // So we iterate over every device
            val entry = device.key
            val wrappedSensorData = device.value.sensorData

            var pointsArray = Array<List<co.yml.charts.common.model.Point>>(values.size) {
                mutableListOf()
            }
            // So now we need to iterate over the sensor data
            wrappedSensorData.forEach { wrappedSensorDataVal ->
                wrappedSensorDataVal.sensorData.value.forEach { sensorValue ->
                    var index = wrappedSensorDataVal.sensorData.value.indexOf(sensorValue)
                    if (!sensorValue.isNaN()) {
                        // Add the sensor data to the localLineData
                        // We need to round the value to 2 decimal places
                        var valueIndex = wrappedSensorData.indexOf(wrappedSensorDataVal)
                        try {
                            pointsArray[index] = pointsArray[index].plus(
                                Point(
                                    valueIndex.toFloat(),
                                    // Round that to 2 decimal places
                                    ((sensorValue * 100).roundToLong() / 100.0f)
                                )
                            ).toMutableList()
                        } catch (
                            e: IndexOutOfBoundsException
                        ) {
                            Logger.getLogger("DataDisplay").warning("Index out of bounds: $index")
                        }
                    }
                }
            }

            // Now we need to create a new MultiLineData object for each entry
            val deviceColor = generateColorBasedOnName(entry)
            for (i in pointsArray.indices) {
                var line = Line(
                    dataPoints = pointsArray[i],
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