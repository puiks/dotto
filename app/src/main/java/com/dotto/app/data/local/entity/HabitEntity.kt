package com.dotto.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val note: String? = null
)
