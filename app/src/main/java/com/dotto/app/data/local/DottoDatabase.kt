package com.dotto.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dotto.app.data.local.dao.CheckInDao
import com.dotto.app.data.local.dao.HabitDao
import com.dotto.app.data.local.entity.CheckInEntity
import com.dotto.app.data.local.entity.HabitEntity

@Database(
    entities = [HabitEntity::class, CheckInEntity::class],
    version = 4,
    exportSchema = false
)
abstract class DottoDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun checkInDao(): CheckInDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderHour INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE habits ADD COLUMN reminderMinute INTEGER DEFAULT NULL")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE check_ins ADD COLUMN comment TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE habits ADD COLUMN note TEXT DEFAULT NULL")
            }
        }

        fun create(context: Context): DottoDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                DottoDatabase::class.java,
                "dotto.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
    }
}
