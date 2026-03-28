package com.poco.app.ui.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    checkedDates: Set<LocalDate>,
    habitColor: Color,
    onDateClick: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // Monday = 1, Sunday = 7
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value
    // Offset to align with Monday start
    val offset = startDayOfWeek - 1

    Column(modifier = modifier) {
        // Month header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
            }
            Text(
                text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${yearMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = onNextMonth,
                enabled = !yearMonth.plusMonths(1).isAfter(YearMonth.now())
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
            }
        }

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            val daysOfWeek = listOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )
            daysOfWeek.forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Calendar grid
        val totalCells = offset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - offset + 1

                    if (dayNumber in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val isChecked = date in checkedDates
                        val isToday = date == today
                        val isFuture = date.isAfter(today)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .then(
                                    if (isChecked) {
                                        Modifier.background(habitColor)
                                    } else if (isToday) {
                                        Modifier.background(habitColor.copy(alpha = 0.1f))
                                    } else {
                                        Modifier
                                    }
                                )
                                .then(
                                    if (!isFuture) {
                                        Modifier.clickable { onDateClick(date) }
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dayNumber",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isChecked -> Color.White
                                    isFuture -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}
