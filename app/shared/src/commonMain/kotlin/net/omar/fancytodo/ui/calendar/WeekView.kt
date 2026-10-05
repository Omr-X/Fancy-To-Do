package net.omar.fancytodo.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.model.WorkBlock
import net.omar.fancytodo.platform.currentTime
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.addDays
import net.omar.fancytodo.time.clockLabel
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.NowLineColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.paletteColor

@Composable
internal fun WeekView(state: AppState, weekStart: String, byDay: Map<String, List<Task>>, compact: Boolean, onOpenTask: (Task) -> Unit) {
    val days = (0..6).map { addDays(weekStart, it) }
    val shape = RoundedCornerShape(12.dp)
    if (compact) {
        // Phones: one day per row instead of seven narrow columns.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            days.forEach { day ->
                Column(Modifier.fillMaxWidth().clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape).padding(12.dp)) {
                    DayHeading(day, state.today)
                    Spacer(Modifier.height(8.dp))
                    DayTasks(state, byDay[day].orEmpty(), Int.MAX_VALUE, onOpenTask)
                }
            }
        }
        return
    }
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val now = currentTime()
    // Start the grid an hour before now (or at 7:00 when looking at another week).
    LaunchedEffect(weekStart) {
        val firstHour = if (state.today in days) (now.hour - 1).coerceAtLeast(0) else 7
        scroll.scrollTo(with(density) { (WeekHour * firstHour).roundToPx() })
    }

    Column(Modifier.fillMaxWidth().clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape)) {
        // Day headings, with the tasks due that day in a strip on top (like all-day events).
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Spacer(Modifier.width(TimeGutter))
            days.forEach { day ->
                Box(Modifier.width(1.dp).fillMaxHeight().background(BorderColor))
                Column(
                    Modifier.weight(1f)
                        .background(if (day == state.today) AccentColor.copy(alpha = 0.06f) else Color.Transparent)
                        .padding(8.dp)
                ) {
                    DayHeading(day, state.today)
                    Spacer(Modifier.height(8.dp))
                    DayTasks(state, byDay[day].orEmpty(), 3, onOpenTask)
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        Box(Modifier.fillMaxWidth().height(560.dp).verticalScroll(scroll)) {
            Box(Modifier.fillMaxWidth().height(WeekHour * 24)) {
                // A line for every hour across the whole week, labelled in the gutter.
                for (hour in 0 until 24) {
                    if (hour > 0) Box(Modifier.offset(y = WeekHour * hour).fillMaxWidth().height(1.dp).background(BorderColor.copy(alpha = 0.6f)))
                    Text(
                        clockLabel(hour * 60),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = LocalMonoFamily.current,
                        modifier = Modifier.offset(y = WeekHour * hour).padding(start = 8.dp, top = 3.dp)
                    )
                }
                Row(Modifier.fillMaxSize().padding(start = TimeGutter)) {
                    days.forEach { day ->
                        Box(Modifier.width(1.dp).fillMaxHeight().background(BorderColor))
                        Box(
                            Modifier.weight(1f).fillMaxHeight()
                                .background(if (day == state.today) AccentColor.copy(alpha = 0.04f) else Color.Transparent)
                        ) {
                            state.blocksOn(day).forEach { block ->
                                state.taskById(block.taskId)?.let { WeekBlock(state, block, it, onOpenTask) }
                            }
                            if (day == state.today) {
                                Box(
                                    Modifier.offset(y = WeekHour * ((now.hour * 60 + now.minute) / 60f))
                                        .fillMaxWidth().height(2.dp).background(NowLineColor)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private val WeekHour = 48.dp
private val TimeGutter = 52.dp

@Composable
private fun WeekBlock(state: AppState, block: WorkBlock, task: Task, onOpenTask: (Task) -> Unit) {
    val color = paletteColor(state.colorIndexOf(task))
    val shape = RoundedCornerShape(5.dp)
    Row(
        Modifier.offset(y = WeekHour * (block.start / 60f)).padding(horizontal = 2.dp)
            .fillMaxWidth().height(WeekHour * ((block.end - block.start) / 60f))
            .clip(shape).background(color.copy(alpha = if (task.isCompleted) 0.1f else 0.24f))
            .clickable { onOpenTask(task) }
    ) {
        Box(Modifier.width(3.dp).fillMaxHeight().background(color))
        Column(Modifier.padding(horizontal = 5.dp, vertical = 3.dp)) {
            Text(task.title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (block.end - block.start >= 60) {
                Text("${clockLabel(block.start)} – ${clockLabel(block.end)}", color = TextSecondary, fontSize = 10.sp, fontFamily = LocalMonoFamily.current)
            }
        }
    }
}
