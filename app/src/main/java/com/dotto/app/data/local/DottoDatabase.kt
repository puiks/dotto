package com.dotto.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dotto.app.data.local.dao.CheckInDao
import com.dotto.app.data.local.dao.HabitDao
import com.dotto.app.data.local.entity.CheckInEntity
import com.dotto.app.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, CheckInEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DottoDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        fun create(context: Context): DottoDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                DottoDatabase::class.java,
                "dotto.db"
            ).build()
        }
    }
}
