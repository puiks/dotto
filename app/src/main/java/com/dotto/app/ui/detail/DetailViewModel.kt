package com.dotto.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotto.app.data.repository.HabitRepository
import com.dotto.app.data.repository.HabitStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
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

    private val _currentMonth = MutableStateFlow(YearMonth.now())

    init {
        loadHabit()
        observeMonth()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            val stats = repository.getStats(habitId)
            _uiState.value = _uiState.value.copy(
                habitName = habit.name,
                habitColor = habit.color,
                currentStreak = stats.currentStreak,
                longestStreak = stats.longestStreak,
                totalCheckIns = stats.totalCheckIns,
                isLoading = false
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMonth() {
        viewModelScope.launch {
            _currentMonth.flatMapLatest { month ->
                repository.observeCheckInsForMonth(habitId, month.year, month.monthValue)
            }.collect { checkIns ->
                val dates = checkIns.map { LocalDate.parse(it.date) }.toSet()
                _uiState.value = _uiState.value.copy(checkedDates = dates)
            }
        }
    }

    fun navigateMonth(offset: Int) {
        val newMonth = _currentMonth.value.plusMonths(offset.toLong())
        _currentMonth.value = newMonth
        _uiState.value = _uiState.value.copy(currentMonth = newMonth)
    }

    fun toggleDate(date: LocalDate) {
        if (date.isAfter(LocalDate.now())) return
        viewModelScope.launch {
            repository.toggleCheckIn(habitId, date)
            val stats = repository.getStats(habitId)
            _uiState.value = _uiState.value.copy(
                currentStreak = stats.currentStreak,
                longestStreak = stats.longestStreak,
                totalCheckIns = stats.totalCheckIns
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
