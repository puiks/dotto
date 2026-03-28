package com.dotto.app.ui.home

import com.dotto.app.data.local.dao.CheckInDao
import com.dotto.app.data.local.dao.HabitDao
import com.dotto.app.data.local.entity.CheckInEntity
import com.dotto.app.data.local.entity.HabitEntity
import com.dotto.app.data.repository.HabitRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeHabitDao: FakeHabitDao
    private lateinit var fakeCheckInDao: FakeCheckInDao
    private lateinit var repository: HabitRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeHabitDao = FakeHabitDao()
        fakeCheckInDao = FakeCheckInDao()
        repository = HabitRepository(fakeHabitDao, fakeCheckInDao)
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state shows empty habits after loading`() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.habits.isEmpty())
    }

    @Test
    fun `adding a habit updates the list`() = runTest(testDispatcher) {
        advanceUntilIdle()
        viewModel.addHabit("Read English", 0xFF5B6ABF.toInt())
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.habits.size)
        assertEquals("Read English", state.habits[0].name)
    }

    @Test
    fun `toggle check-in updates checked state`() = runTest(testDispatcher) {
        // Add a habit first
        viewModel.addHabit("Listen", 0xFF5B6ABF.toInt())
        advanceUntilIdle()

        val habitId = viewModel.uiState.value.habits[0].id
        viewModel.toggleCheckIn(habitId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.habits[0].isCheckedToday)
    }

    @Test
    fun `delete habit removes it from list`() = runTest(testDispatcher) {
        viewModel.addHabit("Temp", 0xFF5B6ABF.toInt())
        advanceUntilIdle()

        val habitId = viewModel.uiState.value.habits[0].id
        viewModel.deleteHabit(habitId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.habits.isEmpty())
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

        override suspend fun insert(checkIn: CheckInEntity) {
            checkIns.add(checkIn)
        }

        override suspend fun delete(habitId: Long, date: String) {
            checkIns.removeAll { it.habitId == habitId && it.date == date }
        }
    }
}
