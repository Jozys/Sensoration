package de.schuettslaar.sensoration.presentation.core.sensor

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.schuettslaar.sensoration.R
import de.schuettslaar.sensoration.domain.exception.UnavailabilityType
import de.schuettslaar.sensoration.domain.sensor.SensorType

@Composable
fun SensorError(
    currentSensorUnavailable: Pair<SensorType, UnavailabilityType>,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.error)
            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Icon(
            imageVector =
                when (
                    currentSensorUnavailable.second
                ) {
                    UnavailabilityType.SENSOR_NOT_SUPPORTED -> Icons.Default.HideSource
                    UnavailabilityType.SENSOR_PERMISSION_DENIED -> Icons.Default.GppBad
                },
            tint = MaterialTheme.colorScheme.onError,
            contentDescription = stringResource(R.string.sensor_error_icon),
            modifier = Modifier.padding(16.dp),
        )

        Text(
            text = stringResource(
                when (currentSensorUnavailable.second) {
                    UnavailabilityType.SENSOR_NOT_SUPPORTED -> R.string.sensor_unavailable
                    UnavailabilityType.SENSOR_PERMISSION_DENIED -> R.string.sensor_permission_denied
                },
                stringResource(currentSensorUnavailable.first.displayNameId)
            ),
            color = MaterialTheme.colorScheme.onError,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
        )



        if (currentSensorUnavailable.second == UnavailabilityType.SENSOR_PERMISSION_DENIED) {
            val context = LocalContext.current
            Button(
                onClick = {
                    openApplicationSettings(context)
                },
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceDim,
                    disabledContentColor = MaterialTheme.colorScheme.surfaceDim,
                )
            ) {
                Text(
                    text = stringResource(R.string.open_permission_settings)
                )
            }
        }
    }


}

fun openApplicationSettings(context: Context) {
    // Implementation to open application settings for permissions
    // This is typically done using an Intent in Android

    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
    }
    context.startActivity(intent)
}