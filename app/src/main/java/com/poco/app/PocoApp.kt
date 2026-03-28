package com.poco.app

import android.app.Application
import com.poco.app.data.local.PocoDatabase
import com.poco.app.data.repository.HabitRepository

class PocoApp : Application() {

    lateinit var database: PocoDatabase
        private set

    lateinit var habitRepository: HabitRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = PocoDatabase.create(this)
        habitRepository = HabitRepository(
            habitDao = database.habitDao(),
            checkInDao = database.checkInDao()
        )
    }
}
