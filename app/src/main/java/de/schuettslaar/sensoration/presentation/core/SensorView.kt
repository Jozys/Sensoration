package de.schuettslaar.sensoration.presentation.core

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.domain.sensor.SensorType

@Composable()
fun SensorView(
    selectedSensorType: SensorType?,
    sensorTypes: List<SensorType>,
    disabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    onSensorTypeSelected: (sensorType: SensorType) -> Unit = { },
) {
    val isDropDownExpanded = remember {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier.clickable(enabled = !disabled, onClick = {
            isDropDownExpanded.value = !isDropDownExpanded.value
        })
    ) {
        content()
    }

    DropdownMenu(
        expanded = isDropDownExpanded.value,
        onDismissRequest = {
            isDropDownExpanded.value = false
        },
        modifier = modifier,
    ) {
        sensorTypes.forEach { sensorType ->
            DropdownMenuItem(onClick = {
                isDropDownExpanded.value = false
                if (selectedSensorType != null && selectedSensorType == sensorType) return@DropdownMenuItem
                onSensorTypeSelected(sensorType)
            }, text = {
                SensorItem(sensorType = sensorType);
            })
        }
    }
}

@Composable
fun SensorItem(sensorType: SensorType, selected: Boolean = false, modifier: Modifier = Modifier) {
    Row(modifier = modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        SensorIcon(
            sensorType = sensorType,
            modifier = Modifier
                .padding(end = 8.dp)
                .padding(4.dp)
        )
        Text(
            text = stringResource(sensorType.displayNameId),
            modifier = Modifier
        )
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
            )
        }
    }
}