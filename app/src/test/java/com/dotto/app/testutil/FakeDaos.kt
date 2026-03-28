package com.dotto.app.testutil

import com.dotto.app.data.local.dao.CheckInDao
import com.dotto.app.data.local.dao.HabitDao
import com.dotto.app.data.local.entity.CheckInEntity
import com.dotto.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeHabitDao : HabitDao {
    private val habits = mutableListOf<HabitEntity>()
    private val flow = MutableStateFlow<List<HabitEntity>>(emptyList())
    private var nextId = 1L

    override fun observeAll(): Flow<List<HabitEntity>> = flow

    override suspend fun getById(id: Long): HabitEntity? = habits.find { it.id == id }

    override suspend fun insert(habit: HabitEntity): Long {
        val id = nextId++
        habits.add(habit.copy(id = id))
        flow.value = habits.toList()
        return id
    }

    override suspend fun update(habit: HabitEntity) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index >= 0) {
            habits[index] = habit
            flow.value = habits.toList()
        }
    }

    override suspend fun deleteById(id: Long) {
        habits.removeAll { it.id == id }
        flow.value = habits.toList()
    }

    override suspend fun getAll(): List<HabitEntity> = habits.toList()

    override suspend fun getHabitsWithReminders(): List<HabitEntity> =
        habits.filter { it.reminderHour != null && it.reminderMinute != null }
}

class FakeCheckInDao : CheckInDao {
    private val checkIns = mutableListOf<CheckInEntity>()

    fun hasCheckIn(habitId: Long, date: String): Boolean =
        checkIns.any { it.habitId == habitId && it.date == date }

    override suspend fun get(habitId: Long, date: String): CheckInEntity? =
        checkIns.find { it.habitId == habitId && it.date == date }

    override fun observeByHabit(habitId: Long): Flow<List<CheckInEntity>> =
        MutableStateFlow(checkIns.filter { it.habitId == habitId })

    override fun observeByHabitInRange(
        habitId: Long,
        startDate: String,
        endDate: String
    ): Flow<List<CheckInEntity>> =
        MutableStateFlow(checkIns.filter {
            it.habitId == habitId && it.date >= startDate && it.date <= endDate
        })

    override suspend fun getAllDatesByHabit(habitId: Long): List<String> =
        checkIns.filter { it.habitId == habitId }.map { it.date }.sortedDescending()

    override suspend fun getDatesByHabitInRange(habitId: Long, startDate: String, endDate: String): List<String> =
        checkIns.filter { it.habitId == habitId && it.date >= startDate && it.date <= endDate }.map { it.date }

    override suspend fun insert(checkIn: CheckInEntity) {
        checkIns.add(checkIn)
    }

    override suspend fun delete(habitId: Long, date: String) {
        checkIns.removeAll { it.habitId == habitId && it.date == date }
    }
}
