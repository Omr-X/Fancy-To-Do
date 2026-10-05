package net.omar.fancytodo.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.model.Area
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.model.Topic
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.ui.components.ColorSquare
import net.omar.fancytodo.ui.components.EmptyState
import net.omar.fancytodo.ui.components.LinkText
import net.omar.fancytodo.ui.components.MetaIcon
import net.omar.fancytodo.ui.components.MetaKind
import net.omar.fancytodo.ui.components.ScreenScaffold
import net.omar.fancytodo.ui.navigation.Page
import net.omar.fancytodo.ui.navigation.PageIcon
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.PanelColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

@Composable
internal fun TasksScreen(
    state: AppState,
    compact: Boolean,
    areaId: String?,
    topicId: String?,
    onSelectArea: (String?) -> Unit,
    onSelectTopic: (String?) -> Unit,
    onOpenTask: (Task) -> Unit
) {
    val area = areaId?.let { id -> state.areas.firstOrNull { it.id == id } }
    val topic = topicId?.let { id -> state.topics.firstOrNull { it.id == id } }

    ScreenScaffold(compact) {
        Breadcrumbs(area, topic, onSelectArea, onSelectTopic)
        Spacer(Modifier.height(24.dp))
        when {
            area == null -> AreaGrid(state, compact, onSelectArea)
            topic == null -> TopicGrid(state, area, compact, onSelectTopic)
            else -> TopicTasks(state, topic, onOpenTask)
        }
    }
}

@Composable
private fun Breadcrumbs(area: Area?, topic: Topic?, onSelectArea: (String?) -> Unit, onSelectTopic: (String?) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Crumb("Tasks", area == null) { onSelectArea(null) }
        if (area != null) {
            CrumbSep()
            Crumb(area.title, topic == null) { onSelectTopic(null) }
        }
        if (topic != null) {
            CrumbSep()
            Crumb(topic.title, true, null)
        }
    }
}

@Composable
private fun Crumb(text: String, active: Boolean, onClick: (() -> Unit)?) {
    Text(
        text,
        color = if (active) TextPrimary else TextSecondary,
        fontSize = if (active) 26.sp else 20.sp,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        modifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
    )
}

@Composable
private fun CrumbSep() {
    Text("  /  ", color = TextMuted, fontSize = 20.sp)
}

@Composable
private fun AreaGrid(state: AppState, compact: Boolean, onSelect: (String?) -> Unit) {
    if (state.areas.isEmpty()) {
        EmptyState("No areas yet", "Tap the + button to create your first area, like School, Gym or a hobby.")
        return
    }
    ItemGrid(state.areas, if (compact) 2 else 3) { area ->
        GridBox(area.title, area.color, { state.setAreaColor(area, it) }, onClick = { onSelect(area.id) }) {
            CountLabel(MetaKind.Topic, state.topicsOf(area.id).size, "topic")
            Spacer(Modifier.width(14.dp))
            CountLabel(null, state.tasksInArea(area.id), "task")
        }
    }
}

@Composable
private fun TopicGrid(state: AppState, area: Area, compact: Boolean, onSelect: (String?) -> Unit) {
    val topics = state.topicsOf(area.id)
    if (topics.isEmpty()) {
        EmptyState("No topics yet", "Tap the + button to add a topic inside ${area.title}.")
        return
    }
    ItemGrid(topics, if (compact) 2 else 3) { topic ->
        GridBox(topic.title, state.topicColorIndex(topic), { state.setTopicColor(topic, it) }, onClick = { onSelect(topic.id) }) {
            CountLabel(null, state.tasksInTopic(topic.id), "task")
        }
    }
}

@Composable
private fun TopicTasks(state: AppState, topic: Topic, onOpenTask: (Task) -> Unit) {
    var showCompleted by remember(topic.id) { mutableStateOf(false) }
    var sort by remember { mutableStateOf(TaskSort.Due) }
    val list = state.tasks.filter { it.topicId == topic.id }
        .sortedWith(compareBy<Task> { it.isCompleted }.then(sort.comparator(state)))
    if (list.isEmpty()) {
        EmptyState("No tasks yet", "Tap the + button to add the first task in ${topic.title}.")
        return
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        SortMenu(sort) { sort = it }
    }
    Spacer(Modifier.height(8.dp))
    val completed = list.count { it.isCompleted }
    val visible = if (showCompleted) list else list.filter { !it.isCompleted }
    if (visible.isEmpty()) {
        Text("All tasks here are done.", color = TextSecondary, fontSize = 14.sp)
    } else {
        TaskTree(state, visible, onOpenTask)
    }
    if (completed > 0) {
        Spacer(Modifier.height(14.dp))
        LinkText(if (showCompleted) "Hide completed tasks" else "See completed tasks ($completed)", TextSecondary) {
            showCompleted = !showCompleted
        }
    }
}

@Composable
private fun <T> ItemGrid(items: List<T>, columns: Int, spacing: androidx.compose.ui.unit.Dp = 12.dp, item: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                row.forEach { Box(Modifier.weight(1f)) { item(it) } }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun GridBox(title: String, color: Int, onColor: (Int) -> Unit, onClick: () -> Unit, stats: @Composable () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier.fillMaxWidth().aspectRatio(1.3f).clip(shape).background(PanelColor).border(1.dp, BorderColor, shape)
            .clickable(onClick = onClick).padding(16.dp)
    ) {
        // Click the color square to recolor without opening the box.
        ColorSquare(color, onColor)
        Spacer(Modifier.weight(1f))
        Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) { stats() }
    }
}

// "3 topics" / "1 task" with its icon; a null kind uses the checklist icon for tasks.
@Composable
private fun CountLabel(kind: MetaKind?, count: Int, noun: String) {
    if (kind != null) MetaIcon(kind, TextSecondary, 13.dp) else PageIcon(Page.Tasks, TextSecondary, 14.dp)
    Spacer(Modifier.width(6.dp))
    Text("$count $noun${if (count == 1) "" else "s"}", color = TextSecondary, fontSize = 12.sp)
}
