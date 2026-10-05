package net.omar.fancytodo.ui.tasks

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.ui.components.MetaKind
import net.omar.fancytodo.ui.theme.BackgroundColor
import net.omar.fancytodo.ui.theme.BorderColor

private val BlockIndent = 30.dp
private val ChildInset = 36.dp

/**
 * Tasks of one topic as boxes. Subtasks sit inside their parent's box; a blocked task sits under
 * the task blocking it, shifted right with a connector. [visible] is every task that should show.
 */
@Composable
internal fun TaskTree(state: AppState, visible: List<Task>, onOpen: (Task) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Reserve room for the checkbox, a readable title and a couple of nesting levels.
        val columns = fitColumns(maxWidth, 300.dp)
        TaskScope(state, visible, visible.associateBy { it.id }, null, columns, onOpen)
    }
}

@Composable
private fun TaskScope(
    state: AppState,
    visible: List<Task>,
    byId: Map<String, Task>,
    parentId: String?,
    columns: List<MetaKind>,
    onOpen: (Task) -> Unit
) {
    val scope = visible.filter { effectiveParent(it, byId) == parentId }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        orderWithBlocked(scope, state).forEach { (task, depth) ->
            Row {
                if (depth > 0) {
                    Spacer(Modifier.width(BlockIndent * (depth - 1)))
                    BlockConnector()
                }
                TaskBox(state, task, visible, byId, columns, onOpen, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TaskBox(
    state: AppState,
    task: Task,
    visible: List<Task>,
    byId: Map<String, Task>,
    columns: List<MetaKind>,
    onOpen: (Task) -> Unit,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    val hasChildren = visible.any { effectiveParent(it, byId) == task.id }
    Column(modifier.clip(shape).background(BackgroundColor).border(1.dp, BorderColor, shape)) {
        TaskLine(state, task, columns, onOpen)
        if (hasChildren) {
            Box(Modifier.padding(start = ChildInset, end = 10.dp, bottom = 10.dp)) {
                TaskScope(state, visible, byId, task.id, columns, onOpen)
            }
        }
    }
}

// The └ shape linking a blocked task to the one above it. Draws a little above its own top so
// it reaches across the gap to the blocking task's box.
@Composable
private fun BlockConnector() {
    Canvas(Modifier.size(width = BlockIndent, height = 24.dp)) {
        val x = size.width * 0.4f
        val y = size.height
        drawPath(
            Path().apply {
                moveTo(x, -8.dp.toPx())
                lineTo(x, y)
                lineTo(size.width - 4.dp.toPx(), y)
            },
            BorderColor,
            style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/** The parent this task nests under, or null when the parent isn't shown or the chain loops. */
private fun effectiveParent(task: Task, byId: Map<String, Task>): String? {
    val parent = task.parentTaskId?.takeIf { it in byId } ?: return null
    val seen = mutableSetOf(task.id)
    var cursor: String? = parent
    while (cursor != null) {
        if (!seen.add(cursor)) return null
        cursor = byId[cursor]?.parentTaskId?.takeIf { it in byId }
    }
    return parent
}

/**
 * Orders sibling tasks so each blocked task sits under the blocking sibling that is shown lowest,
 * paired with how far it's shifted right. Loops in blocking fall back to the top level.
 */
private fun orderWithBlocked(scope: List<Task>, state: AppState): List<Pair<Task, Int>> {
    val ids = scope.map { it.id }.toSet()
    val blockers = scope.associate { t ->
        t.id to t.blockedByIds.filter { b -> b != t.id && b in ids && state.taskById(b)?.isCompleted == false }.distinct()
    }
    val under = mutableMapOf<String, String?>()
    scope.filter { blockers.getValue(it.id).isEmpty() }.forEach { under[it.id] = null }

    fun layout(): List<Pair<Task, Int>> {
        val out = mutableListOf<Pair<Task, Int>>()
        val seen = mutableSetOf<String>()
        fun place(task: Task, depth: Int) {
            if (!seen.add(task.id)) return
            out += task to depth
            scope.filter { it.id in under && under[it.id] == task.id }.forEach { place(it, depth + 1) }
        }
        scope.filter { it.id in under && under[it.id] == null }.forEach { place(it, 0) }
        return out
    }

    // Attach blocked tasks one at a time, once all their blockers are placed, under whichever
    // blocker currently sits lowest. Attaching never reorders tasks that are already placed.
    val pending = scope.filter { it.id !in under }.toMutableList()
    while (pending.isNotEmpty()) {
        val position = layout().withIndex().associate { (i, item) -> item.first.id to i }
        val ready = pending.firstOrNull { t -> blockers.getValue(t.id).all { it in position } } ?: break
        under[ready.id] = blockers.getValue(ready.id).maxBy { position.getValue(it) }
        pending.remove(ready)
    }
    pending.forEach { under[it.id] = null }
    return layout()
}
