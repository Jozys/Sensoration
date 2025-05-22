package de.schuettslaar.sensoration.presentation.core.data

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DisplayToggle(
    isTableView: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        Text(
//            text = if (isTableView)
//                stringResource(R.string.table_view)
//            else stringResource(R.string.chart_view),
//            style = MaterialTheme.typography.labelMedium,
//            modifier = Modifier.weight(1f)
//        )

        Switch(
            checked = isTableView,
            onCheckedChange = onToggle,
            thumbContent = {
                Icon(
                    imageVector = if (isTableView)
                        Icons.AutoMirrored.Filled.ViewList
                    else Icons.AutoMirrored.Filled.ShowChart,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        )
    }
}