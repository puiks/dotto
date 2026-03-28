package com.dotto.app.ui.detail

import com.dotto.app.data.local.entity.HabitEntity
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
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeHabitDao: FakeHabitDao
    private lateinit var repository: HabitRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeHabitDao = FakeHabitDao()
        repository = HabitRepository(fakeHabitDao, FakeCheckInDao())
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
}
