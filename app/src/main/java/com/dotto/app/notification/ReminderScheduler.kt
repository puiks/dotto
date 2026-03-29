package com.dotto.app.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context, habitId: Long, hour: Int, minute: Int) {
        val wm = WorkManager.getInstance(context)
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(LocalTime.of(hour, minute))
        if (target.isBefore(now)) {
            target = target.plusDays(1)
        }
        val initialDelay = Duration.between(now, target).toMillis()

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_HABIT_ID to habitId))
            .build()

        wm.enqueueUniquePeriodicWork(
            workName(habitId),
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            request
        )
    }

    fun cancel(context: Context, habitId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(habitId))
    }

    private fun workName(habitId: Long) = "reminder_$habitId"
}
