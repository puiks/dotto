package com.poco.app.ui.detail

import com.poco.app.data.local.dao.CheckInDao
import com.poco.app.data.local.dao.HabitDao
import com.poco.app.data.local.entity.CheckInEntity
import com.poco.app.data.local.entity.HabitEntity
import com.poco.app.data.repository.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeHabitDao: FakeHabitDao
    private lateinit var fakeCheckInDao: FakeCheckInDao
    private lateinit var repository: HabitRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeHabitDao = FakeHabitDao()
        fakeCheckInDao = FakeCheckInDao()
        repository = HabitRepository(fakeHabitDao, fakeCheckInDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads habit details on init`() = runTest(testDispatcher) {
        fakeHabitDao.insert(HabitEntity(name = "Read", color = 0xFF0000))
        val viewModel = DetailViewModel(repository, 1L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Read", state.habitName)
        assertFalse(state.isLoading)
    }

    @Test
    fun `toggle date updates stats`() = runTest(testDispatcher) {
        fakeHabitDao.insert(HabitEntity(name = "Read", color = 0xFF0000))
        val viewModel = DetailViewModel(repository, 1L)
        advanceUntilIdle()

        viewModel.toggleDate(LocalDate.now())
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.currentStreak)
        assertEquals(1, viewModel.uiState.value.totalCheckIns)
    }

    @Test
    fun `cannot toggle future dates`() = runTest(testDispatcher) {
        fakeHabitDao.insert(HabitEntity(name = "Read", color = 0xFF0000))
        val viewModel = DetailViewModel(repository, 1L)
        advanceUntilIdle()

        viewModel.toggleDate(LocalDate.now().plusDays(1))
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.totalCheckIns)
    }

    @Test
    fun `update habit changes name and color`() = runTest(testDispatcher) {
        fakeHabitDao.insert(HabitEntity(name = "Read", color = 0xFF0000))
        val viewModel = DetailViewModel(repository, 1L)
        advanceUntilIdle()

        viewModel.updateHabit("Write", 0x00FF00)
        advanceUntilIdle()

        assertEquals("Write", viewModel.uiState.value.habitName)
        assertEquals(0x00FF00, viewModel.uiState.value.habitColor)
    }

    // --- Fakes ---

    private class FakeHabitDao : HabitDao {
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
    }

    private class FakeCheckInDao : CheckInDao {
        private val checkIns = mutableListOf<CheckInEntity>()

        override suspend fun get(habitId: Long, date: String): CheckInEntity? =
            checkIns.find { it.habitId == habitId && it.date == date }

        override fun observeByHabit(habitId: Long): Flow<List<CheckInEntity>> =
            MutableStateFlow(checkIns.filter { it.habitId == habitId })

        override fun observeByHabitInRange(
            habitId: Long, startDate: String, endDate: String
        ): Flow<List<CheckInEntity>> =
            MutableStateFlow(checkIns.filter {
                it.habitId == habitId && it.date >= startDate && it.date <= endDate
            })

        override suspend fun getAllDatesByHabit(habitId: Long): List<String> =
            checkIns.filter { it.habitId == habitId }.map { it.date }.sortedDescending()

        override suspend fun insert(checkIn: CheckInEntity) { checkIns.add(checkIn) }
        override suspend fun delete(habitId: Long, date: String) {
            checkIns.removeAll { it.habitId == habitId && it.date == date }
        }
    }
}
