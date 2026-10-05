package net.omar.fancytodo.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.CalendarDate
import net.omar.fancytodo.time.MONTH_NAMES
import net.omar.fancytodo.time.WEEKDAY_ABBREVIATIONS
import net.omar.fancytodo.time.addDays
import net.omar.fancytodo.time.isoDate
import net.omar.fancytodo.time.parseDate
import net.omar.fancytodo.time.shortDate
import net.omar.fancytodo.time.weekdayIndex
import net.omar.fancytodo.ui.components.ChevronIcon
import net.omar.fancytodo.ui.components.Dot
import net.omar.fancytodo.ui.components.GhostButton
import net.omar.fancytodo.ui.components.LockIcon
import net.omar.fancytodo.ui.components.ScreenScaffold
import net.omar.fancytodo.ui.components.Segmented
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.DangerColor
import net.omar.fancytodo.ui.theme.HeadingLineHeight
import net.omar.fancytodo.ui.theme.HeadingTracking
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.paletteColor

private enum class CalendarView(val label: String) { Week("Week"), Month("Month") }

/** Tasks laid out by due date, as a week (default) or a month. Weeks start on Monday. */
@Composable
internal fun CalendarScreen(state: AppState, compact: Boolean, onOpenTask: (Task) -> Unit) {
    var view by remember { mutableStateOf(CalendarView.Week) }
    // Any day inside the week/month being shown.
    var anchor by remember { mutableStateOf(state.today) }
    val anchorDate = parseDate(anchor)

    // Parent tasks are covered by their subtasks (which inherit the parent's date when they have none).
    val byDay = state.tasks.filter { !state.isParent(it) }
        .mapNotNull { t -> state.effectiveDue(t)?.let { it to t } }
        .groupBy({ it.first }, { it.second })
        .mapValues { (_, list) -> list.sortedWith(compareBy({ it.isCompleted }, { it.title.lowercase() })) }

    val weekStart = addDays(anchor, -weekdayIndex(anchor))
    val title = when (view) {
        CalendarView.Week -> {
            val end = addDays(weekStart, 6)
            "${shortDate(weekStart)} – ${shortDate(end)} ${parseDate(end).year}"
        }
        CalendarView.Month -> "${MONTH_NAMES[anchorDate.month - 1]} ${anchorDate.year}"
    }

    ScreenScaffold(compact) {
        Text(title, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Medium, letterSpacing = HeadingTracking, lineHeight = HeadingLineHeight)
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            NavButton(left = true) {
                anchor = when (view) {
                    CalendarView.Week -> addDays(anchor, -7)
                    CalendarView.Month -> shiftMonth(anchorDate, -1)
                }
            }
            Spacer(Modifier.width(4.dp))
            GhostButton("Today") { anchor = state.today }
            Spacer(Modifier.width(4.dp))
            NavButton(left = false) {
                anchor = when (view) {
                    CalendarView.Week -> addDays(anchor, 7)
                    CalendarView.Month -> shiftMonth(anchorDate, 1)
                }
            }
            Spacer(Modifier.width(16.dp))
            Segmented(CalendarView.entries.map { it.label }, view.label) { label ->
                view = CalendarView.entries.first { it.label == label }
            }
        }
        Spacer(Modifier.height(20.dp))
        when (view) {
            CalendarView.Week -> WeekView(state, weekStart, byDay, compact, onOpenTask)
            CalendarView.Month -> MonthView(state, anchorDate, byDay, compact, onOpenTask)
        }
    }
}

private fun shiftMonth(from: CalendarDate, by: Int): String {
    val index = from.year * 12 + (from.month - 1) + by
    return isoDate(index / 12, index % 12 + 1, 1)
}

@Composable
private fun NavButton(left: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        Modifier.size(38.dp).clip(shape).border(1.dp, BorderColor, shape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        ChevronIcon(left = left, color = TextPrimary, dimension = 14.dp)
    }
}

@Composable
internal fun DayHeading(day: String, today: String) {
    val isToday = day == today
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(WEEKDAY_ABBREVIATIONS[weekdayIndex(day)], color = if (isToday) AccentColor else TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(6.dp))
        DayNumber(parseDate(day).day, isToday)
    }
}

@Composable
internal fun DayNumber(day: Int, isToday: Boolean) {
    Box(
        Modifier.size(24.dp).clip(CircleShape).background(if (isToday) AccentColor else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Text("$day", color = if (isToday) Color.White else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun DayTasks(state: AppState, tasks: List<Task>, limit: Int, onOpenTask: (Task) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        tasks.take(limit).forEach { TaskChip(state, it, onOpenTask) }
        if (tasks.size > limit) Text("+${tasks.size - limit} more", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
    }
}

@Composable
private fun TaskChip(state: AppState, task: Task, onOpenTask: (Task) -> Unit) {
    val color = paletteColor(state.colorIndexOf(task))
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = if (task.isCompleted) 0.06f else 0.14f))
            .clickable { onOpenTask(task) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (state.isBlocked(task)) LockIcon(DangerColor, 11.dp) else Dot(color, 6.dp)
        Spacer(Modifier.width(6.dp))
        Text(
            task.title,
            color = if (task.isCompleted) TextMuted else TextPrimary,
            fontSize = 12.sp,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
