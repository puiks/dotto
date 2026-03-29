package com.dotto.app.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dotto.app.data.repository.HabitRepository
import com.dotto.app.notification.ReminderScheduler
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
    val habitNote: String? = null,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCheckIns: Int = 0,
    val currentMonth: YearMonth = YearMonth.now(),
    val checkedDates: Set<LocalDate> = emptySet(),
    val commentsByDate: Map<LocalDate, String> = emptyMap(),
    val heatmapDates: Set<LocalDate> = emptySet(),
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
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
        loadHeatmap()
    }

    private fun loadHabit() {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            val stats = repository.getStats(habitId)
            _uiState.value = _uiState.value.copy(
                habitName = habit.name,
                habitColor = habit.color,
                habitNote = habit.note,
                currentStreak = stats.currentStreak,
                longestStreak = stats.longestStreak,
                totalCheckIns = stats.totalCheckIns,
                reminderHour = habit.reminderHour,
                reminderMinute = habit.reminderMinute,
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
                val comments = checkIns
                    .filter { !it.comment.isNullOrBlank() }
                    .associate { LocalDate.parse(it.date) to it.comment!! }
                _uiState.value = _uiState.value.copy(
                    checkedDates = dates,
                    commentsByDate = comments
                )
            }
        }
    }

    private fun loadHeatmap() {
        viewModelScope.launch {
            val year = LocalDate.now().year
            val dates = repository.getCheckInDatesForYear(habitId, year)
            _uiState.value = _uiState.value.copy(heatmapDates = dates)
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
            loadHeatmap()
        }
    }

    fun updateComment(date: LocalDate, comment: String) {
        viewModelScope.launch {
            val trimmed = comment.trim()
            repository.updateComment(habitId, date, trimmed.ifEmpty { null })
        }
    }

    fun setReminder(context: Context, hour: Int, minute: Int) {
        viewModelScope.launch {
            repository.setReminder(habitId, hour, minute)
            ReminderScheduler.schedule(context, habitId, hour, minute)
            _uiState.value = _uiState.value.copy(reminderHour = hour, reminderMinute = minute)
        }
    }

    fun clearReminder(context: Context) {
        viewModelScope.launch {
            repository.clearReminder(habitId)
            ReminderScheduler.cancel(context, habitId)
            _uiState.value = _uiState.value.copy(reminderHour = null, reminderMinute = null)
        }
    }

    fun updateHabit(name: String, color: Int, note: String?) {
        viewModelScope.launch {
            val habit = repository.getHabitById(habitId) ?: return@launch
            repository.updateHabit(habit.copy(name = name, color = color, note = note))
            _uiState.value = _uiState.value.copy(habitName = name, habitColor = color, habitNote = note)
        }
    }

    fun deleteHabit(context: Context, onDeleted: () -> Unit) {
        viewModelScope.launch {
            ReminderScheduler.cancel(context, habitId)
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
