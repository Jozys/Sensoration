package de.schuettslaar.sensoration.presentation.core.icon

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


/**
 * Allows to render an icon based on an enum value.
 * This is useful for rendering icons based on the state of a device or sensor.
 *
 * @param enumValue The enum value to render the icon for.
 * @param modifier The modifier to apply to the icon.
 * @param getIcon A lambda function that takes the enum value and returns the corresponding icon.
 * */
@Composable()
fun <T : Enum<T>> BasicIcon(
    enumValue: T?,
    modifier: Modifier = Modifier,
    getIcon: (T?) -> ImageVector,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Icon(
        getIcon(enumValue),
        contentDescription = null,
        modifier = modifier
            .padding(4.dp),
        tint = tint
    )
}