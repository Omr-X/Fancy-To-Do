package net.omar.fancytodo.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.omar.fancytodo.model.Area
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.model.Topic
import net.omar.fancytodo.request.TaskEditorRequest
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.addDays
import net.omar.fancytodo.time.dueLabel
import net.omar.fancytodo.ui.components.CloseIcon
import net.omar.fancytodo.ui.components.Dot
import net.omar.fancytodo.ui.components.LinkText
import net.omar.fancytodo.ui.components.LockIcon
import net.omar.fancytodo.ui.components.MetaIcon
import net.omar.fancytodo.ui.components.MetaKind
import net.omar.fancytodo.ui.components.SmallButton
import net.omar.fancytodo.ui.navigation.Page
import net.omar.fancytodo.ui.navigation.PageIcon
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.DangerColor
import net.omar.fancytodo.ui.theme.ErrorSurfaceColor
import net.omar.fancytodo.ui.theme.PanelColor
import net.omar.fancytodo.ui.theme.RaisedPanelColor
import net.omar.fancytodo.ui.theme.ScrimColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.TitleTracking
import net.omar.fancytodo.ui.theme.paletteColor

/**
 * Compact create/edit modal: breadcrumb, title and description on top, the task's properties as a
 * row of small chips (each opens its own menu), and the actions in a footer bar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TaskEditor(state: AppState, request: TaskEditorRequest, onClose: () -> Unit) {
    val task = request.task
    var title by remember { mutableStateOf(task?.title ?: "") }
    var areaId by remember {
        mutableStateOf(
            task?.let { state.areaOf(it)?.id }
                ?: request.defaultTopicId?.let { tid -> state.topics.firstOrNull { it.id == tid }?.areaId }
                ?: state.areas.firstOrNull()?.id
        )
    }
    var topicId by remember { mutableStateOf(task?.topicId ?: request.defaultTopicId ?: state.topicsOf(areaId).firstOrNull()?.id) }
    var due by remember { mutableStateOf(if (task != null) task.dueDate else request.defaultDue) }
    var estimateText by remember { mutableStateOf(task?.estimatedHours?.let { formatHoursInput(it) } ?: "") }
    var notes by remember { mutableStateOf(task?.description ?: "") }
    var parentId by remember { mutableStateOf(task?.parentTaskId) }
    var blockers by remember { mutableStateOf(task?.blockedByIds?.toList() ?: emptyList()) }
    var creating by remember { mutableStateOf<Creating?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    var missing by remember { mutableStateOf<List<String>>(emptyList()) }
    val done = task?.isCompleted ?: false
    val shape = RoundedCornerShape(14.dp)

    val area = state.areas.firstOrNull { it.id == areaId }
    val topic = state.topics.firstOrNull { it.id == topicId }
    // Parent and blockers come from the same topic, so they can be shown together on the Tasks page.
    val siblings = topicId?.let { tid -> state.tasks.filter { it.topicId == tid && it.id != task?.id } } ?: emptyList()
    val inheritedDue = parentId?.let { id -> state.taskById(id)?.let { state.effectiveDue(it) } }

    val resetRelations = {
        parentId = null
        blockers = emptyList()
    }

    val save: () -> Unit = {
        val chosenTopic = topicId
        missing = listOfNotNull(
            "A task name".takeIf { title.isBlank() },
            "An area".takeIf { areaId == null },
            "A topic".takeIf { chosenTopic == null }
        )
        if (missing.isEmpty() && chosenTopic != null) {
            state.saveTask(
                (task ?: Task(title = "", topicId = chosenTopic)).copy(
                    title = title.trim(),
                    topicId = chosenTopic,
                    parentTaskId = parentId,
                    blockedByIds = blockers.toMutableList(),
                    dueDate = due,
                    estimatedHours = parseHours(estimateText),
                    description = notes.ifBlank { null },
                    isCompleted = done
                ),
                task == null
            )
            onClose()
        }
    }

    Box(
        Modifier.fillMaxSize().background(ScrimColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(16.dp).widthIn(max = 640.dp).fillMaxWidth()
                .clip(shape).background(PanelColor).border(1.dp, BorderColor, shape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .verticalScroll(rememberScrollState())
        ) {
            // Breadcrumb + close
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(RoundedCornerShape(6.dp)).background(RaisedPanelColor).padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Dot(if (area != null) paletteColor(area.color) else TextMuted, 7.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(area?.title ?: "No area", color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                }
                Text("  ›  ", color = TextMuted, fontSize = 12.sp)
                Text(if (task == null) "New task" else "Edit task", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(28.dp).clip(RoundedCornerShape(7.dp)).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                    CloseIcon(TextSecondary, 11.dp)
                }
            }

            // Title + description
            Column(Modifier.padding(horizontal = 18.dp).padding(top = 10.dp)) {
                PlainField(
                    value = title,
                    onValueChange = { title = it.filter { c -> c != '\n' && c != '\r' } },
                    placeholder = "Task title",
                    style = TextStyle(color = TextPrimary, fontSize = 19.sp, fontWeight = FontWeight.Medium, letterSpacing = TitleTracking),
                    singleLine = true
                )
                Spacer(Modifier.height(6.dp))
                PlainField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Add description...",
                    style = TextStyle(color = TextSecondary, fontSize = 14.sp),
                    singleLine = false
                )
            }

            Spacer(Modifier.height(18.dp))

            // Property chips
            FlowRow(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChipMenu(
                    icon = { MetaIcon(MetaKind.Area, if (area != null) paletteColor(area.color) else TextSecondary, 13.dp) },
                    label = area?.title ?: "Area",
                    filled = area != null
                ) { close ->
                    state.areas.forEach { a ->
                        MenuRow(a.title, paletteColor(a.color), selected = a.id == areaId) {
                            if (a.id != areaId) {
                                areaId = a.id
                                topicId = state.topicsOf(a.id).firstOrNull()?.id
                                resetRelations()
                            }
                            close()
                        }
                    }
                    MenuRow("New area...", AccentColor, selected = false) { creating = Creating.Area; close() }
                }
                ChipMenu(
                    icon = { MetaIcon(MetaKind.Topic, if (topic != null) paletteColor(state.topicColorIndex(topic)) else TextSecondary, 13.dp) },
                    label = topic?.title ?: "Topic",
                    filled = topic != null
                ) { close ->
                    if (areaId == null) {
                        MenuRow("Pick an area first", TextMuted, selected = false) { close() }
                    } else {
                        state.topicsOf(areaId).forEach { t ->
                            MenuRow(t.title, paletteColor(state.topicColorIndex(t)), selected = t.id == topicId) {
                                if (t.id != topicId) resetRelations()
                                topicId = t.id
                                close()
                            }
                        }
                        MenuRow("New topic...", AccentColor, selected = false) { creating = Creating.Topic; close() }
                    }
                }
                ChipMenu(
                    icon = { MetaIcon(MetaKind.Due, if (due != null) TextPrimary else TextSecondary, 13.dp) },
                    label = due?.let { dueLabel(it, state.today) } ?: inheritedDue?.let { "${dueLabel(it, state.today)} (parent)" } ?: "Due date",
                    filled = due != null
                ) { close ->
                    MenuRow("Today", null, selected = due == state.today) { due = state.today; close() }
                    MenuRow("Tomorrow", null, selected = due == addDays(state.today, 1)) { due = addDays(state.today, 1); close() }
                    MenuRow(if (inheritedDue != null) "Same as parent" else "No date", null, selected = due == null) { due = null; close() }
                    Box(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        DatePicker(due, state.today, Modifier.width(250.dp)) { due = it; close() }
                    }
                }
                EstimateChip(estimateText) { new -> estimateText = new.filter { it.isDigit() || it == '.' } }
                ChipMenu(
                    icon = { PageIcon(Page.Tasks, if (parentId != null) TextPrimary else TextSecondary, 14.dp) },
                    label = parentId?.let { id -> state.taskById(id)?.title }?.let { "Parent: $it" } ?: "Parent task",
                    filled = parentId != null
                ) { close ->
                    val excluded = task?.let { descendantsOf(state, it.id) } ?: emptySet()
                    MenuRow("None", null, selected = parentId == null) { parentId = null; close() }
                    val options = siblings.filter { it.id !in excluded }
                    if (options.isEmpty()) MenuRow("No other tasks in this topic", TextMuted, selected = false) { close() }
                    options.forEach { t -> MenuRow(t.title, null, selected = t.id == parentId) { parentId = t.id; close() } }
                }
                ChipMenu(
                    icon = { LockIcon(if (blockers.isNotEmpty()) DangerColor else TextSecondary, 14.dp) },
                    label = when (blockers.size) {
                        0 -> "Blocked by"
                        1 -> "Blocked by: ${state.taskById(blockers[0])?.title ?: "deleted task"}"
                        else -> "Blocked by ${blockers.size} tasks"
                    },
                    filled = blockers.isNotEmpty()
                ) { close ->
                    // Stays open so several blockers can be toggled in one go.
                    if (siblings.isEmpty()) MenuRow("No other tasks in this topic", TextMuted, selected = false) { close() }
                    siblings.forEach { t ->
                        MenuRow(t.title, null, selected = t.id in blockers) {
                            blockers = if (t.id in blockers) blockers - t.id else blockers + t.id
                        }
                    }
                }
            }

            creating?.let { kind ->
                CreateRow(
                    kind = kind,
                    initialColor = if (kind == Creating.Area) state.nextAreaColor() else area?.color ?: 0,
                    onCancel = { creating = null }
                ) { name, color ->
                    if (kind == Creating.Area) {
                        areaId = state.addArea(name, color).id
                        topicId = null
                    } else {
                        val currentArea = areaId
                        if (currentArea != null) topicId = state.addTopic(currentArea, name, color.takeIf { it != area?.color }).id
                    }
                    resetRelations()
                    creating = null
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(BorderColor))

            // Footer
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (task != null) {
                    LinkText(if (confirmDelete) "Click again to delete" else "Delete", DangerColor) {
                        if (confirmDelete) {
                            state.deleteTask(task.id)
                            onClose()
                        } else {
                            confirmDelete = true
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                SmallButton("Cancel", primary = false, onClick = onClose)
                Spacer(Modifier.width(8.dp))
                SmallButton(if (task == null) "Create task" else "Save changes", primary = true, onClick = save)
            }
        }
        if (missing.isNotEmpty()) MissingInfoToast(missing, isNew = task == null) { missing = emptyList() }
    }
}

internal enum class Creating { Area, Topic }

/** Borderless text field with a placeholder, for the title/description at the top of the modal. */
@Composable
private fun PlainField(value: String, onValueChange: (String) -> Unit, placeholder: String, style: TextStyle, singleLine: Boolean) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        textStyle = LocalTextStyle.current.merge(style),
        cursorBrush = SolidColor(AccentColor),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) Text(placeholder, style = LocalTextStyle.current.merge(style).copy(color = TextMuted))
                inner()
            }
        }
    )
}

