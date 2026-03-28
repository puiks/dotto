package com.dotto.app.ui.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dotto.app.ui.home.HabitUiModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habit: HabitUiModel,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val habitColor = Color(habit.color)

    val checkScale by animateFloatAsState(
        targetValue = if (habit.isCheckedToday) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    val checkBgColor by animateColorAsState(
        targetValue = if (habit.isCheckedToday) habitColor else Color(0xFFE8E8E8),
        label = "checkBgColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: color indicator + habit info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .size(4.dp, 40.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(habitColor)
                )

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (habit.currentStreak > 0) {
                        Text(
                            text = "${habit.currentStreak} day streak \uD83D\uDD25",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Today is a fresh start",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Right: check button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(checkScale)
                    .clip(CircleShape)
                    .background(checkBgColor)
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggle()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (habit.isCheckedToday) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
