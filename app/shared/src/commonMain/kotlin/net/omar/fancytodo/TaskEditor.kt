package net.omar.fancytodo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun TaskEditor(state: AppState, request: EditorRequest, onClose: () -> Unit) {
    val task = request.task
    var title by remember { mutableStateOf(task?.title ?: "") }
    var areaId by remember {
        mutableStateOf(
            task?.let { state.areaFor(it)?.id }
                ?: request.defaultTopicId?.let { tid -> state.topics.firstOrNull { it.id == tid }?.bookId }
                ?: state.areas.firstOrNull()?.id
        )
    }
    var topicId by remember { mutableStateOf(task?.chapterId ?: request.defaultTopicId ?: state.topicsOf(areaId).firstOrNull()?.id) }
    var due by remember { mutableStateOf(if (task != null) task.dueDate else request.defaultDue) }
    var estimateText by remember { mutableStateOf(task?.guessedTime?.let { formatHoursInput(it) } ?: "") }
    var notes by remember { mutableStateOf(task?.description ?: "") }
    val done = task?.isCompleted ?: false
    var calendarOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val tomorrow = addDays(state.today, 1)
    val shape = RoundedCornerShape(18.dp)

    val save: () -> Unit = {
        val topic = topicId
        if (title.isNotBlank() && topic != null) {
            state.saveTask(
                (task ?: Task(title = "", chapterId = topic)).copy(
                    title = title.trim(),
                    chapterId = topic,
                    dueDate = due,
                    guessedTime = parseHours(estimateText),
                    description = notes.ifBlank { null },
                    isCompleted = done
                ),
                task == null
            )
            onClose()
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color(0xCC000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(16.dp).widthIn(max = 560.dp).fillMaxWidth()
                .clip(shape).background(PanelColor).border(1.dp, BorderColor, shape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .verticalScroll(rememberScrollState())
                .padding(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionLabel(if (task == null) "New task" else "Task")
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(30.dp).clip(CircleShape).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                    CloseIcon(TextMid, 13.dp)
                }
            }
            Spacer(Modifier.height(10.dp))
            BasicTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = TextHi, fontSize = 26.sp, fontWeight = FontWeight.Bold),
                cursorBrush = SolidColor(AccentColor),
                decorationBox = { inner ->
                    Box {
                        if (title.isEmpty()) Text("Task name", color = TextLow, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        inner()
                    }
                }
            )
            Spacer(Modifier.height(18.dp))

            PropertyRow("Area") {
                AreaField(
                    state = state,
                    areaId = areaId,
                    onSelect = { id ->
                        areaId = id
                        topicId = state.topicsOf(id).firstOrNull()?.id
                    },
                    onCreate = { name ->
                        val created = state.addArea(name)
                        areaId = created.id
                        topicId = state.topicsOf(created.id).firstOrNull()?.id
                    }
                )
            }
            PropertyRow("Topic") {
                val currentArea = areaId
                if (currentArea == null) {
                    Text("Pick an area first", color = TextLow, fontSize = 15.sp)
                } else {
                    TopicField(
                        state = state,
                        areaId = currentArea,
                        topicId = topicId,
                        onSelect = { topicId = it },
                        onCreate = { name -> topicId = state.addTopic(currentArea, name).id }
                    )
                }
            }
            PropertyRow("Due") {
                DueField(
                    due = due,
                    today = state.today,
                    tomorrow = tomorrow,
                    calendarOpen = calendarOpen,
                    onPick = { due = it; calendarOpen = false },
                    onOpenCalendar = { calendarOpen = true }
                )
            }
            if (calendarOpen) {
                Spacer(Modifier.height(6.dp))
                DatePicker(due, state.today) {
                    due = it
                    calendarOpen = false
                }
                Spacer(Modifier.height(6.dp))
            }
            PropertyRow("Estimate") {
                BasicTextField(
                    value = estimateText,
                    onValueChange = { new -> estimateText = new.filter { it.isDigit() || it == '.' } },
                    textStyle = TextStyle(color = TextHi, fontSize = 15.sp),
                    cursorBrush = SolidColor(AccentColor),
                    decorationBox = { inner ->
                        Box {
                            if (estimateText.isEmpty()) Text("Empty (hours)", color = TextLow, fontSize = 15.sp)
                            inner()
                        }
                    }
                )
            }

            Spacer(Modifier.height(6.dp))
            Text("Notes", color = TextLow, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            InputField(notes, { notes = it }, "Add details, links, page numbers...", Modifier.fillMaxWidth(), singleLine = false, minLines = 4)
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PrimaryButton(if (task == null) "Create task" else "Save changes") { save() }
                Spacer(Modifier.width(10.dp))
                GhostButton("Cancel") { onClose() }
                Spacer(Modifier.weight(1f))
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
            }
            if (title.isBlank() || topicId == null) {
                Spacer(Modifier.height(12.dp))
                Text("Add a task name and pick a topic to save.", color = TextLow, fontSize = 13.sp)
            }
        }
    }
}

private fun parseHours(text: String) = text.toDoubleOrNull()

