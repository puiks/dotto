package com.poco.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.poco.app.data.local.entity.HabitEntity
import com.poco.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitUiModel(
    val id: Long,
    val name: String,
    val color: Int,
    val isCheckedToday: Boolean,
    val currentStreak: Int
)

data class HomeUiState(
    val habits: List<HabitUiModel> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: HabitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeHabits()
    }

    private fun observeHabits() {
        viewModelScope.launch {
            repository.observeAllHabits().collect { habits ->
                val models = habits.map { it.toUiModel() }
                _uiState.value = HomeUiState(habits = models, isLoading = false)
            }
        }
    }

    fun toggleCheckIn(habitId: Long) {
        viewModelScope.launch {
            repository.toggleCheckIn(habitId, LocalDate.now())
            refreshHabits()
        }
    }

    fun addHabit(name: String, color: Int) {
        viewModelScope.launch {
            repository.addHabit(name, color)
            // Flow will auto-update via observeHabits
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
        }
    }

    private suspend fun refreshHabits() {
        val currentHabits = _uiState.value.habits
        val updated = currentHabits.map { habit ->
            val isChecked = repository.isCheckedIn(habit.id, LocalDate.now())
            val streak = repository.calculateCurrentStreak(habit.id)
            habit.copy(isCheckedToday = isChecked, currentStreak = streak)
        }
        _uiState.value = _uiState.value.copy(habits = updated)
    }

    private suspend fun HabitEntity.toUiModel(): HabitUiModel {
        return HabitUiModel(
            id = id,
            name = name,
            color = color,
            isCheckedToday = repository.isCheckedIn(id, LocalDate.now()),
            currentStreak = repository.calculateCurrentStreak(id)
        )
    }

    class Factory(private val repository: HabitRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
