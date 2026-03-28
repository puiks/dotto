package com.dotto.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import android.graphics.Color as AndroidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dotto.app.DottoApp
import com.dotto.app.MainActivity
import java.time.LocalDate

class DottoWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as DottoApp
        val repo = app.habitRepository
        val today = LocalDate.now()

        val habits = repo.getHabitsSnapshot()
        val items = habits.take(5).map { habit ->
            val checked = repo.isCheckedIn(habit.id, today)
            val stats = repo.getStats(habit.id)
            WidgetHabit(
                name = habit.name,
                color = habit.color,
                isChecked = checked,
                streak = stats.currentStreak
            )
        }

        provideContent {
            WidgetContent(items)
        }
    }
}

private data class WidgetHabit(
    val name: String,
    val color: Int,
    val isChecked: Boolean,
    val streak: Int
)

@Composable
private fun WidgetContent(habits: List<WidgetHabit>) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "Dotto",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = GlanceTheme.colors.onBackground
            )
        )
        Spacer(modifier = GlanceModifier.height(8.dp))

        if (habits.isEmpty()) {
            Text(
                text = "No habits yet",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = GlanceTheme.colors.onBackground
                )
            )
        } else {
            habits.forEach { habit ->
                WidgetHabitRow(habit)
                Spacer(modifier = GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun WidgetHabitRow(habit: WidgetHabit) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (habit.isChecked) "✓" else "○",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorProvider(habit.color)
            )
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = habit.name,
            style = TextStyle(
                fontSize = 13.sp,
                color = GlanceTheme.colors.onBackground
            ),
            maxLines = 1
        )
        if (habit.streak > 0) {
            Spacer(modifier = GlanceModifier.width(4.dp))
            Text(
                text = "${habit.streak}d",
                style = TextStyle(
                    fontSize = 11.sp,
                    color = GlanceTheme.colors.onBackground
                )
            )
        }
    }
}

class DottoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DottoWidget()
}
