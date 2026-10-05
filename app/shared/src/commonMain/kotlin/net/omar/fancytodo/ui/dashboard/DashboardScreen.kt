package net.omar.fancytodo.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.addDays
import net.omar.fancytodo.time.hoursLabel
import net.omar.fancytodo.ui.components.Dot
import net.omar.fancytodo.ui.components.PageHeader
import net.omar.fancytodo.ui.components.ProgressBar
import net.omar.fancytodo.ui.components.ScreenScaffold
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.DangerColor
import net.omar.fancytodo.ui.theme.HeadingLineHeight
import net.omar.fancytodo.ui.theme.HeadingTracking
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.SuccessColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.TitleTracking
import net.omar.fancytodo.ui.theme.WarningColor
import net.omar.fancytodo.ui.theme.paletteColor

@Composable
internal fun DashboardScreen(state: AppState, compact: Boolean) {
    // Parent tasks just summarise their subtasks, so counts use the actual work items.
    val work = state.tasks.filter { !state.isParent(it) }
    val done = work.count { it.isCompleted }
    val started = work.count { !it.isCompleted && state.workedHours(it) > 0.0 }
    val pending = work.count { !it.isCompleted && state.workedHours(it) <= 0.0 }
    val open = work.filter { !it.isCompleted }

    ScreenScaffold(compact) {
        PageHeader("Dashboard", "Where your tasks and habits stand")
        Spacer(Modifier.height(24.dp))

        // Task status
        TileRow(
            compact,
            { StatTile("Pending", "$pending", "Not started yet", TextSecondary, it) },
            { StatTile("Started", "$started", "Time logged, not done", AccentColor, it) },
            { StatTile("Done", "$done", "Completed", SuccessColor, it) }
        )
        Spacer(Modifier.height(12.dp))
        StatusBar(listOf(pending to TextMuted, started to AccentColor, done to SuccessColor))

        Spacer(Modifier.height(28.dp))
        DashCard("Habits", "Last $HABIT_HISTORY_DAYS days") { HabitActivityGrid(state) }

        Spacer(Modifier.height(28.dp))
        SectionHeading("Overview")
        Spacer(Modifier.height(12.dp))
        val overdue = open.count { t -> state.effectiveDue(t)?.let { it < state.today } == true }
        val dueThisWeek = open.count { t -> state.effectiveDue(t)?.let { it >= state.today && it <= addDays(state.today, 6) } == true }
        val blocked = open.count { state.isBlocked(it) }
        val rate = if (work.isEmpty()) 0 else (done * 100f / work.size).roundToInt()
        val estimated = open.sumOf { it.estimatedHours ?: 0.0 }
        val logged = state.tasks.sumOf { state.workedHours(it) }
        val habitsToday = state.habits.count { it.completions[state.today] == true }
        val bestStreak = state.habits.maxByOrNull { state.streak(it) }
        TileRow(
            compact,
            { StatTile("Areas", "${state.areas.size}", "${state.topics.size} topics", TextSecondary, it) },
            { StatTile("Tasks", "${work.size}", "${open.size} still open", TextSecondary, it) },
            { StatTile("Completion", "$rate%", "of all tasks", SuccessColor, it) },
            { StatTile("Habits today", "$habitsToday/${state.habits.size}", "checked off", AccentColor, it) }
        )
        Spacer(Modifier.height(12.dp))
        val streakDays = bestStreak?.let { state.streak(it) } ?: 0
        TileRow(
            compact,
            { StatTile("Overdue", "$overdue", "past their due date", if (overdue > 0) DangerColor else TextSecondary, it) },
            { StatTile("Due this week", "$dueThisWeek", "next 7 days", WarningColor, it) },
            { StatTile("Blocked", "$blocked", "waiting on another task", if (blocked > 0) DangerColor else TextSecondary, it) },
            { StatTile("Best streak", "${streakDays}d", bestStreak?.takeIf { streakDays > 0 }?.title ?: "no streak yet", AccentColor, it) }
        )
        Spacer(Modifier.height(12.dp))
        TileRow(
            compact,
            { StatTile("Estimated work left", hoursLabel(estimated), "on open tasks", TextSecondary, it) },
            { StatTile("Time logged", hoursLabel(logged), "across all tasks", TextSecondary, it) }
        )

        Spacer(Modifier.height(28.dp))
        DashCard("Tasks by area", "Done out of total") { AreaBreakdown(state, work) }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(text, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Medium, letterSpacing = TitleTracking)
}

/** Equal-width tiles side by side (two per row on phones). Each tile gets its width modifier. */
@Composable
private fun TileRow(compact: Boolean, vararg tiles: @Composable (Modifier) -> Unit) {
    val perRow = if (compact) 2 else tiles.size
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.toList().chunked(perRow).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { tile -> tile(Modifier.weight(1f)) }
                repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, caption: String, accent: Color, modifier: Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Column(modifier.clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Dot(accent, 7.dp)
            Spacer(Modifier.width(8.dp))
            Text(label, color = TextSecondary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(10.dp))
        Text(value, color = TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Medium, letterSpacing = HeadingTracking, lineHeight = HeadingLineHeight)
        Spacer(Modifier.height(4.dp))
        Text(caption, color = TextMuted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** One bar split by the share of each count. */
@Composable
private fun StatusBar(parts: List<Pair<Int, Color>>) {
    val total = parts.sumOf { it.first }
    Row(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(BorderColor)) {
        if (total > 0) {
            parts.filter { it.first > 0 }.forEach { (count, color) ->
                Box(Modifier.weight(count.toFloat()).height(6.dp).background(color))
            }
        }
    }
}

@Composable
private fun DashCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(Modifier.fillMaxWidth().clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = TitleTracking, modifier = Modifier.weight(1f))
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        Column(Modifier.fillMaxWidth().padding(18.dp), content = content)
    }
}

@Composable
private fun AreaBreakdown(state: AppState, work: List<Task>) {
    if (state.areas.isEmpty()) {
        Text("No areas yet.", color = TextSecondary, fontSize = 14.sp)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        state.areas.forEach { area ->
            val tasks = work.filter { state.areaOf(it)?.id == area.id }
            val doneCount = tasks.count { it.isCompleted }
            val color = paletteColor(area.color)
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Dot(color, 8.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(area.title, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("$doneCount/${tasks.size}", color = TextSecondary, fontSize = 12.sp, fontFamily = LocalMonoFamily.current)
                }
                Spacer(Modifier.height(8.dp))
                ProgressBar(if (tasks.isEmpty()) 0f else doneCount.toFloat() / tasks.size, color)
            }
        }
    }
}
