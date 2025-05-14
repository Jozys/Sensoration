package de.schuettslaar.sensoration.presentation.core.data

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.line.config.LineChartColorConfig
import com.himanshoe.charty.line.model.LineData
import com.himanshoe.charty.line.model.MultiLineData
import de.schuettslaar.sensoration.presentation.views.advertisment.model.DeviceInfo
import de.schuettslaar.sensoration.utils.generateColorBasedOnName
import java.util.logging.Logger
import kotlin.math.roundToLong

class DataToMultiLineDataService {

    companion object {

        /**
         * We need to parse the data from the sensors
         * Therefore we need to put together a list of MultiLineData grouped by the endPointIds
         * */
        fun parseSensorData(
            data: Map<String, DeviceInfo>,
            primaryColor: Color,
            valueSize: Int
        ): Array<Map<String, MultiLineData>> {
            if (data.isEmpty()) {
                return arrayOf()
            }
            var allSensorValues = Array<Map<String, MultiLineData>>(valueSize) {
                mapOf()
            }

            // We need a list of maps of the MultliLineData
            // That is because the different sensors have different values, all provided in an array
            // But we want to create a graph for each value of them

            for (device in data) {
                val wrappedSensorData = device.value.sensorData

                if (wrappedSensorData.isEmpty()) {
                    continue
                }

                // So we iterate over every device
                allSensorValues = parseDeviceEntry(device, allSensorValues, primaryColor)
            }

            return allSensorValues
        }

        /**
         * Parse the sensor data for a single device
         * @return an array of the sensor data in the format of MultiLineData
         * */
        private fun parseDeviceEntry(
            device: Map.Entry<String, DeviceInfo>,
            initialValues: Array<Map<String, MultiLineData>>,
            primaryColor: Color
        ): Array<Map<String, MultiLineData>> {
            val values = initialValues.copyOf()
            // So we iterate over every device
            val entry = device.key
            val wrappedSensorData = device.value.sensorData

            var lineDataArray = Array<List<LineData>>(values.size) {
                mutableListOf()
            }
            // So now we need to iterate over the sensor data
            wrappedSensorData.forEach { wrappedSensorDataVal ->
                wrappedSensorDataVal.sensorData.value.forEach { sensorValue ->
                    var index = wrappedSensorDataVal.sensorData.value.indexOf(sensorValue)
                    if (!sensorValue.isNaN()) {
                        // Add the sensor data to the localLineData
                        // We need to round the value to 2 decimal places
                        try {
                            lineDataArray[index] = lineDataArray[index].plus(
                                LineData(
                                    xValue = wrappedSensorData.indexOf(wrappedSensorDataVal),
                                    // Round that to 2 decimal places
                                    yValue = (sensorValue * 100).roundToLong() / 100.0f,
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
            for (i in lineDataArray.indices) {

                val multiLineData = MultiLineData(
                    colorConfig = LineChartColorConfig(
                        lineColor = ChartColor.Solid(color = deviceColor),
                        axisColor = ChartColor.Solid(color = primaryColor),
                        gridLineColor = ChartColor.Solid(color = primaryColor),
                        lineFillColor = ChartColor.Solid(color = primaryColor),
                        selectionBarColor = ChartColor.Solid(color = primaryColor),
                    ),
                    data = lineDataArray[i]
                )
                values[i] = values[i].plus(entry to multiLineData)
            }
            return values
        }
    }

}