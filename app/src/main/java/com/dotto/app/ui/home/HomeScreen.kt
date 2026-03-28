package com.dotto.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dotto.app.ui.components.AddHabitSheet
import com.dotto.app.ui.components.MilestoneOverlay
import com.dotto.app.ui.home.components.EmptyState
import com.dotto.app.ui.home.components.HabitCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onHabitClick: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitUiModel?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Dotto", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = today,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!state.isLoading && state.habits.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showAddSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add habit")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300)),
                exit = fadeOut()
            ) {
                if (state.habits.isEmpty()) {
                    EmptyState(
                        onAddClick = { showAddSheet = true },
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.habits, key = { it.id }) { habit ->
                            HabitCard(
                                habit = habit,
                                onToggle = { viewModel.toggleCheckIn(habit.id) },
                                onClick = { onHabitClick(habit.id) },
                                onRename = { newName -> viewModel.renameHabit(habit.id, newName) },
                                onDelete = {
                                    habitToDelete = habit
                                    showDeleteConfirm = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Milestone celebration
    state.milestoneStreak?.let { days ->
        MilestoneOverlay(
            days = days,
            onDismiss = { viewModel.dismissMilestone() }
        )
    }

    if (showAddSheet) {
        AddHabitSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, color ->
                viewModel.addHabit(name, color)
                showAddSheet = false
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        habitToDelete?.let { habit ->
            AlertDialog(
                onDismissRequest = {
                    showDeleteConfirm = false
                    habitToDelete = null
                },
                title = { Text("Delete habit?") },
                text = { Text("Remove \"${habit.name}\" and all its history?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteHabit(habit.id)
                        showDeleteConfirm = false
                        habitToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        habitToDelete = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
