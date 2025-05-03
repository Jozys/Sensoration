package de.schuettslaar.sensoration.presentation.core.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.IncompleteCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import de.schuettslaar.sensoration.domain.ApplicationStatus

@Composable
fun ApplicationStatusIcon(
    applicationStatus: ApplicationStatus,
    modifier: Modifier = Modifier,
) {
    BasicIcon<ApplicationStatus>(
        enumValue = applicationStatus,
        getIcon = {
            getIcon(applicationStatus)
        },
        modifier = modifier
    )
}

/**
 * Returns the icon associated with the given [ApplicationStatus].
 *
 * @param applicationStatus The application status for which to get the icon.
 * @return The icon associated with the given application status.
 */
fun getIcon(applicationStatus: ApplicationStatus): ImageVector {
    return when (applicationStatus) {
        ApplicationStatus.INIT -> Icons.Default.RocketLaunch
        ApplicationStatus.IDLE -> Icons.Default.IncompleteCircle
        ApplicationStatus.DESTINATION -> Icons.Default.Flag
        ApplicationStatus.ACTIVE -> Icons.Default.FlashOn
        ApplicationStatus.ERROR -> Icons.Default.Error
        ApplicationStatus.FINISHED -> Icons.Filled.DoneOutline
    }
}