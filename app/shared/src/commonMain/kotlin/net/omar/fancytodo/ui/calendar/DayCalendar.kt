package net.omar.fancytodo.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.platform.currentTime
import net.omar.fancytodo.request.ScheduleRequest
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.MINUTES_PER_DAY
import net.omar.fancytodo.time.clockLabel
import net.omar.fancytodo.time.longDate
import net.omar.fancytodo.time.parseDate
import net.omar.fancytodo.ui.background.frostedDots
import net.omar.fancytodo.ui.scheduling.DEFAULT_BLOCK_MINUTES
import net.omar.fancytodo.ui.scheduling.DropTarget
import net.omar.fancytodo.ui.scheduling.LocalTaskDrag
import net.omar.fancytodo.ui.scheduling.SNAP_MINUTES
import net.omar.fancytodo.ui.scheduling.snap
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.NowLineColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.TitleTracking

private val HourHeight = 60.dp
internal val LabelGutter = 52.dp

/**
 * Today's timeline. Work blocks can be placed by dropping a task on it, or by holding on an empty
 * time and sliding to pick a range (then choosing the task). Blocks move by dragging, resize from
 * their bottom edge and are removed with their ×.
 */
@Composable
internal fun DayCalendar(
    state: AppState,
    onOpenTask: (Task) -> Unit,
    onPickRange: (ScheduleRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val info = remember { currentTime() }
    val date = state.today
    val density = LocalDensity.current
    val hourPx = with(density) { HourHeight.toPx() }
    val minutesNow = info.hour * 60 + info.minute
    val scroll = rememberScrollState()
    val drag = LocalTaskDrag.current

    // Open on the current time (with an hour of context above) instead of midnight.
    LaunchedEffect(Unit) {
        scroll.scrollTo(with(density) { (HourHeight * (minutesNow / 60f) - HourHeight).coerceAtLeast(0.dp).roundToPx() })
    }

    // Geometry for drops, kept outside of state so scrolling doesn't recompose anything.
    val geometry = remember { Geometry() }
    val target = remember(date, hourPx) {
        DropTarget(date, { geometry.viewport }) { rootY -> ((rootY - geometry.contentTop) / hourPx * 60).toInt() }
    }
    SideEffect { drag.target = target }
    DisposableEffect(target) { onDispose { if (drag.target === target) drag.target = null } }

    // Hold-and-slide selection, in minutes.
    var selection by remember { mutableStateOf<IntRange?>(null) }
    val hoverStart = drag.hoverStart()

    Box(modifier.background(BackgroundColor)) {
        Box(Modifier.matchParentSize().frostedDots())
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(16.dp)) {
                Text(info.utcOffsetLabel, color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    longDate(date) + " ${parseDate(date).year}",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = TitleTracking
                )
                Spacer(Modifier.height(4.dp))
                Text("Drag a task here, or hold and slide to plan time", color = TextMuted, fontSize = 12.sp)
            }
            Box(
                Modifier.weight(1f).fillMaxWidth()
                    .onGloballyPositioned { geometry.viewport = it.boundsInRoot() }
                    .verticalScroll(scroll)
            ) {
                Box(
                    Modifier.fillMaxWidth().height(HourHeight * 24).padding(horizontal = 16.dp)
                        .onGloballyPositioned { geometry.contentTop = it.positionInRoot().y }
                        .pointerInput(date) {
                            var anchor = 0
                            var moved = false
                            fun minuteAt(y: Float) = snap(y / hourPx * 60).coerceIn(0, MINUTES_PER_DAY)
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    // Start on the quarter hour that was pressed.
                                    anchor = ((offset.y / hourPx * 60).toInt() / SNAP_MINUTES * SNAP_MINUTES)
                                        .coerceIn(0, MINUTES_PER_DAY - SNAP_MINUTES)
                                    moved = false
                                    selection = anchor until anchor + DEFAULT_BLOCK_MINUTES
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    moved = true
                                    val at = minuteAt(change.position.y)
                                    selection = if (at > anchor) anchor until max(at, anchor + SNAP_MINUTES)
                                    else at until anchor + SNAP_MINUTES
                                },
                                onDragEnd = {
                                    selection?.let { range ->
                                        val end = if (moved) range.last + 1 else min(anchor + DEFAULT_BLOCK_MINUTES, MINUTES_PER_DAY)
                                        onPickRange(ScheduleRequest(date, range.first, end))
                                    }
                                    selection = null
                                },
                                onDragCancel = { selection = null }
                            )
                        }
                ) {
                    Column {
                        for (hour in 0 until 24) {
                            Box(Modifier.fillMaxWidth().height(HourHeight)) {
                                HorizontalDivider(color = BorderColor, modifier = Modifier.align(Alignment.TopStart).fillMaxWidth())
                                Text(
                                    clockLabel(hour * 60),
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = LocalMonoFamily.current,
                                    modifier = Modifier.align(Alignment.TopStart).padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    state.blocksOn(date).forEach { block ->
                        state.taskById(block.taskId)?.let { task -> WorkBlockCard(state, block, task, hourPx, onOpenTask) }
                    }

                    selection?.let { range -> PlannedArea(range.first, range.last + 1) }
                    hoverStart?.let { start -> PlannedArea(start, start + DEFAULT_BLOCK_MINUTES) }

                    Row(
                        Modifier.fillMaxWidth().offset(y = HourHeight * (minutesNow / 60f) - 5.dp).align(Alignment.TopStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(10.dp).background(NowLineColor, RoundedCornerShape(5.dp)))
                        Box(Modifier.weight(1f).height(2.dp).background(NowLineColor))
                    }
                }
            }
        }
    }
}

private class Geometry {
    var viewport = Rect.Zero
    var contentTop = 0f
}

internal fun minutesToDp(minutes: Int): Dp = HourHeight * (minutes / 60f)

/** Outline showing where a block will go (while selecting or dropping). */
@Composable
private fun PlannedArea(start: Int, end: Int) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        Modifier.offset(y = minutesToDp(start)).padding(start = LabelGutter, end = 4.dp)
            .fillMaxWidth().height(minutesToDp(end - start))
            .clip(shape).background(AccentColor.copy(alpha = 0.16f)).border(1.dp, AccentColor, shape)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text("${clockLabel(start)} – ${clockLabel(end)}", color = TextPrimary, fontSize = 11.sp, fontFamily = LocalMonoFamily.current)
    }
}
