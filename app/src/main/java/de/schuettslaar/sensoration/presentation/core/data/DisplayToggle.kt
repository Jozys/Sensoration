package de.schuettslaar.sensoration.presentation.core.data

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R

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
        TabRow(
            selectedTabIndex = if (isTableView) 1 else 0,
            modifier = Modifier.weight(1f),
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Tab(
                selected = !isTableView,
                onClick = { onToggle(false) },
                text = { Text(stringResource(R.string.display_toggle_chart)) },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            Tab(
                selected = isTableView,
                onClick = { onToggle(true) },
                text = { Text(stringResource(R.string.display_toggle_table)) },
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ViewList,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

        }
    }
}