package com.dotto.app.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun HeatmapGrid(
    checkedDates: Set<LocalDate>,
    habitColor: Color,
    year: Int,
    modifier: Modifier = Modifier
) {
    val jan1 = LocalDate.of(year, 1, 1)
    val dec31 = LocalDate.of(year, 12, 31)
    val today = LocalDate.now()

    // Build weeks: each week is Mon-Sun
    val weekFields = WeekFields.of(DayOfWeek.MONDAY, 1)
    val firstMonday = jan1.with(weekFields.dayOfWeek(), 1)

    // Collect all weeks from first Monday on or before Jan 1 to Dec 31
    val weeks = mutableListOf<List<LocalDate?>>()
    var weekStart = firstMonday
    while (weekStart.year <= year || weekStart.isBefore(dec31)) {
        val week = (0 until 7).map { offset ->
            val date = weekStart.plusDays(offset.toLong())
            if (date.year == year) date else null
        }
        weeks.add(week)
        weekStart = weekStart.plusWeeks(1)
        if (weekStart.isAfter(dec31)) break
    }

    Column(modifier = modifier) {
        Text(
            text = "$year",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Month labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D").forEach {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        // Grid: 7 rows (Mon-Sun) × N columns (weeks)
        // Transpose: rows are days of week, columns are weeks
        for (dayOfWeek in 0 until 7) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                weeks.forEach { week ->
                    val date = week.getOrNull(dayOfWeek)
                    val cellSize = 6.dp

                    if (date != null && !date.isAfter(today)) {
                        val isChecked = date in checkedDates
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .clip(RoundedCornerShape(1.dp))
                                .background(
                                    if (isChecked) habitColor
                                    else habitColor.copy(alpha = 0.08f)
                                )
                        )
                    } else {
                        // Future or null
                        Box(modifier = Modifier.size(cellSize))
                    }
                }
            }
        }
    }
}
