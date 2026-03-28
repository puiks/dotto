package com.dotto.app.ui.home

import com.dotto.app.data.repository.HabitRepository
import com.dotto.app.testutil.FakeCheckInDao
import com.dotto.app.testutil.FakeHabitDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var repository: HabitRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = HabitRepository(FakeHabitDao(), FakeCheckInDao())
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
}
