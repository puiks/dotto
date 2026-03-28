package com.poco.app.data.repository

import com.poco.app.data.local.dao.CheckInDao
import com.poco.app.data.local.dao.HabitDao
import com.poco.app.data.local.entity.CheckInEntity
import com.poco.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepositoryTest {

    private lateinit var repository: HabitRepository
    private lateinit var fakeHabitDao: FakeHabitDao
    private lateinit var fakeCheckInDao: FakeCheckInDao

    @Before
    fun setup() {
        fakeHabitDao = FakeHabitDao()
        fakeCheckInDao = FakeCheckInDao()
        repository = HabitRepository(fakeHabitDao, fakeCheckInDao)
    }

    // --- Toggle ---

    @Test
    fun `toggle check-in inserts when not checked`() = runTest {
        val result = repository.toggleCheckIn(1L, LocalDate.of(2024, 1, 15))
        assertTrue(result)
        assertTrue(fakeCheckInDao.hasCheckIn(1L, "2024-01-15"))
    }

    @Test
    fun `toggle check-in deletes when already checked`() = runTest {
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-15"))

        val result = repository.toggleCheckIn(1L, LocalDate.of(2024, 1, 15))
        assertFalse(result)
        assertFalse(fakeCheckInDao.hasCheckIn(1L, "2024-01-15"))
    }

    // --- Current Streak ---

    @Test
    fun `current streak is 0 when no check-ins`() = runTest {
        assertEquals(0, repository.calculateCurrentStreak(1L))
    }

    @Test
    fun `current streak counts consecutive days from today`() = runTest {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        fakeCheckInDao.insert(CheckInEntity(1L, today.format(formatter)))
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(1).format(formatter)))
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(2).format(formatter)))

        assertEquals(3, repository.calculateCurrentStreak(1L))
    }

    @Test
    fun `current streak starts from yesterday if today not checked`() = runTest {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        // Not checked today, but checked yesterday and day before
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(1).format(formatter)))
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(2).format(formatter)))

        assertEquals(2, repository.calculateCurrentStreak(1L))
    }

    @Test
    fun `current streak breaks on gap`() = runTest {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        fakeCheckInDao.insert(CheckInEntity(1L, today.format(formatter)))
        // Skip yesterday
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(2).format(formatter)))

        assertEquals(1, repository.calculateCurrentStreak(1L))
    }

    // --- Longest Streak ---

    @Test
    fun `longest streak is 0 when no check-ins`() = runTest {
        assertEquals(0, repository.calculateLongestStreak(1L))
    }

    @Test
    fun `longest streak finds the longest consecutive run`() = runTest {
        // Run 1: 3 days
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-01"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-02"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-03"))
        // Gap
        // Run 2: 5 days (longest)
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-10"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-11"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-12"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-13"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-14"))
        // Gap
        // Run 3: 2 days
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-20"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-21"))

        assertEquals(5, repository.calculateLongestStreak(1L))
    }

    @Test
    fun `longest streak with single check-in is 1`() = runTest {
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-15"))
        assertEquals(1, repository.calculateLongestStreak(1L))
    }

    // --- Total Check-ins ---

    @Test
    fun `total check-ins counts all entries`() = runTest {
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-01"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-05"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-10"))

        assertEquals(3, repository.totalCheckIns(1L))
    }

    // --- Fakes ---

    private class FakeHabitDao : HabitDao {
        private val habits = mutableListOf<HabitEntity>()
        private var nextId = 1L

        override fun observeAll(): Flow<List<HabitEntity>> = flowOf(habits.toList())

        override suspend fun getById(id: Long): HabitEntity? = habits.find { it.id == id }

        override suspend fun insert(habit: HabitEntity): Long {
            val id = nextId++
            habits.add(habit.copy(id = id))
            return id
        }

        override suspend fun update(habit: HabitEntity) {
            val index = habits.indexOfFirst { it.id == habit.id }
            if (index >= 0) habits[index] = habit
        }

        override suspend fun deleteById(id: Long) {
            habits.removeAll { it.id == id }
        }
    }

    private class FakeCheckInDao : CheckInDao {
        private val checkIns = mutableListOf<CheckInEntity>()

        fun hasCheckIn(habitId: Long, date: String): Boolean =
            checkIns.any { it.habitId == habitId && it.date == date }

        override suspend fun get(habitId: Long, date: String): CheckInEntity? =
            checkIns.find { it.habitId == habitId && it.date == date }

        override fun observeByHabit(habitId: Long): Flow<List<CheckInEntity>> =
            flowOf(checkIns.filter { it.habitId == habitId })

        override fun observeByHabitInRange(
            habitId: Long,
            startDate: String,
            endDate: String
        ): Flow<List<CheckInEntity>> =
            flowOf(checkIns.filter {
                it.habitId == habitId && it.date >= startDate && it.date <= endDate
            })

        override suspend fun getAllDatesByHabit(habitId: Long): List<String> =
            checkIns.filter { it.habitId == habitId }.map { it.date }.sortedDescending()

        override suspend fun insert(checkIn: CheckInEntity) {
            checkIns.add(checkIn)
        }

        override suspend fun delete(habitId: Long, date: String) {
            checkIns.removeAll { it.habitId == habitId && it.date == date }
        }
    }
}
