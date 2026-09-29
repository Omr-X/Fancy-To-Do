package net.omar.fancytodo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val state = remember { AppState(scope) }
    var page by remember { mutableStateOf(Page.Home) }
    var editor by remember { mutableStateOf<EditorRequest?>(null) }
    var namePrompt by remember { mutableStateOf<NamePromptRequest?>(null) }
    var taskAreaId by remember { mutableStateOf<String?>(null) }
    var taskTopicId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        state.refresh()
        while (true) {
            delay(60_000)
            state.syncToday()
        }
    }

    val openTask = { task: Task -> editor = EditorRequest(task) }
    val selectArea = { id: String? -> taskAreaId = id; taskTopicId = null }
    val selectTopic = { id: String? -> taskTopicId = id }
    val fabVisible = page == Page.Home || page == Page.Tasks
    val fabAction: () -> Unit = {
        when (page) {
            Page.Home -> editor = EditorRequest(null, defaultDue = state.today)
            Page.Tasks -> when {
                taskAreaId == null -> namePrompt = NamePromptRequest.NewArea
                taskTopicId == null -> {
                    val areaTitle = state.areas.firstOrNull { it.id == taskAreaId }?.title ?: ""
                    namePrompt = NamePromptRequest.NewTopic(taskAreaId!!, areaTitle)
                }
                else -> editor = EditorRequest(null, defaultTopicId = taskTopicId)
            }
            else -> {}
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme(primary = AccentColor, background = BgColor, surface = PanelColor, onBackground = TextHi, onSurface = TextHi)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize().background(BgColor)) {
            val compact = maxWidth < 760.dp
            val showAgenda = page == Page.Home && maxWidth >= 1260.dp

            Box(Modifier.fillMaxSize()) {
                if (compact) {
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            Screen(page, state, true, taskAreaId, taskTopicId, selectArea, selectTopic, openTask)
                            if (fabVisible) Fab(fabAction)
                        }
                        BottomNav(page) { page = it }
                    }
                } else {
                    Row(Modifier.fillMaxSize()) {
                        Sidebar(page) { page = it }
                        Box(Modifier.weight(1f).fillMaxHeight()) {
                            Screen(page, state, false, taskAreaId, taskTopicId, selectArea, selectTopic, openTask)
                            if (fabVisible) Fab(fabAction)
                        }
                        if (showAgenda) {
                            Box(Modifier.width(1.dp).fillMaxHeight().background(BorderColor))
                            DayCalendar(Modifier.width(340.dp).fillMaxHeight())
                        }
                    }
                }
                editor?.let { request -> TaskEditor(state, request) { editor = null } }
                namePrompt?.let { req ->
                    NamePromptDialog(req, onCancel = { namePrompt = null }) { name ->
                        when (req) {
                            is NamePromptRequest.NewArea -> taskAreaId = state.addArea(name).id
                            is NamePromptRequest.NewTopic -> taskTopicId = state.addTopic(req.areaId, name).id
                        }
                        namePrompt = null
                    }
                }
                ErrorToast(state)
            }
        }
    }
}

@Composable
private fun Screen(
    page: Page,
    state: AppState,
    compact: Boolean,
    taskAreaId: String?,
    taskTopicId: String?,
    onSelectArea: (String?) -> Unit,
    onSelectTopic: (String?) -> Unit,
    onOpenTask: (Task) -> Unit
) {
    if (state.loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...", color = TextMid, fontSize = 15.sp)
        }
    } else {
        when (page) {
            Page.Home -> HomeScreen(state, compact, onOpenTask)
            Page.Tasks -> TasksScreen(state, compact, taskAreaId, taskTopicId, onSelectArea, onSelectTopic, onOpenTask)
            Page.Calendar -> Box(Modifier.fillMaxSize())
            Page.Dashboard -> Box(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun BoxScope.Fab(onClick: () -> Unit) {
    Box(
        Modifier.align(Alignment.BottomEnd).padding(24.dp)
            .shadow(12.dp, CircleShape)
            .size(58.dp).clip(CircleShape).background(AccentColor).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        PlusIcon(Color.White, 24.dp)
    }
}

@Composable
private fun BoxScope.ErrorToast(state: AppState) {
    state.error?.let { message ->
        val shape = RoundedCornerShape(12.dp)
        Row(
            Modifier.align(Alignment.BottomCenter).padding(24.dp)
                .clip(shape).background(Color(0xFF2A1A1E)).border(1.dp, DangerColor.copy(alpha = 0.5f), shape)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(message, color = TextHi, fontSize = 14.sp)
            Spacer(Modifier.width(16.dp))
            LinkText("Retry", DangerColor) {
                state.dismissError()
                state.refresh()
            }
            LinkText("Dismiss", TextMid) { state.dismissError() }
        }
    }
}
