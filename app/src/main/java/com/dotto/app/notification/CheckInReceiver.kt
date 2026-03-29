package com.dotto.app.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.dotto.app.DottoApp
import com.dotto.app.widget.DottoWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class CheckInReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1)
        if (habitId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as DottoApp
                app.habitRepository.toggleCheckIn(habitId, LocalDate.now())

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(habitId.toInt())

                val widget = DottoWidget()
                val widgetManager = GlanceAppWidgetManager(context)
                widgetManager.getGlanceIds(DottoWidget::class.java).forEach { glanceId ->
                    widget.update(context, glanceId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_HABIT_ID = "habit_id"
        const val ACTION_CHECK_IN = "com.dotto.app.ACTION_CHECK_IN"
    }
}
