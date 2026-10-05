package net.omar.fancytodo.ui.scheduling

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.MINUTES_PER_DAY
import net.omar.fancytodo.time.clockLabel
import net.omar.fancytodo.ui.components.Dot
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.RaisedPanelColor
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.paletteColor

internal const val SNAP_MINUTES = 15
internal const val DEFAULT_BLOCK_MINUTES = 30

internal fun snap(minute: Float) = ((minute / SNAP_MINUTES).roundToInt() * SNAP_MINUTES)

/** Where a dragged task can be dropped: an area of the screen mapped to a day and a time. */
internal class DropTarget(val date: String, val bounds: () -> Rect, val minuteAt: (rootY: Float) -> Int)

/** A task being dragged across the app (root coordinates), e.g. from a list onto the calendar. */
internal class TaskDrag {
    var task by mutableStateOf<Task?>(null)
        private set
    var pointer by mutableStateOf(Offset.Zero)
        private set
    var target: DropTarget? = null
    var onDrop: (task: Task, date: String, start: Int) -> Unit = { _, _, _ -> }

    /** Start minute the task would land on right now, or null when not over the calendar. */
    fun hoverStart(): Int? {
        val t = target ?: return null
        if (task == null || !t.bounds().contains(pointer)) return null
        return snap(t.minuteAt(pointer.y).toFloat()).coerceIn(0, MINUTES_PER_DAY - DEFAULT_BLOCK_MINUTES)
    }

    fun start(task: Task, at: Offset) {
        this.task = task
        pointer = at
    }

    fun move(by: Offset) {
        pointer += by
    }

    fun end() {
        val dragged = task
        val start = hoverStart()
        val t = target
        if (dragged != null && start != null && t != null) onDrop(dragged, t.date, start)
        task = null
    }

    fun cancel() {
        task = null
    }
}

internal val LocalTaskDrag = staticCompositionLocalOf { TaskDrag() }

/** Lets a task row be picked up and dropped onto the day calendar. Clicks still go through. */
internal fun Modifier.draggableTask(task: Task): Modifier = composed {
    val drag = LocalTaskDrag.current
    val current by rememberUpdatedState(task)
    var origin by remember { mutableStateOf(Offset.Zero) }
    this
        .onGloballyPositioned { origin = it.positionInRoot() }
        .pointerInput(drag) {
            detectDragGestures(
                onDragStart = { offset -> drag.start(current, origin + offset) },
                onDrag = { change, amount ->
                    change.consume()
                    drag.move(amount)
                },
                onDragEnd = { drag.end() },
                onDragCancel = { drag.cancel() }
            )
        }
}

/** The little card following the cursor while a task is dragged. */
@Composable
internal fun TaskDragGhost(state: AppState) {
    val drag = LocalTaskDrag.current
    val task = drag.task ?: return
    val color = paletteColor(state.colorIndexOf(task))
    val hover = drag.hoverStart()
    val shape = RoundedCornerShape(8.dp)
    Row(
        Modifier
            .offset { IntOffset(drag.pointer.x.roundToInt() + 12, drag.pointer.y.roundToInt() + 8) }
            .shadow(10.dp, shape)
            .clip(shape).background(RaisedPanelColor).border(1.dp, color.copy(alpha = 0.6f), shape)
            .widthIn(max = 260.dp)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Dot(color, 7.dp)
        Spacer(Modifier.width(8.dp))
        Text(task.title, color = TextPrimary, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        if (hover != null) {
            Spacer(Modifier.width(8.dp))
            Text(clockLabel(hover), color = TextSecondary, fontSize = 12.sp, fontFamily = LocalMonoFamily.current)
        }
    }
}
