package de.schuettslaar.sensoration.presentation.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun Accordion(
    title: String,
    content: @Composable () -> Unit,
) {
    AccordionContent(
        title = title,
        content = content,
    )
}

@Composable
fun AccordionContent(
    title: String,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "accordion-arrow"
    )

    Box(
        modifier = Modifier
            .padding(8.dp)

    ) {
        Column {
            AccordionHeader(title = title, arrowRotation = arrowRotation, onExpandClick = {
                expanded = !expanded
            })
            AnimatedVisibility(
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween()
                ),
                // animate shrink vertically to the top when collapsed
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween()
                ),
                visible = expanded
            ) {
                content()
            }
        }
    }


}

@Composable
fun AccordionHeader(
    title: String,
    arrowRotation: Float = 0f,
    onExpandClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            // clip the ripple effect
            .clip(RoundedCornerShape(12.dp))
            .clickable { onExpandClick() },
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .padding(8.dp)
                    .rotate(arrowRotation)
            )
        }
    }


}
