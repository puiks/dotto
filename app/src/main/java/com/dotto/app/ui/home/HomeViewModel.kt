package com.dotto.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotto.app.data.local.entity.HabitEntity
import com.dotto.app.data.repository.HabitRepository
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
                val today = LocalDate.now()
                val models = habits.map { habit ->
                    val isChecked = repository.isCheckedIn(habit.id, today)
                    val stats = repository.getStats(habit.id)
                    HabitUiModel(
                        id = habit.id,
                        name = habit.name,
                        color = habit.color,
                        isCheckedToday = isChecked,
                        currentStreak = stats.currentStreak
                    )
                }
                _uiState.value = HomeUiState(habits = models, isLoading = false)
            }
        }
    }

    fun toggleCheckIn(habitId: Long) {
        viewModelScope.launch {
            repository.toggleCheckIn(habitId, LocalDate.now())
            // Manually refresh since check_ins table changes don't trigger habits Flow
            reloadStats()
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

    private suspend fun reloadStats() {
        val today = LocalDate.now()
        val updated = _uiState.value.habits.map { habit ->
            val isChecked = repository.isCheckedIn(habit.id, today)
            val stats = repository.getStats(habit.id)
            habit.copy(isCheckedToday = isChecked, currentStreak = stats.currentStreak)
        }
        _uiState.value = _uiState.value.copy(habits = updated)
    }

    class Factory(private val repository: HabitRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
