package net.omar.fancytodo.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.platform.currentTime
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.clockLabel
import net.omar.fancytodo.time.longDate
import net.omar.fancytodo.ui.components.ScreenScaffold
import net.omar.fancytodo.ui.shell.DayCalendarToggle
import net.omar.fancytodo.ui.tasks.SortMenu
import net.omar.fancytodo.ui.tasks.TaskLine
import net.omar.fancytodo.ui.tasks.TaskSort
import net.omar.fancytodo.ui.tasks.fitColumns
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.HeadingLineHeight
import net.omar.fancytodo.ui.theme.HeadingTracking
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.RaisedPanelColor
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.TitleTracking

@Composable
internal fun HomeScreen(state: AppState, compact: Boolean, onOpenTask: (Task) -> Unit, dayCalendarOpen: Boolean, onToggleDayCalendar: () -> Unit) {
    // Parent tasks are summaries of their subtasks, so only the subtasks are listed here.
    // Due Today still shows blocked tasks (they're due regardless); Tasks only shows ones you can act on.
    val dueToday = state.tasks.filter { !it.isCompleted && !state.isParent(it) && state.effectiveDue(it) == state.today }
    var sort by remember { mutableStateOf(TaskSort.Due) }
    val open = state.tasks.filter { !it.isCompleted && !state.isParent(it) && !state.isBlocked(it) }
        .sortedWith(sort.comparator(state))
    val sortControl: @Composable () -> Unit = { SortMenu(sort) { sort = it } }

    if (compact) {
        ScreenScaffold(true) {
            HomeHeader(dueToday.size, dayCalendarOpen, onToggleDayCalendar)
            Spacer(Modifier.height(20.dp))
            HomePanel("Habits", Modifier.fillMaxWidth(), scroll = false) { HabitList(state) }
            Spacer(Modifier.height(16.dp))
            HomePanel("Due Today", Modifier.fillMaxWidth(), dueToday.size, scroll = false) { TaskList(state, dueToday, "Nothing due today.", onOpenTask) }
            Spacer(Modifier.height(16.dp))
            HomePanel("Tasks", Modifier.fillMaxWidth(), open.size, scroll = false, action = sortControl) { TaskList(state, open, "No tasks yet.", onOpenTask) }
        }
    } else {
        Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 28.dp)) {
            HomeHeader(dueToday.size, dayCalendarOpen, onToggleDayCalendar)
            Spacer(Modifier.height(24.dp))
            // Habits run the full height on the left so they rarely need scrolling; on the right,
            // Due Today : Tasks keep their 1.25 : 2 height split.
            Row(Modifier.fillMaxWidth().weight(1f)) {
                HomePanel("Habits", Modifier.weight(1f).fillMaxHeight()) { HabitList(state) }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(3f).fillMaxHeight()) {
                    HomePanel("Due Today", Modifier.fillMaxWidth().weight(1.25f), dueToday.size) { TaskList(state, dueToday, "Nothing due today.", onOpenTask) }
                    Spacer(Modifier.height(20.dp))
                    HomePanel("Tasks", Modifier.fillMaxWidth().weight(2f), open.size, bottomInset = 72.dp, action = sortControl) { TaskList(state, open, "No tasks yet.", onOpenTask) }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(dueCount: Int, dayCalendarOpen: Boolean, onToggleDayCalendar: () -> Unit) {
    var now by remember { mutableStateOf(currentTime()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000)
            now = currentTime()
        }
    }
    val greeting = when (now.hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    val message = when (dueCount) {
        0 -> "Nothing due today. Enjoy the free time."
        1 -> "You have 1 task due today."
        else -> "You have $dueCount tasks due today."
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(greeting, color = TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Medium, letterSpacing = HeadingTracking, lineHeight = HeadingLineHeight)
            Spacer(Modifier.height(4.dp))
            Text(message, color = TextSecondary, fontSize = 15.sp)
        }
        Spacer(Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    clockLabel(now.minuteOfDay),
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = LocalMonoFamily.current,
                    letterSpacing = (-0.02).em,
                    lineHeight = HeadingLineHeight
                )
                Spacer(Modifier.height(2.dp))
                Text(longDate(now.isoDate), color = TextSecondary, fontSize = 12.sp)
            }
            Spacer(Modifier.width(10.dp))
            DayCalendarToggle(dayCalendarOpen, onToggleDayCalendar)
        }
    }
}

@Composable
private fun HomePanel(
    title: String,
    modifier: Modifier,
    count: Int? = null,
    scroll: Boolean = true,
    bottomInset: Dp = 8.dp,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    // Solid page color: the dot grid shows around the blocks, not through them.
    Column(modifier.clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Medium, letterSpacing = TitleTracking, modifier = Modifier.weight(1f))
            action?.let {
                it()
                Spacer(Modifier.width(8.dp))
            }
            count?.let {
                Box(Modifier.clip(RoundedCornerShape(50)).background(RaisedPanelColor).padding(horizontal = 9.dp, vertical = 2.dp)) {
                    Text("$it", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
        Column(
            (if (scroll) Modifier.weight(1f).verticalScroll(rememberScrollState()) else Modifier)
                .fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = bottomInset),
            content = content
        )
    }
}

@Composable
private fun TaskList(state: AppState, list: List<Task>, emptyText: String, onOpenTask: (Task) -> Unit) {
    if (list.isEmpty()) {
        Text(emptyText, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        return
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Room left after padding, the checkbox and a minimum readable title.
        val columns = fitColumns(maxWidth, 220.dp)

        Column(Modifier.fillMaxWidth()) {
            list.forEachIndexed { i, task ->
                if (i > 0) Box(Modifier.fillMaxWidth().padding(start = 44.dp, end = 16.dp).height(1.dp).background(BorderColor.copy(alpha = 0.55f)))
                TaskLine(state, task, columns, onOpenTask)
            }
        }
    }
}
