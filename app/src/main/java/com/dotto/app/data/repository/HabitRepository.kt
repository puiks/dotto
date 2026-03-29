package com.dotto.app.data.repository

import com.dotto.app.data.local.dao.CheckInDao
import com.dotto.app.data.local.dao.HabitDao
import com.dotto.app.data.local.entity.CheckInEntity
import com.dotto.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HabitStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCheckIns: Int
)

class HabitRepository(
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun observeAllHabits(): Flow<List<HabitEntity>> = habitDao.observeAll()

    suspend fun getHabitsSnapshot(): List<HabitEntity> = habitDao.getAll()

    suspend fun getHabitById(id: Long): HabitEntity? = habitDao.getById(id)

    suspend fun addHabit(name: String, color: Int): Long {
        require(name.isNotBlank()) { "Habit name cannot be empty" }
        return habitDao.insert(HabitEntity(name = name.trim(), color = color))
    }

    suspend fun updateHabit(habit: HabitEntity) = habitDao.update(habit)

    suspend fun setReminder(habitId: Long, hour: Int, minute: Int) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(habit.copy(reminderHour = hour, reminderMinute = minute))
    }

    suspend fun clearReminder(habitId: Long) {
        val habit = habitDao.getById(habitId) ?: return
        habitDao.update(habit.copy(reminderHour = null, reminderMinute = null))
    }

    suspend fun getHabitsWithReminders(): List<HabitEntity> = habitDao.getHabitsWithReminders()

    suspend fun deleteHabit(id: Long) = habitDao.deleteById(id)

    suspend fun toggleCheckIn(habitId: Long, date: LocalDate): Boolean {
        val dateStr = date.format(dateFormatter)
        val existing = checkInDao.get(habitId, dateStr)
        return if (existing != null) {
            checkInDao.delete(habitId, dateStr)
            false // unchecked
        } else {
            checkInDao.insert(CheckInEntity(habitId = habitId, date = dateStr))
            true // checked
        }
    }

    suspend fun updateComment(habitId: Long, date: LocalDate, comment: String?) {
        val dateStr = date.format(dateFormatter)
        checkInDao.updateComment(habitId, dateStr, comment?.take(50)?.trim())
    }

    suspend fun getComment(habitId: Long, date: LocalDate): String? {
        val dateStr = date.format(dateFormatter)
        return checkInDao.get(habitId, dateStr)?.comment
    }

    suspend fun isCheckedIn(habitId: Long, date: LocalDate): Boolean {
        val dateStr = date.format(dateFormatter)
        return checkInDao.get(habitId, dateStr) != null
    }

    suspend fun getCheckInDatesForYear(habitId: Long, year: Int): Set<LocalDate> {
        val start = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        return checkInDao.getDatesByHabitInRange(
            habitId,
            start.format(dateFormatter),
            end.format(dateFormatter)
        ).map { LocalDate.parse(it, dateFormatter) }.toSet()
    }

    fun observeCheckInsForMonth(habitId: Long, year: Int, month: Int): Flow<List<CheckInEntity>> {
        val start = LocalDate.of(year, month, 1)
        val end = start.withDayOfMonth(start.lengthOfMonth())
        return checkInDao.observeByHabitInRange(
            habitId,
            start.format(dateFormatter),
            end.format(dateFormatter)
        )
    }

    /**
     * Calculates all stats in a single pass over the check-in dates.
     * One DB query, one iteration — no redundant work.
     */
    suspend fun getStats(habitId: Long): HabitStats {
        val dates = checkInDao.getAllDatesByHabit(habitId)
            .map { LocalDate.parse(it, dateFormatter) }
            .sorted()

        if (dates.isEmpty()) return HabitStats(0, 0, 0)

        val dateSet = dates.toSet()
        val today = LocalDate.now()

        // Current streak: walk backwards from today (or yesterday if today not checked)
        var currentStreak = 0
        var cursor = if (today in dateSet) today else today.minusDays(1)
        while (cursor in dateSet) {
            currentStreak++
            cursor = cursor.minusDays(1)
        }

        // Longest streak: single forward pass
        var longestStreak = 1
        var runLength = 1
        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].plusDays(1)) {
                runLength++
                longestStreak = maxOf(longestStreak, runLength)
            } else {
                runLength = 1
            }
        }

        return HabitStats(
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            totalCheckIns = dates.size
        )
    }
}