private fun formatHoursInput(hours: Double) = if (hours == hours.toLong().toDouble()) hours.toLong().toString() else hours.toString()

@Composable
private fun PropertyRow(label: String, content: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TextLow, fontSize = 14.sp, modifier = Modifier.width(88.dp))
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun AreaField(state: AppState, areaId: String?, onSelect: (String) -> Unit, onCreate: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val area = state.areas.firstOrNull { it.id == areaId }

    Box {
        Row(Modifier.clickable { expanded = true }, verticalAlignment = Alignment.CenterVertically) {
            if (area != null) {
                Dot(areaColor(area.color), 8.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(area?.title ?: "Empty", color = if (area != null) TextHi else TextLow, fontSize = 15.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            state.areas.forEach { a ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Dot(areaColor(a.color), 8.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(a.title)
                        }
                    },
                    onClick = { onSelect(a.id); expanded = false }
                )
            }
            DropdownMenuItem(text = { Text("+ New area", color = AccentColor) }, onClick = { expanded = false; creating = true })
        }
    }
    if (creating) {
        Spacer(Modifier.height(8.dp))
        InputField(
            draft, { draft = it }, "Area name", Modifier.fillMaxWidth(),
            onDone = {
                if (draft.isNotBlank()) onCreate(draft)
                draft = ""
                creating = false
            }
        )
    }
}

@Composable
private fun TopicField(state: AppState, areaId: String, topicId: String?, onSelect: (String) -> Unit, onCreate: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var creating by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    val topics = state.topicsOf(areaId)
    val topic = topics.firstOrNull { it.id == topicId }

    Box {
        Text(
            topic?.title ?: "Empty",
            color = if (topic != null) TextHi else TextLow,
            fontSize = 15.sp,
            modifier = Modifier.clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            topics.forEach { t ->
                DropdownMenuItem(text = { Text(t.title) }, onClick = { onSelect(t.id); expanded = false })
            }
            DropdownMenuItem(text = { Text("+ New topic", color = AccentColor) }, onClick = { expanded = false; creating = true })
        }
    }
    if (creating) {
        Spacer(Modifier.height(8.dp))
        InputField(
            draft, { draft = it }, "Topic name", Modifier.fillMaxWidth(),
            onDone = {
                if (draft.isNotBlank()) onCreate(draft)
                draft = ""
                creating = false
            }
        )
    }
}

@Composable
private fun DueField(due: String?, today: String, tomorrow: String, calendarOpen: Boolean, onPick: (String?) -> Unit, onOpenCalendar: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            due?.let { shortDate(it) } ?: "Empty",
            color = if (due != null) TextHi else TextLow,
            fontSize = 15.sp,
            modifier = Modifier.clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Today") }, onClick = { onPick(today); expanded = false })
            DropdownMenuItem(text = { Text("Tomorrow") }, onClick = { onPick(tomorrow); expanded = false })
            DropdownMenuItem(text = { Text(if (calendarOpen) "Hide calendar" else "Pick a date...") }, onClick = { expanded = false; onOpenCalendar() })
            DropdownMenuItem(text = { Text("No date") }, onClick = { onPick(null); expanded = false })
        }
    }
}

@Composable
private fun DatePicker(selected: String?, today: String, onSelect: (String) -> Unit) {
    val start = ymd(selected ?: today)
    var year by remember { mutableStateOf(start.y) }
    var month by remember { mutableStateOf(start.m) }
    val shape = RoundedCornerShape(12.dp)
    val offset = weekday(isoOf(year, month, 1))
    val length = daysInMonth(year, month)

    Column(Modifier.fillMaxWidth().clip(shape).background(PanelHiColor).border(1.dp, BorderColor, shape).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).clickable {
                    if (month == 1) {
                        month = 12
                        year -= 1
                    } else {
                        month -= 1
                    }
                },
                contentAlignment = Alignment.Center
            ) { ChevronIcon(left = true) }
            Text(
                "${MonthNames[month - 1]} $year",
                color = TextHi,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Box(
                Modifier.size(32.dp).clip(CircleShape).clickable {
                    if (month == 12) {
                        month = 1
                        year += 1
                    } else {
                        month += 1
                    }
                },
                contentAlignment = Alignment.Center
            ) { ChevronIcon(left = false) }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            WeekdayShort.forEach {
                Text(it.take(2), color = TextLow, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        for (week in 0 until (offset + length + 6) / 7) {
            Row(Modifier.fillMaxWidth()) {
                for (column in 0..6) {
                    val day = week * 7 + column - offset + 1
                    val valid = day in 1..length
                    val iso = isoOf(year, month, day)
                    val active = valid && iso == selected
                    Box(
                        Modifier.weight(1f).aspectRatio(1f).padding(2.dp).clip(CircleShape)
                            .background(if (active) AccentColor else Color.Transparent)
                            .then(if (valid) Modifier.clickable { onSelect(iso) } else Modifier),
                        contentAlignment = Alignment.Center
                    ) {
                        if (valid) {
                            Text(
                                "$day",
                                color = if (active) Color.White else if (iso == today) AccentColor else TextHi,
                                fontSize = 13.sp,
                                fontWeight = if (iso == today) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
