package com.dotto.app.ui.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dotto.app.DottoApp
import com.dotto.app.R
import com.dotto.app.data.ThemeMode
import com.dotto.app.data.ThemePreference
import com.dotto.app.ui.components.AddHabitSheet
import com.dotto.app.ui.components.MilestoneOverlay
import com.dotto.app.ui.home.components.EmptyState
import com.dotto.app.ui.home.components.HabitCard
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onHabitClick: (Long) -> Unit,
    themePreference: ThemePreference
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val app = context.applicationContext as DottoApp
    val state by viewModel.uiState.collectAsState()
    val themeMode by themePreference.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val scope = rememberCoroutineScope()
    var showAddSheet by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<HabitUiModel?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val json = app.backupManager.export()
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            Toast.makeText(context, "Exported successfully", Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                    ?: return@launch
                app.backupManager.import(json)
                Toast.makeText(context, "Imported successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: invalid file", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                actions = {
                    IconButton(onClick = {
                        val next = when (themeMode) {
                            ThemeMode.SYSTEM -> ThemeMode.LIGHT
                            ThemeMode.LIGHT -> ThemeMode.DARK
                            ThemeMode.DARK -> ThemeMode.SYSTEM
                        }
                        scope.launch { themePreference.setThemeMode(next) }
                    }) {
                        val (icon, desc) = when (themeMode) {
                            ThemeMode.SYSTEM -> R.drawable.ic_theme_system to "Theme: follow system"
                            ThemeMode.LIGHT -> R.drawable.ic_theme_light to "Theme: light mode"
                            ThemeMode.DARK -> R.drawable.ic_theme_dark to "Theme: dark mode"
                        }
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = desc,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export data") },
                                onClick = {
                                    showMenu = false
                                    exportLauncher.launch("dotto-backup.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import data") },
                                onClick = {
                                    showMenu = false
                                    showImportConfirm = true
                                }
                            )
                        }
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
                            .padding(padding)
                            .pointerInput(Unit) {
                                detectTapGestures { focusManager.clearFocus() }
                            },
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
                                },
                                onCommentChange = { comment -> viewModel.updateComment(habit.id, comment) }
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

    // Import confirmation dialog
    if (showImportConfirm) {
        AlertDialog(
            onDismissRequest = { showImportConfirm = false },
            title = { Text("Import data") },
            text = { Text("Imported habits will be added alongside existing ones. Continue?") },
            confirmButton = {
                TextButton(onClick = {
                    showImportConfirm = false
                    importLauncher.launch(arrayOf("application/json"))
                }) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirm = false }) {
                    Text("Cancel")
                }
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