// Small toast along the bottom; goes away on its own after a few seconds or when clicked.
@Composable
private fun BoxScope.MissingInfoToast(missing: List<String>, isNew: Boolean, onDismiss: () -> Unit) {
    LaunchedEffect(missing) {
        delay(4_000)
        onDismiss()
    }
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier.align(Alignment.BottomCenter).padding(24.dp).widthIn(max = 420.dp)
            .clip(shape).background(ErrorSurfaceColor).border(1.dp, DangerColor.copy(alpha = 0.5f), shape)
            .clickable(onClick = onDismiss)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(DangerColor, 7.dp)
        Spacer(Modifier.width(10.dp))
        Text(
            (if (isNew) "Can't create yet. Missing: " else "Can't save yet. Missing: ") +
                missing.joinToString(", ") { it.lowercase() },
            color = TextPrimary,
            fontSize = 13.sp
        )
    }
}

private fun parseHours(text: String) = text.toDoubleOrNull()

private fun formatHoursInput(hours: Double) = if (hours == hours.toLong().toDouble()) hours.toLong().toString() else hours.toString()

// A task can't become the parent of its own subtasks (that would make a loop).
private fun descendantsOf(state: AppState, id: String): Set<String> {
    val out = mutableSetOf<String>()
    val queue = ArrayDeque(listOf(id))
    while (queue.isNotEmpty()) {
        state.childrenOf(queue.removeFirst()).forEach { if (out.add(it.id)) queue.addLast(it.id) }
    }
    return out
}
