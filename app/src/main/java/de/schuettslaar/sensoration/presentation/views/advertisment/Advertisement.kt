package de.schuettslaar.sensoration.presentation.views.advertisment

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.schuettslaar.sensoration.R

@Composable
fun Advertisement(
    onAdvertisingStop: () -> Unit
) {

    Column {
        Text(stringResource(R.string.advertising_title))
        Button(onClick = {
            onAdvertisingStop()
        }) {
            Text(stringResource(R.string.stop_advertising))
        }
    }
}