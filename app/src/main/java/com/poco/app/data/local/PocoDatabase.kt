package com.poco.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.poco.app.data.local.dao.CheckInDao
import com.poco.app.data.local.dao.HabitDao
import com.poco.app.data.local.entity.CheckInEntity
import com.poco.app.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, CheckInEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PocoDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        fun create(context: Context): PocoDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                PocoDatabase::class.java,
                "poco.db"
            ).build()
        }
    }
}
