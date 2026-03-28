package com.poco.app.data.repository

import com.poco.app.data.local.dao.CheckInDao
import com.poco.app.data.local.dao.HabitDao
import com.poco.app.data.local.entity.CheckInEntity
import com.poco.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(
    private val habitDao: HabitDao,
    private val checkInDao: CheckInDao
) {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun observeAllHabits(): Flow<List<HabitEntity>> = habitDao.observeAll()

    suspend fun getHabitById(id: Long): HabitEntity? = habitDao.getById(id)

    suspend fun addHabit(name: String, color: Int): Long {
        return habitDao.insert(HabitEntity(name = name, color = color))
    }

    suspend fun updateHabit(habit: HabitEntity) = habitDao.update(habit)

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

    suspend fun isCheckedIn(habitId: Long, date: LocalDate): Boolean {
        val dateStr = date.format(dateFormatter)
        return checkInDao.get(habitId, dateStr) != null
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

    suspend fun calculateCurrentStreak(habitId: Long): Int {
        val dates = checkInDao.getAllDatesByHabit(habitId)
            .map { LocalDate.parse(it, dateFormatter) }
            .toSet()

        var streak = 0
        var current = LocalDate.now()

        // If today is not checked, start from yesterday
        if (current !in dates) {
            current = current.minusDays(1)
        }

        while (current in dates) {
            streak++
            current = current.minusDays(1)
        }

        return streak
    }

    suspend fun calculateLongestStreak(habitId: Long): Int {
        val dates = checkInDao.getAllDatesByHabit(habitId)
            .map { LocalDate.parse(it, dateFormatter) }
            .sorted()

        if (dates.isEmpty()) return 0

        var longest = 1
        var current = 1

        for (i in 1 until dates.size) {
            if (dates[i] == dates[i - 1].plusDays(1)) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 1
            }
        }

        return longest
    }

    suspend fun totalCheckIns(habitId: Long): Int {
        return checkInDao.getAllDatesByHabit(habitId).size
    }
}
