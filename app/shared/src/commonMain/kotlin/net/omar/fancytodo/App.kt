package net.omar.fancytodo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch

private val BackgroundColor = Color(0xFF0A0A0D)
private val SidebarColor = Color(0xFF050506)
private val CardColor = Color(0xFF18181C)
private val TextPrimary = Color(0xFFEDEDF0)
private val TextSecondary = Color(0xFFA0A0AC)
private val ErrorColor = Color(0xFFE05A5A)
private val NowLineColor = Color(0xFFE0554A)
private val DateBadgeColor = Color(0xFFC7DBF5)

@Composable
fun App() {
    var home by remember { mutableStateOf<Home?>(null) }
    var books by remember { mutableStateOf<Map<String, Book>>(emptyMap()) }
    var chapters by remember { mutableStateOf<Map<String, Chapter>>(emptyMap()) }
    var allTasks by remember { mutableStateOf<List<Task>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf("Home") }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskChapterId by remember { mutableStateOf("") }
    var newTaskDueDate by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskGuessedTime by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val client = remember {
        HttpClient(Js) {
            install(ContentNegotiation) { json() }
            defaultRequest { host = "localhost"; port = 8080 }
        }
    }

    suspend fun refresh() {
        try {
            home = client.get("home").body<Home>()
            val bookList = client.get("books").body<List<Book>>()
            books = bookList.associateBy { it.id }
            val chapterMap = mutableMapOf<String, Chapter>()
            bookList.forEach { book ->
                client.get("books/${book.id}/chapters").body<List<Chapter>>().forEach { chapterMap[it.id] = it }
            }
            chapters = chapterMap
            allTasks = client.get("tasks").body<List<Task>>()
            error = null
        } catch (e: Exception) {
            error = e.message
        }
    }

    LaunchedEffect(Unit) { refresh() }

    fun bookTitleFor(task: Task) = chapters[task.chapterId]?.bookId?.let { books[it]?.title } ?: ""

    fun toggleTask(task: Task) = scope.launch {
        client.put("tasks/${task.id}") {
            contentType(ContentType.Application.Json)
            setBody(task.copy(isCompleted = !task.isCompleted))
        }
        refresh()
    }

    fun toggleHabit(habit: Habit) = scope.launch {
        val updated = habit.completions + ("today" to !(habit.completions["today"] ?: false))
        client.put("habits/${habit.id}") {
            contentType(ContentType.Application.Json)
            setBody(habit.copy(completions = updated))
        }
        refresh()
    }

    fun createTask() = scope.launch {
        if (newTaskChapterId.isEmpty() || newTaskTitle.isBlank()) {
            error = "Pick a chapter and enter a title first"
            return@launch
        }
        client.post("chapters/$newTaskChapterId/tasks") {
            contentType(ContentType.Application.Json)
            setBody(
                Task(
                    chapterId = newTaskChapterId,
                    title = newTaskTitle,
                    description = newTaskDescription.ifBlank { null },
                    dueDate = newTaskDueDate.ifBlank { null },
                    guessedTime = newTaskGuessedTime.toDoubleOrNull()
                )
            )
        }
        newTaskTitle = ""
        newTaskChapterId = ""
        newTaskDueDate = ""
        newTaskDescription = ""
        newTaskGuessedTime = ""
        currentPage = "Home"
        refresh()
    }

    MaterialTheme(colorScheme = darkColorScheme(background = BackgroundColor, surface = CardColor)) {
        Row(Modifier.fillMaxSize().background(BackgroundColor)) {
            Sidebar(currentPage) { currentPage = it }
            when (currentPage) {
                "Home" -> {
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        Row(Modifier.fillMaxSize()) {
                            Column(Modifier.weight(1f).fillMaxHeight().padding(32.dp).verticalScroll(rememberScrollState())) {
                                error?.let { Text("Error: $it", color = ErrorColor) }
                                home?.let { h ->
                                    Row(Modifier.fillMaxWidth()) {
                                        Column(Modifier.weight(1f)) {
                                            SectionTitle("Habits")
                                            Spacer(Modifier.height(16.dp))
                                            h.habitsToday.forEach { habit ->
                                                HabitRow(habit) { toggleHabit(habit) }
                                                Spacer(Modifier.height(10.dp))
                                            }
                                        }
                                        Spacer(Modifier.width(48.dp))
                                        Column(Modifier.weight(1f)) {
                                            SectionTitle("Due Today")
                                            Spacer(Modifier.height(16.dp))
                                            h.tasksDueToday.forEach { task ->
                                                TaskRow(task, bookTitleFor(task), showDescription = false) { toggleTask(task) }
                                                Spacer(Modifier.height(10.dp))
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(48.dp))
                                    SectionTitle("Tasks")
                                    Spacer(Modifier.height(16.dp))
                                    h.tasksByPriority.forEach { task ->
                                        TaskRow(task, bookTitleFor(task), showDescription = true) { toggleTask(task) }
                                        Spacer(Modifier.height(10.dp))
                                    }
                                }
                            }
                            Box(Modifier.width(1.dp).fillMaxHeight().background(Color(0xFF1E1E24)))
                            DayCalendar(Modifier.width(380.dp).fillMaxHeight())
                        }
                        FloatingActionButton(
                            onClick = { currentPage = "NewTask" },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                            containerColor = CardColor,
                            contentColor = TextPrimary
                        ) {
                            Text("+", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                "Task" -> {
                    Column(Modifier.weight(1f).fillMaxHeight().padding(32.dp).verticalScroll(rememberScrollState())) {
                        SectionTitle("Task")
                        Spacer(Modifier.height(16.dp))
                        allTasks.forEach { task ->
                            TaskRow(task, bookTitleFor(task), showDescription = true) { toggleTask(task) }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
                "NewTask" -> {
                    NewTaskPage(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        chapters = chapters,
                        books = books,
                        title = newTaskTitle,
                        onTitleChange = { newTaskTitle = it },
                        chapterId = newTaskChapterId,
                        onChapterChange = { newTaskChapterId = it },
                        dueDate = newTaskDueDate,
                        onDueDateChange = { newTaskDueDate = it },
                        description = newTaskDescription,
                        onDescriptionChange = { newTaskDescription = it },
                        guessedTime = newTaskGuessedTime,
                        onGuessedTimeChange = { newTaskGuessedTime = it },
                        onCancel = { currentPage = "Home" },
                        onCreate = { createTask() }
                    )
                }
                else -> Box(Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun Sidebar(currentPage: String, onSelect: (String) -> Unit) {
    Column(Modifier.width(240.dp).fillMaxHeight().background(SidebarColor).padding(24.dp)) {
        Text("TaskMan", color = TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(40.dp))
        listOf("Home", "Task", "Calendar", "Dashboard").forEach { page ->
            Text(
                page,
                color = if (currentPage == page) TextPrimary else TextSecondary,
                fontSize = 18.sp,
                fontWeight = if (currentPage == page) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.clickable { onSelect(page) }
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun rowCheckboxColors() = CheckboxDefaults.colors(uncheckedColor = TextSecondary, checkedColor = TextPrimary)

@Composable
private fun HabitRow(habit: Habit, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(CardColor, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Checkbox(checked = habit.completions["today"] == true, onCheckedChange = { onToggle() }, colors = rowCheckboxColors())
        Spacer(Modifier.width(12.dp))
        Text(habit.title, color = TextPrimary, fontSize = 16.sp)
    }
}

@Composable
private fun TaskRow(task: Task, bookTitle: String, showDescription: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(CardColor, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 12.dp)) {
        Checkbox(checked = task.isCompleted, onCheckedChange = { onToggle() }, colors = rowCheckboxColors())
        Spacer(Modifier.width(12.dp))
        Text(task.title, color = TextPrimary, fontSize = 16.sp, modifier = Modifier.width(160.dp))
        Text(bookTitle, color = TextSecondary, fontSize = 16.sp, modifier = Modifier.width(140.dp))
        Text(formatDate(task.dueDate), color = TextSecondary, fontSize = 16.sp, modifier = Modifier.width(110.dp))
        if (showDescription) Text(task.description ?: "", color = TextSecondary, fontSize = 16.sp)
    }
}

@Composable
private fun DayCalendar(modifier: Modifier = Modifier, startHour: Int = 7, endHour: Int = 20) {
    val info = remember { currentNow() }
    val hourRowHeight = 60.dp
    val minutesFromStart = ((info.hour - startHour) * 60 + info.minute).coerceIn(0, (endHour - startHour) * 60)
    val nowOffsetDp = hourRowHeight * (minutesFromStart / 60f)

    Column(modifier.background(BackgroundColor)) {
        Column(Modifier.padding(16.dp)) {
            Text(info.offsetLabel, color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(info.dayAbbrev, color = Color(0xFF9BB8E8), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.size(44.dp).background(DateBadgeColor, RoundedCornerShape(22.dp)), contentAlignment = Alignment.Center) {
                    Text("${info.dayNumber}", color = Color(0xFF15202B), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Box(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Column {
                for (hour in startHour..endHour) {
                    Box(Modifier.fillMaxWidth().height(hourRowHeight)) {
                        HorizontalDivider(color = Color(0xFF2A2A30), modifier = Modifier.align(Alignment.TopStart).fillMaxWidth())
                        Text(
                            hour.toString().padStart(2, '0') + ":00",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp)
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth().offset(y = nowOffsetDp).align(Alignment.TopStart), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).background(NowLineColor, RoundedCornerShape(5.dp)))
                Box(Modifier.weight(1f).height(2.dp).background(NowLineColor))
            }
        }
    }
}

@Composable
private fun NewTaskPage(
    modifier: Modifier = Modifier,
    chapters: Map<String, Chapter>,
    books: Map<String, Book>,
    title: String,
    onTitleChange: (String) -> Unit,
    chapterId: String,
    onChapterChange: (String) -> Unit,
    dueDate: String,
    onDueDateChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    guessedTime: String,
    onGuessedTimeChange: (String) -> Unit,
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
    Column(modifier.padding(48.dp).verticalScroll(rememberScrollState())) {
        Text("← Back", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.clickable { onCancel() })
        Spacer(Modifier.height(24.dp))
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            textStyle = TextStyle(color = TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold),
            cursorBrush = SolidColor(TextPrimary),
            decorationBox = { inner ->
                Box {
                    if (title.isEmpty()) Text("New Task", color = TextSecondary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    inner()
                }
            }
        )
        Spacer(Modifier.height(28.dp))

        PropertyRow("\uD83D\uDCD8", "Chapter") {
            var expanded by remember { mutableStateOf(false) }
            Box {
                val selected = chapters[chapterId]
                val label = selected?.let { "${books[it.bookId]?.title ?: ""} — ${it.title}" } ?: "Empty"
                Text(
                    label,
                    color = if (selected == null) TextSecondary else TextPrimary,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { expanded = true }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    chapters.values.forEach { chapter ->
                        DropdownMenuItem(
                            text = { Text("${books[chapter.bookId]?.title ?: ""} — ${chapter.title}") },
                            onClick = { onChapterChange(chapter.id); expanded = false }
                        )
                    }
                }
            }
        }
        PropertyTextRow("\uD83D\uDCC5", "Due Date", dueDate, "YYYY-MM-DD", onDueDateChange)
        PropertyTextRow("\u23F1", "Guessed Time", guessedTime, "Hours", onGuessedTimeChange)
        PropertyTextRow("\uD83D\uDCDD", "Description", description, "Empty", onDescriptionChange)

        Spacer(Modifier.height(32.dp))
        Row(
            Modifier
                .background(CardColor, RoundedCornerShape(8.dp))
                .clickable { onCreate() }
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text("Create Task", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun PropertyRow(icon: String, label: String, valueContent: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 15.sp, modifier = Modifier.width(28.dp))
        Text(label, color = TextSecondary, fontSize = 15.sp, modifier = Modifier.width(140.dp))
        valueContent()
    }
}

@Composable
private fun PropertyTextRow(icon: String, label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    PropertyRow(icon, label) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
            cursorBrush = SolidColor(TextPrimary),
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) Text(placeholder, color = TextSecondary, fontSize = 16.sp)
                    inner()
                }
            }
        )
    }
}

private fun formatDate(date: String?): String {
    val parts = date?.split("-") ?: return ""
    return if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else date
}
