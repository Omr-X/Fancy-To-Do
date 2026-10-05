package net.omar.fancytodo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import net.omar.fancytodo.auth.UserProfile
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.request.NamePromptRequest
import net.omar.fancytodo.request.ScheduleRequest
import net.omar.fancytodo.request.TaskEditorRequest
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.state.SessionState
import net.omar.fancytodo.time.MINUTES_PER_DAY
import net.omar.fancytodo.ui.auth.AuthDialog
import net.omar.fancytodo.ui.auth.AuthMode
import net.omar.fancytodo.ui.auth.LogInPrompt
import net.omar.fancytodo.ui.background.DotGrid
import net.omar.fancytodo.ui.background.LocalDotGrid
import net.omar.fancytodo.ui.background.dotFieldHost
import net.omar.fancytodo.ui.background.trackDotGridPointer
import net.omar.fancytodo.ui.calendar.CalendarScreen
import net.omar.fancytodo.ui.calendar.DayCalendar
import net.omar.fancytodo.ui.dashboard.DashboardScreen
import net.omar.fancytodo.ui.editor.TaskEditor
import net.omar.fancytodo.ui.home.HomeScreen
import net.omar.fancytodo.ui.navigation.BottomNav
import net.omar.fancytodo.ui.navigation.Page
import net.omar.fancytodo.ui.navigation.Sidebar
import net.omar.fancytodo.ui.scheduling.DEFAULT_BLOCK_MINUTES
import net.omar.fancytodo.ui.scheduling.LocalTaskDrag
import net.omar.fancytodo.ui.scheduling.TaskDrag
import net.omar.fancytodo.ui.scheduling.TaskDragGhost
import net.omar.fancytodo.ui.scheduling.TaskPickerDialog
import net.omar.fancytodo.ui.settings.SettingsScreen
import net.omar.fancytodo.ui.shell.AddTaskButton
import net.omar.fancytodo.ui.shell.DayCalendarOverlay
import net.omar.fancytodo.ui.shell.DayCalendarToggle
import net.omar.fancytodo.ui.shell.DayCalendarWidth
import net.omar.fancytodo.ui.shell.ErrorToast
import net.omar.fancytodo.ui.shell.MainPanelShape
import net.omar.fancytodo.ui.tasks.NamePromptDialog
import net.omar.fancytodo.ui.tasks.TasksScreen
import net.omar.fancytodo.ui.theme.AppTheme
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.SidebarColor
import net.omar.fancytodo.ui.theme.StrongBorderColor
import net.omar.fancytodo.ui.theme.TextSecondary

/** Root of the app. Works as a guest; logging in (optional) switches to that account's data. */
@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val session = remember { SessionState(scope) }
    val dotGrid = remember { DotGrid() }
    LaunchedEffect(Unit) { session.restore() }
    // Which form of the log-in dialog is open, if any.
    var authMode by remember { mutableStateOf<AuthMode?>(null) }
    var logInPromptDismissed by remember { mutableStateOf(false) }
    LaunchedEffect(session.user) { if (session.user != null) authMode = null }

    AppTheme {
        CompositionLocalProvider(LocalDotGrid provides dotGrid) {
            Box(Modifier.fillMaxSize().background(BackgroundColor).trackDotGridPointer(dotGrid)) {
                if (session.checking) {
                    LoadingMessage()
                } else {
                    // Logging in or out gets a fresh AppState, and so that account's data (or the guest data).
                    key(session.token) {
                        MainContent(
                            session,
                            showLogInPrompt = session.user == null && !logInPromptDismissed,
                            onOpenAuth = { authMode = it },
                            onDismissLogInPrompt = { logInPromptDismissed = true }
                        )
                    }
                }
                val mode = authMode
                if (mode != null && session.user == null) {
                    AuthDialog(
                        mode,
                        session,
                        onSwitchMode = { authMode = if (mode == AuthMode.LogIn) AuthMode.CreateAccount else AuthMode.LogIn },
                        onClose = { authMode = null }
                    )
                }
            }
        }
    }
}

