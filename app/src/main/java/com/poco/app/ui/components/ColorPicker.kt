package com.poco.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.poco.app.ui.theme.HabitColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HabitColors.forEach { color ->
            val colorInt = color.value.toInt()
            val isSelected = colorInt == selectedColor
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "colorScale"
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (isSelected) {
                            Modifier.border(3.dp, Color.White, CircleShape)
                                .border(4.dp, color.copy(alpha = 0.5f), CircleShape)
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onColorSelected(colorInt) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}
