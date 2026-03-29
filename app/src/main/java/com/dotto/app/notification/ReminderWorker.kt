package com.dotto.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dotto.app.DottoApp
import com.dotto.app.MainActivity
import com.dotto.app.R
import java.time.LocalDate

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getLong(KEY_HABIT_ID, -1)
        if (habitId == -1L) return Result.failure()

        val app = context.applicationContext as DottoApp
        val repo = app.habitRepository

        // Don't notify if already checked in today
        val alreadyChecked = repo.isCheckedIn(habitId, LocalDate.now())
        if (alreadyChecked) return Result.success()

        val habit = repo.getHabitById(habitId) ?: return Result.failure()

        ensureChannel()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, habitId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val checkInIntent = Intent(context, CheckInReceiver::class.java).apply {
            action = CheckInReceiver.ACTION_CHECK_IN
            putExtra(CheckInReceiver.EXTRA_HABIT_ID, habitId)
        }
        val checkInPendingIntent = PendingIntent.getBroadcast(
            context, habitId.toInt() + 10000, checkInIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(habit.name)
            .setContentText("Time to check in today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification, "Check in ✓", checkInPendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(habitId.toInt(), notification)

        return Result.success()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders for your habits"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "dotto_reminders"
        const val KEY_HABIT_ID = "habit_id"
    }
}
