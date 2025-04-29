package de.schuettslaar.sensoration.presentation.core.textDialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun TextDialog(
    dialogInfo: TextDialogInfo,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = stringResource(dialogInfo.titleResId)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                dialogInfo.textResIds.forEach { textId ->
                    Text(text = stringResource(textId))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("OK")
            }
        },
        modifier = modifier
    )
}