//package de.schuettslaar.sensoration.presentation.core.data
//
//import androidx.compose.foundation.layout.R
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ShowChart
//import androidx.compose.material.icons.filled.ViewList
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.SegmentedButtonDefaults.Icon
//import androidx.compose.material3.Switch
//import androidx.compose.material3.SwitchDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.unit.dp
//
//@Composable
//fun DisplayToggle(
//    index: Int,
//    isTableView: Boolean,
//    onToggle: (Boolean) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 4.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = if (isTableView)
//                stringResource(R.string.table_view)
//            else stringResource(R.string.chart_view),
//            style = MaterialTheme.typography.labelMedium,
//            modifier = Modifier.weight(1f)
//        )
//
//        Switch(
//            checked = isTableView,
//            onCheckedChange = onToggle,
//            thumbContent = {
//                Icon(
//                    imageVector = if (isTableView)
//                        Icons.Default.ViewList
//                    else Icons.Default.ShowChart,
//                    contentDescription = null,
//                    modifier = Modifier.size(SwitchDefaults.IconSize)
//                )
//            }
//        )
//    }
//}