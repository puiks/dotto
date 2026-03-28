package com.dotto.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotto.app.data.local.entity.CheckInEntity
import com.dotto.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class DetailUiState(
    val habitName: String = "",
    val habitColor: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCheckIns: Int = 0,
    val currentMonth: YearMonth = YearMonth.now(),
    val checkedDates: Set<LocalDate> = emptySet(),
    val isLoading: Boolean = true
)

class DetailViewModel(
    private val repository: HabitRepository,
    private val habitId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        loadHabit()
        observeMonth()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            val streak = repository.calculateCurrentStreak(habitId)
            val longest = repository.calculateLongestStreak(habitId)
            val total = repository.totalCheckIns(habitId)
            _uiState.value = _uiState.value.copy(
                habitName = habit.name,
                habitColor = habit.color,
                currentStreak = streak,
                longestStreak = longest,
                totalCheckIns = total,
                isLoading = false
            )
        }
    }

    private fun observeMonth() {
        viewModelScope.launch {
            val month = _uiState.value.currentMonth
            repository.observeCheckInsForMonth(habitId, month.year, month.monthValue)
                .collect { checkIns ->
                    val dates = checkIns.map { LocalDate.parse(it.date) }.toSet()
                    _uiState.value = _uiState.value.copy(checkedDates = dates)
                }
        }
    }

    fun navigateMonth(offset: Int) {
        val newMonth = _uiState.value.currentMonth.plusMonths(offset.toLong())
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
        viewModelScope.launch {
            repository.observeCheckInsForMonth(habitId, newMonth.year, newMonth.monthValue)
                .collect { checkIns ->
                    val dates = checkIns.map { LocalDate.parse(it.date) }.toSet()
                    _uiState.value = _uiState.value.copy(checkedDates = dates)
                }
        }
    }

    fun toggleDate(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) return // Can't check future dates
        viewModelScope.launch {
            repository.toggleCheckIn(habitId, date)
            // Refresh stats
            val streak = repository.calculateCurrentStreak(habitId)
            val longest = repository.calculateLongestStreak(habitId)
            val total = repository.totalCheckIns(habitId)
            _uiState.value = _uiState.value.copy(
                currentStreak = streak,
                longestStreak = longest,
                totalCheckIns = total
            )
        }
    }

    fun updateHabit(name: String, color: Int) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            repository.updateHabit(habit.copy(name = name, color = color))
            _uiState.value = _uiState.value.copy(habitName = name, habitColor = color)
        }
    }

    fun deleteHabit(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            onDeleted()
        }
    }

    class Factory(
        private val repository: HabitRepository,
        private val habitId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DetailViewModel(repository, habitId) as T
        }
    }
}