/** Navigation, the main panel, the day calendar and every dialog. */
@Composable
private fun MainContent(
    session: SessionState,
    showLogInPrompt: Boolean,
    onOpenAuth: (AuthMode) -> Unit,
    onDismissLogInPrompt: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val state = remember { AppState(scope, session.token, session::onSessionExpired) }
    var page by remember { mutableStateOf(Page.Home) }
    var editorRequest by remember { mutableStateOf<TaskEditorRequest?>(null) }
    var namePromptRequest by remember { mutableStateOf<NamePromptRequest?>(null) }
    var scheduleRequest by remember { mutableStateOf<ScheduleRequest?>(null) }
    // Where the Tasks page is drilled into.
    var selectedAreaId by remember { mutableStateOf<String?>(null) }
    var selectedTopicId by remember { mutableStateOf<String?>(null) }
    val taskDrag = remember { TaskDrag() }
    taskDrag.onDrop = { task, date, start ->
        state.addBlock(task.id, date, start, minOf(start + DEFAULT_BLOCK_MINUTES, MINUTES_PER_DAY))
    }

    LaunchedEffect(Unit) {
        state.refresh()
        while (true) {
            delay(60_000)
            state.syncToday()
            // Time inside a running block keeps adding to its task.
            state.syncWorkedTime()
        }
    }

    val openTask = { task: Task -> editorRequest = TaskEditorRequest(task) }
    val selectArea = { id: String? -> selectedAreaId = id; selectedTopicId = null }
    val selectTopic = { id: String? -> selectedTopicId = id }
    val showAddButton = page == Page.Home || page == Page.Tasks
    // The + button creates what fits the page: a task, or an area/topic while browsing the Tasks page.
    val onAdd: () -> Unit = {
        when (page) {
            Page.Home -> editorRequest = TaskEditorRequest(null, defaultDue = state.today)
            Page.Tasks -> {
                val areaId = selectedAreaId
                when {
                    areaId == null -> namePromptRequest = NamePromptRequest.NewArea
                    selectedTopicId == null -> {
                        val areaTitle = state.areas.firstOrNull { it.id == areaId }?.title ?: ""
                        namePromptRequest = NamePromptRequest.NewTopic(areaId, areaTitle)
                    }
                    else -> editorRequest = TaskEditorRequest(null, defaultTopicId = selectedTopicId)
                }
            }
            else -> {}
        }
    }

    CompositionLocalProvider(LocalTaskDrag provides taskDrag) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val compact = maxWidth < 760.dp
            // The day calendar sits beside the page on wide screens and slides over it otherwise.
            val roomForDayCalendar = maxWidth >= 1260.dp
            // The user's toggle wins until the window crosses the size threshold again.
            var dayCalendarChoice by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(roomForDayCalendar) { dayCalendarChoice = null }
            val dayCalendarOpen = dayCalendarChoice ?: roomForDayCalendar
            val toggleDayCalendar = { dayCalendarChoice = !dayCalendarOpen }
            val dayCalendarAvailable = page != Page.Settings
            val dayCalendarInline = dayCalendarAvailable && dayCalendarOpen && roomForDayCalendar
            val dayCalendarOverlaid = dayCalendarAvailable && dayCalendarOpen && !roomForDayCalendar
            // Home has the toggle next to its clock; other pages get it in the top-right corner.
            val showCornerToggle = dayCalendarAvailable && page != Page.Home
            val overlayWidth = if (maxWidth < DayCalendarWidth + 48.dp) maxWidth - 48.dp else DayCalendarWidth
            val dayCalendar: @Composable (Modifier) -> Unit = { modifier ->
                DayCalendar(state, openTask, { scheduleRequest = it }, modifier)
            }
            val pageArea: @Composable BoxScope.(Boolean) -> Unit = { isCompact ->
                PageContent(
                    page, state, isCompact, selectedAreaId, selectedTopicId, selectArea, selectTopic, openTask,
                    dayCalendarOpen, toggleDayCalendar, session.user, session::logOut, onOpenAuth
                )
                if (showAddButton) AddTaskButton(onAdd)
                if (showCornerToggle) {
                    DayCalendarToggle(dayCalendarOpen, toggleDayCalendar, Modifier.align(Alignment.TopEnd).padding(top = 22.dp, end = 22.dp))
                }
                if (dayCalendarOverlaid) DayCalendarOverlay(overlayWidth, toggleDayCalendar, dayCalendar)
            }

            if (compact) {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f).fillMaxWidth().dotFieldHost()) { pageArea(true) }
                    BottomNav(page) { page = it }
                }
            } else {
                Row(Modifier.fillMaxSize().background(SidebarColor)) {
                    Sidebar(
                        page,
                        onSelect = { page = it },
                        notice = if (showLogInPrompt) {
                            { LogInPrompt(onLogIn = { onOpenAuth(AuthMode.LogIn) }, onDismiss = onDismissLogInPrompt) }
                        } else null
                    )
                    Row(
                        Modifier.weight(1f).fillMaxHeight()
                            .padding(top = 10.dp)
                            .clip(MainPanelShape).background(BackgroundColor).border(1.dp, StrongBorderColor, MainPanelShape)
                            .dotFieldHost()
                    ) {
                        Box(Modifier.weight(1f).fillMaxHeight()) { pageArea(false) }
                        if (dayCalendarInline) {
                            Box(Modifier.width(1.dp).fillMaxHeight().background(BorderColor))
                            dayCalendar(Modifier.width(DayCalendarWidth).fillMaxHeight())
                        }
                    }
                }
            }

            // Dialogs and overlays, above everything else.
            editorRequest?.let { request -> TaskEditor(state, request) { editorRequest = null } }
            namePromptRequest?.let { request ->
                val areaColorIndex = (request as? NamePromptRequest.NewTopic)?.let { t -> state.areas.firstOrNull { it.id == t.areaId }?.color }
                NamePromptDialog(request, areaColorIndex ?: state.nextAreaColor(), onCancel = { namePromptRequest = null }) { name, color ->
                    // Stay on the current list; the new area/topic just appears in it.
                    when (request) {
                        is NamePromptRequest.NewArea -> state.addArea(name, color)
                        // Left on the area's color, the topic keeps following the area's color.
                        is NamePromptRequest.NewTopic -> state.addTopic(request.areaId, name, color.takeIf { it != areaColorIndex })
                    }
                    namePromptRequest = null
                }
            }
            scheduleRequest?.let { range ->
                TaskPickerDialog(state, range, onCancel = { scheduleRequest = null }) { task ->
                    state.addBlock(task.id, range.date, range.start, range.end)
                    scheduleRequest = null
                }
            }
            ErrorToast(state)
            TaskDragGhost(state)
        }
    }
}

@Composable
private fun PageContent(
    page: Page,
    state: AppState,
    compact: Boolean,
    selectedAreaId: String?,
    selectedTopicId: String?,
    onSelectArea: (String?) -> Unit,
    onSelectTopic: (String?) -> Unit,
    onOpenTask: (Task) -> Unit,
    dayCalendarOpen: Boolean,
    onToggleDayCalendar: () -> Unit,
    user: UserProfile?,
    onLogOut: () -> Unit,
    onOpenAuth: (AuthMode) -> Unit
) {
    if (state.loading) {
        LoadingMessage()
        return
    }
    when (page) {
        Page.Home -> HomeScreen(state, compact, onOpenTask, dayCalendarOpen, onToggleDayCalendar)
        Page.Tasks -> TasksScreen(state, compact, selectedAreaId, selectedTopicId, onSelectArea, onSelectTopic, onOpenTask)
        Page.Calendar -> CalendarScreen(state, compact, onOpenTask)
        Page.Dashboard -> DashboardScreen(state, compact)
        Page.Settings -> SettingsScreen(user, compact, onLogOut, onOpenAuth)
    }
}

@Composable
private fun LoadingMessage() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading...", color = TextSecondary, fontSize = 15.sp)
    }
}
