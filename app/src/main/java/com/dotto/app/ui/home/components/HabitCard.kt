package com.dotto.app.ui.home.components

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dotto.app.ui.components.ConfettiEffect
import com.dotto.app.ui.home.HabitUiModel
import com.dotto.app.ui.theme.UncheckedColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitCard(
    habit: HabitUiModel,
    onCheckInClick: () -> Unit,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val habitColor = Color(habit.color)
    var isEditing by remember { mutableStateOf(false) }
    var hasFocused by remember { mutableStateOf(false) }
    var editText by remember(habit.name) {
        mutableStateOf(TextFieldValue(habit.name, TextRange(habit.name.length)))
    }
    val focusRequester = remember { FocusRequester() }
    var showConfetti by remember { mutableStateOf(false) }

    val checkScale by animateFloatAsState(
        targetValue = if (habit.isCheckedToday) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkScale"
    )

    val checkBgColor by animateColorAsState(
        targetValue = if (habit.isCheckedToday) habitColor else UncheckedColor,
        label = "checkBgColor"
    )

    val streakText = if (habit.currentStreak > 0) {
        "${habit.currentStreak} day streak"
    } else {
        "Today is a fresh start"
    }

    fun commitAndExit() {
        val trimmed = editText.text.trim()
        if (trimmed.isNotEmpty() && trimmed != habit.name) {
            onRename(trimmed)
        }
        isEditing = false
        hasFocused = false
        focusManager.clearFocus()
    }

    // Intercept system back to save and exit edit mode
    BackHandler(enabled = isEditing) {
        commitAndExit()
    }

    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${habit.name}, $streakText"
            }
            .combinedClickable(
                onClick = {
                    if (isEditing) {
                        commitAndExit()
                    } else {
                        onClick()
                    }
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isEditing = true
                    editText = TextFieldValue(habit.name, TextRange(habit.name.length))
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
                    if (isEditing) {
                        BasicTextField(
                            value = editText,
                            onValueChange = { editText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester)
                                .onFocusChanged { state ->
                                    if (state.isFocused) {
                                        hasFocused = true
                                    } else if (hasFocused && isEditing) {
                                        // Lost focus after having been focused → save and exit
                                        commitAndExit()
                                    }
                                },
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(habitColor),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { commitAndExit() })
                        )
                    } else {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
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
            }

            // Right: check button or delete button
            if (isEditing) {
                // Delete button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDelete()
                        }
                        .semantics { contentDescription = "Delete ${habit.name}" },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                // Check button with confetti
                val checkLabel = if (habit.isCheckedToday) "Uncheck ${habit.name}" else "Check in ${habit.name}"
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCheckInClick()
                            }
                            .semantics { contentDescription = checkLabel },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .scale(checkScale)
                                .clip(CircleShape)
                                .background(checkBgColor),
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
                    ConfettiEffect(
                        color = habitColor,
                        trigger = showConfetti,
                        modifier = Modifier.size(80.dp)
                    )
                }

                // Reset confetti after animation
                LaunchedEffect(showConfetti) {
                    if (showConfetti) {
                        kotlinx.coroutines.delay(800)
                        showConfetti = false
                    }
                }
            }
        }

        // Show comment preview if checked in and has a comment
        if (habit.isCheckedToday && !isEditing && !habit.comment.isNullOrBlank()) {
            Text(
                text = habit.comment,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 16.dp, bottom = 12.dp)
            )
        }
    }
}
