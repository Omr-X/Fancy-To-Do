package net.omar.fancytodo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal sealed class NamePromptRequest(val title: String, val placeholder: String) {
    object NewArea : NamePromptRequest("New area", "e.g. Gym")
    data class NewTopic(val areaId: String, val areaTitle: String) : NamePromptRequest("New topic in $areaTitle", "e.g. Push day")
}

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
private fun Breadcrumbs(area: Book?, topic: Chapter?, onSelectArea: (String?) -> Unit, onSelectTopic: (String?) -> Unit) {
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
        color = if (active) TextHi else TextMid,
        fontSize = if (active) 26.sp else 20.sp,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
        modifier = onClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
    )
}

@Composable
private fun CrumbSep() {
    Text("  /  ", color = TextLow, fontSize = 20.sp)
}

@Composable
private fun AreaGrid(state: AppState, compact: Boolean, onSelect: (String?) -> Unit) {
    if (state.areas.isEmpty()) {
        EmptyState("No areas yet", "Tap the + button to create your first area, like School, Gym or a hobby.")
        return
    }
    ItemGrid(state.areas, if (compact) 2 else 3) { area ->
        val open = state.tasks.count { !it.isCompleted && state.areaFor(it)?.id == area.id }
        GridBox(area.title, areaColor(area.color), "$open open") { onSelect(area.id) }
    }
}

@Composable
private fun TopicGrid(state: AppState, area: Book, compact: Boolean, onSelect: (String?) -> Unit) {
    val topics = state.topicsOf(area.id)
    if (topics.isEmpty()) {
        EmptyState("No topics yet", "Tap the + button to add a topic inside ${area.title}.")
        return
    }
    ItemGrid(topics, if (compact) 2 else 3) { topic ->
        val open = state.tasks.count { it.chapterId == topic.id && !it.isCompleted }
        GridBox(topic.title, areaColor(area.color), "$open open") { onSelect(topic.id) }
    }
}

@Composable
private fun TopicTasks(state: AppState, topic: Chapter, onOpenTask: (Task) -> Unit) {
    val list = state.tasks.filter { it.chapterId == topic.id }
        .sortedWith(compareBy({ it.isCompleted }, { it.dueDate == null }, { it.dueDate }))
    if (list.isEmpty()) {
        EmptyState("No tasks yet", "Tap the + button to add the first task in ${topic.title}.")
    } else {
        list.forEach {
            TaskRow(state, it, onOpenTask)
            Spacer(Modifier.height(8.dp))
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
private fun GridBox(title: String, color: Color, subtitle: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier.fillMaxWidth().aspectRatio(1.3f).clip(shape).background(PanelColor).border(1.dp, BorderColor, shape)
            .clickable(onClick = onClick).padding(16.dp)
    ) {
        Dot(color, 12.dp)
        Spacer(Modifier.weight(1f))
        Text(title, color = TextHi, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, color = TextMid, fontSize = 12.sp)
    }
}

@Composable
internal fun NamePromptDialog(request: NamePromptRequest, onCancel: () -> Unit, onCreate: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val shape = RoundedCornerShape(16.dp)
    val submit: () -> Unit = { if (text.isNotBlank()) onCreate(text) }

    Box(
        Modifier.fillMaxSize().background(Color(0xCC000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancel),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(16.dp).widthIn(max = 380.dp).fillMaxWidth()
                .clip(shape).background(PanelColor).border(1.dp, BorderColor, shape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .padding(24.dp)
        ) {
            Text(request.title, color = TextHi, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            InputField(text, { text = it }, request.placeholder, Modifier.fillMaxWidth(), onDone = submit)
            Spacer(Modifier.height(18.dp))
            Row {
                PrimaryButton("Create") { submit() }
                Spacer(Modifier.width(10.dp))
                GhostButton("Cancel", onClick = onCancel)
            }
        }
    }
}
