package com.poco.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.poco.app.ui.components.AddHabitSheet
import com.poco.app.ui.home.components.EmptyState
import com.poco.app.ui.home.components.HabitCard
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

    val today = remember {
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Poco", style = MaterialTheme.typography.headlineMedium)
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
            if (state.habits.isNotEmpty()) {
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
        AnimatedVisibility(
            visible = !state.isLoading,
            enter = fadeIn(),
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
                            onClick = { onHabitClick(habit.id) }
                        )
                    }
                }
            }
        }
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
}
