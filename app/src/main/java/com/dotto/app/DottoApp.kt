package com.dotto.app

import android.app.Application
import com.dotto.app.data.local.DottoDatabase
import com.dotto.app.data.repository.HabitRepository

class DottoApp : Application() {

    lateinit var database: DottoDatabase
        private set

    lateinit var habitRepository: HabitRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = DottoDatabase.create(this)
        habitRepository = HabitRepository(
            habitDao = database.habitDao(),
            checkInDao = database.checkInDao()
        )
    }
}
