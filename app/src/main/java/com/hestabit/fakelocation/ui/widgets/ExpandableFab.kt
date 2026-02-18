package com.hestabit.fakelocation.ui.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hestabit.fakelocation.data.model.FabOption

@Composable
fun ExpandableFab(
    fabIcon: ImageVector,
    items: List<FabOption>,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.BottomStart
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 45f else 0f)

    Box(modifier = modifier) {
        // Fullscreen transparent overlay to collapse when tapping outside - placed before FABs
        if (expanded) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = false }
            )
        }

        Column(
            modifier = Modifier
                .wrapContentSize()
                .align(alignment)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Action items (appear above main FAB)
            items.forEachIndexed { index, item ->
                AnimatedVisibility(
                    visible = expanded,
                    enter = slideInVertically { (it * (items.size - index)) } + fadeIn(),
                    exit = slideOutVertically { (it * (items.size - index)) } + fadeOut()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        FloatingActionButton(
                            shape = CircleShape,
                            onClick = {
                                item.onClick()
                                expanded = false
                            }
                        ) {
                            Icon(item.icon, contentDescription = item.label)
                        }

                        Surface(
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            Text(
                                text = item.label,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main FAB (rotates when expanded)
            FloatingActionButton(
                shape = CircleShape,
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    fabIcon,
                    contentDescription = "Open actions",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
    }
}