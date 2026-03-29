package com.dotto.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.dotto.app.data.local.entity.CheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun get(habitId: Long, date: String): CheckInEntity?

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId ORDER BY date DESC")
    fun observeByHabit(habitId: Long): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    fun observeByHabitInRange(habitId: Long, startDate: String, endDate: String): Flow<List<CheckInEntity>>

    @Query("SELECT date FROM check_ins WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getAllDatesByHabit(habitId: Long): List<String>

    @Query("SELECT date FROM check_ins WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    suspend fun getDatesByHabitInRange(habitId: Long, startDate: String, endDate: String): List<String>

    @Insert
    suspend fun insert(checkIn: CheckInEntity)

    @Query("DELETE FROM check_ins WHERE habitId = :habitId AND date = :date")
    suspend fun delete(habitId: Long, date: String)

    @Query("UPDATE check_ins SET comment = :comment WHERE habitId = :habitId AND date = :date")
    suspend fun updateComment(habitId: Long, date: String, comment: String?)

    @Query("SELECT * FROM check_ins WHERE habitId = :habitId ORDER BY date ASC")
    suspend fun getAllByHabit(habitId: Long): List<CheckInEntity>
}
