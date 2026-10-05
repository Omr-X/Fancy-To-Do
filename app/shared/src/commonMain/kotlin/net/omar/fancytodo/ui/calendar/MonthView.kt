package net.omar.fancytodo.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.CalendarDate
import net.omar.fancytodo.time.WEEKDAY_ABBREVIATIONS
import net.omar.fancytodo.time.addDays
import net.omar.fancytodo.time.daysInMonth
import net.omar.fancytodo.time.isoDate
import net.omar.fancytodo.time.parseDate
import net.omar.fancytodo.time.weekdayIndex
import net.omar.fancytodo.ui.components.Dot
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.paletteColor

@Composable
internal fun MonthView(state: AppState, month: CalendarDate, byDay: Map<String, List<Task>>, compact: Boolean, onOpenTask: (Task) -> Unit) {
    val first = isoDate(month.year, month.month, 1)
    val offset = weekdayIndex(first)
    val weeks = (offset + daysInMonth(month.year, month.month) + 6) / 7
    val gridStart = addDays(first, -offset)
    val shape = RoundedCornerShape(12.dp)

    Column(Modifier.fillMaxWidth().clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            WEEKDAY_ABBREVIATIONS.forEach {
                Text(it, color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f).padding(start = 10.dp))
            }
        }
        for (week in 0 until weeks) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
            Row(Modifier.fillMaxWidth()) {
                for (column in 0..6) {
                    if (column > 0) Box(Modifier.width(1.dp).height(if (compact) 64.dp else 112.dp).background(BorderColor))
                    val day = addDays(gridStart, week * 7 + column)
                    val inMonth = parseDate(day).month == month.month
                    val tasks = byDay[day].orEmpty()
                    Column(
                        Modifier.weight(1f).height(if (compact) 64.dp else 112.dp)
                            .background(if (day == state.today) AccentColor.copy(alpha = 0.06f) else Color.Transparent)
                            .padding(6.dp)
                    ) {
                        Box(Modifier.alpha(if (inMonth) 1f else 0.35f)) { DayNumber(parseDate(day).day, day == state.today) }
                        Spacer(Modifier.height(4.dp))
                        if (compact) {
                            // Too narrow for titles: a dot per task.
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                tasks.take(4).forEach { Dot(paletteColor(state.colorIndexOf(it)), 6.dp) }
                            }
                        } else {
                            DayTasks(state, tasks, 2, onOpenTask)
                        }
                    }
                }
            }
        }
    }
}
