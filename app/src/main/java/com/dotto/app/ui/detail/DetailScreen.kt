package com.dotto.app.ui.detail

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dotto.app.ui.components.AddHabitSheet
import com.dotto.app.ui.detail.components.CalendarGrid
import com.dotto.app.ui.detail.components.HeatmapGrid
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var commentEditDate by remember { mutableStateOf<LocalDate?>(null) }
    val habitColor = Color(state.habitColor)
    val hasReminder = state.reminderHour != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(habitColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(state.habitName)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (hasReminder) {
                            viewModel.clearReminder(context)
                        } else {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> viewModel.setReminder(context, hour, minute) },
                                9, 0, true
                            ).show()
                        }
                    }) {
                        Icon(
                            if (hasReminder) Icons.Default.Notifications else Icons.Outlined.Notifications,
                            contentDescription = if (hasReminder) "Remove reminder" else "Set reminder"
                        )
                    }
                    IconButton(onClick = { showEditSheet = true }) {
                        Icon(Icons.Default.Edit, "Edit habit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete habit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Reminder indicator
            if (hasReminder) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = habitColor.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            TimePickerDialog(
                                context,
                                { _, hour, minute -> viewModel.setReminder(context, hour, minute) },
                                state.reminderHour ?: 9,
                                state.reminderMinute ?: 0,
                                true
                            ).show()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = habitColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reminder at %02d:%02d".format(state.reminderHour, state.reminderMinute),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Calendar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                CalendarGrid(
                    yearMonth = state.currentMonth,
                    checkedDates = state.checkedDates,
                    commentsByDate = state.commentsByDate,
                    habitColor = habitColor,
                    onDateClick = { viewModel.toggleDate(it) },
                    onDateLongClick = { commentEditDate = it },
                    onPreviousMonth = { viewModel.navigateMonth(-1) },
                    onNextMonth = { viewModel.navigateMonth(1) },
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = "Current",
                    value = state.currentStreak,
                    subtitle = if (state.currentStreak == 0) "Fresh start" else "days",
                    color = habitColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Longest",
                    value = state.longestStreak,
                    subtitle = "days",
                    color = habitColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Total",
                    value = state.totalCheckIns,
                    subtitle = "check-ins",
                    color = habitColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // Heatmap
            if (state.heatmapDates.isNotEmpty() || state.totalCheckIns > 0) {
                Spacer(modifier = Modifier.height(24.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    HeatmapGrid(
                        checkedDates = state.heatmapDates,
                        habitColor = habitColor,
                        year = LocalDate.now().year,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Encouraging message
            if (!state.isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                val message = if (state.currentStreak == 0) {
                    "Today is a fresh start"
                } else if (state.currentStreak >= state.longestStreak && state.currentStreak > 1) {
                    "New personal best! Keep going"
                } else {
                    "${state.currentStreak} day streak, keep it up!"
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete habit?") },
            text = { Text("Delete this habit and all history? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(context) { onBack() }
                    showDeleteDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Comment edit sheet
    commentEditDate?.let { date ->
        val existingComment = state.commentsByDate[date] ?: ""
        var commentText by remember(date) { mutableStateOf(existingComment) }

        ModalBottomSheet(
            onDismissRequest = {
                viewModel.updateComment(date, commentText)
                commentEditDate = null
            },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Text(
                    text = "${date.monthValue}/${date.dayOfMonth} Note",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                BasicTextField(
                    value = commentText,
                    onValueChange = { if (it.length <= 50) commentText = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(habitColor),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        viewModel.updateComment(date, commentText)
                        focusManager.clearFocus()
                        commentEditDate = null
                    }),
                    decorationBox = { innerTextField ->
                        Box {
                            if (commentText.isEmpty()) {
                                Text(
                                    text = "Add a note...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                Text(
                    text = "${commentText.length}/50",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Edit sheet
    if (showEditSheet) {
        AddHabitSheet(
            onDismiss = { showEditSheet = false },
            onSave = { name, color ->
                viewModel.updateHabit(name, color)
                showEditSheet = false
            },
            isEditMode = true,
            initialName = state.habitName,
            initialColor = state.habitColor
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = if (value > 0) color else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
