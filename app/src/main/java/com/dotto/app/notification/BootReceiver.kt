package com.dotto.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dotto.app.DottoApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as DottoApp
                app.rescheduleAllReminders()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
