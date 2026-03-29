package com.dotto.app

import android.app.Application
import com.dotto.app.data.BackupManager
import com.dotto.app.data.ThemePreference
import com.dotto.app.data.local.DottoDatabase
import com.dotto.app.data.repository.HabitRepository
import com.dotto.app.notification.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DottoApp : Application() {

    lateinit var database: DottoDatabase
        private set

    lateinit var habitRepository: HabitRepository
        private set

    lateinit var themePreference: ThemePreference
        private set

    lateinit var backupManager: BackupManager
        private set

    override fun onCreate() {
        super.onCreate()
        database = DottoDatabase.create(this)
        habitRepository = HabitRepository(
            habitDao = database.habitDao(),
            checkInDao = database.checkInDao()
        )
        themePreference = ThemePreference(this)
        backupManager = BackupManager(
            habitDao = database.habitDao(),
            checkInDao = database.checkInDao()
        )
        CoroutineScope(Dispatchers.IO).launch { rescheduleAllReminders() }
    }

    suspend fun rescheduleAllReminders() {
        habitRepository.getHabitsWithReminders().forEach { habit ->
            val hour = habit.reminderHour ?: return@forEach
            val minute = habit.reminderMinute ?: return@forEach
            ReminderScheduler.schedule(this@DottoApp, habit.id, hour, minute)
        }
    }
}
