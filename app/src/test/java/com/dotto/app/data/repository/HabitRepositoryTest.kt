package com.dotto.app.data.repository

import com.dotto.app.data.local.entity.CheckInEntity
import com.dotto.app.testutil.FakeCheckInDao
import com.dotto.app.testutil.FakeHabitDao
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
    private lateinit var fakeCheckInDao: FakeCheckInDao

    @Before
    fun setup() {
        val fakeHabitDao = FakeHabitDao()
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

    // --- Stats: Current Streak ---

    @Test
    fun `stats are all zero when no check-ins`() = runTest {
        val stats = repository.getStats(1L)
        assertEquals(0, stats.currentStreak)
        assertEquals(0, stats.longestStreak)
        assertEquals(0, stats.totalCheckIns)
    }

    @Test
    fun `current streak counts consecutive days from today`() = runTest {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        fakeCheckInDao.insert(CheckInEntity(1L, today.format(formatter)))
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(1).format(formatter)))
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(2).format(formatter)))

        assertEquals(3, repository.getStats(1L).currentStreak)
    }

    @Test
    fun `current streak starts from yesterday if today not checked`() = runTest {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(1).format(formatter)))
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(2).format(formatter)))

        assertEquals(2, repository.getStats(1L).currentStreak)
    }

    @Test
    fun `current streak breaks on gap`() = runTest {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        fakeCheckInDao.insert(CheckInEntity(1L, today.format(formatter)))
        // Skip yesterday
        fakeCheckInDao.insert(CheckInEntity(1L, today.minusDays(2).format(formatter)))

        assertEquals(1, repository.getStats(1L).currentStreak)
    }

    // --- Stats: Longest Streak ---

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

        assertEquals(5, repository.getStats(1L).longestStreak)
    }

    @Test
    fun `longest streak with single check-in is 1`() = runTest {
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-15"))
        assertEquals(1, repository.getStats(1L).longestStreak)
    }

    // --- Stats: Total ---

    @Test
    fun `total check-ins counts all entries`() = runTest {
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-01"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-05"))
        fakeCheckInDao.insert(CheckInEntity(1L, "2024-01-10"))

        assertEquals(3, repository.getStats(1L).totalCheckIns)
    }

    // --- Comments ---

    @Test
    fun `updateComment stores comment on existing check-in`() = runTest {
        val date = LocalDate.of(2024, 1, 15)
        repository.toggleCheckIn(1L, date)

        repository.updateComment(1L, date, "Felt great today")
        assertEquals("Felt great today", repository.getComment(1L, date))
    }

    @Test
    fun `updateComment truncates to 50 chars`() = runTest {
        val date = LocalDate.of(2024, 1, 15)
        repository.toggleCheckIn(1L, date)

        val longComment = "a".repeat(60)
        repository.updateComment(1L, date, longComment)
        assertEquals(50, repository.getComment(1L, date)!!.length)
    }

    @Test
    fun `updateComment with null clears comment`() = runTest {
        val date = LocalDate.of(2024, 1, 15)
        repository.toggleCheckIn(1L, date)
        repository.updateComment(1L, date, "note")
        repository.updateComment(1L, date, null)

        assertEquals(null, repository.getComment(1L, date))
    }

    @Test
    fun `getComment returns null when no check-in exists`() = runTest {
        assertEquals(null, repository.getComment(1L, LocalDate.of(2024, 1, 15)))
    }

    // --- Input Validation ---

    @Test(expected = IllegalArgumentException::class)
    fun `addHabit rejects blank name`() = runTest {
        repository.addHabit("   ", 0xFF0000)
    }
}
