package net.omar.fancytodo.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.dueLabel
import net.omar.fancytodo.time.hoursLabel
import net.omar.fancytodo.ui.components.CheckSquare
import net.omar.fancytodo.ui.components.LockIcon
import net.omar.fancytodo.ui.components.MetaIcon
import net.omar.fancytodo.ui.components.MetaKind
import net.omar.fancytodo.ui.scheduling.draggableTask
import net.omar.fancytodo.ui.theme.DangerColor
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.WarningColor
import net.omar.fancytodo.ui.theme.paletteColor

private val MetaWidths = mapOf(
    MetaKind.Area to 120.dp,
    MetaKind.Topic to 120.dp,
    MetaKind.Due to 100.dp,
    MetaKind.Estimate to 72.dp,
    MetaKind.Worked to 72.dp
)

// Columns are dropped in this order of least importance when there isn't room.
private val MetaPriority = listOf(MetaKind.Due, MetaKind.Area, MetaKind.Topic, MetaKind.Estimate, MetaKind.Worked)

/** Which metadata columns fit next to a readable title in [width] (after [reserved] for padding etc). */
internal fun fitColumns(width: Dp, reserved: Dp): List<MetaKind> {
    var room = width - reserved
    val shown = MetaPriority.filter { kind ->
        val w = MetaWidths.getValue(kind)
        (room >= w).also { fits -> if (fits) room -= w }
    }.toSet()
    return MetaKind.entries.filter { it in shown }
}

/** One task on a single line: checkbox (or red lock when blocked), title, then metadata columns. */
@Composable
internal fun TaskLine(state: AppState, task: Task, columns: List<MetaKind>, onOpen: (Task) -> Unit) {
    val area = state.areaOf(task)
    val color = paletteColor(state.colorIndexOf(task))
    val areaTint = paletteColor(area?.color ?: 0)
    val blocked = state.isBlocked(task)
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .draggableTask(task) // drag onto the day calendar to plan time for it
            .clickable { onOpen(task) }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (blocked) LockIcon(DangerColor, 20.dp) else CheckSquare(task.isCompleted, color) { state.toggleTask(task) }
        Spacer(Modifier.width(12.dp))
        Text(
            task.title,
            color = if (task.isCompleted || blocked) TextSecondary else TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        columns.forEach { kind ->
            val due = state.effectiveDue(task)
            val (text, textColor, iconColor) = when (kind) {
                MetaKind.Area -> Triple(area?.title, TextSecondary, areaTint)
                MetaKind.Topic -> Triple(state.topicOf(task)?.title, TextSecondary, color)
                MetaKind.Due -> {
                    val dueColor = when {
                        due == null || task.isCompleted -> TextSecondary
                        due < state.today -> DangerColor
                        due == state.today -> WarningColor
                        else -> TextSecondary
                    }
                    Triple(due?.let { dueLabel(it, state.today) }, dueColor, dueColor)
                }
                MetaKind.Estimate -> Triple(task.estimatedHours?.let { hoursLabel(it) }, TextSecondary, TextSecondary)
                MetaKind.Worked -> Triple(state.workedHours(task).takeIf { it > 0.0 }?.let { hoursLabel(it) }, TextSecondary, TextSecondary)
            }
            MetaCell(kind, text, textColor, iconColor, MetaWidths.getValue(kind))
        }
    }
}

@Composable
private fun MetaCell(kind: MetaKind, text: String?, textColor: Color, iconColor: Color, width: Dp) {
    Row(Modifier.width(width).padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        MetaIcon(kind, if (text == null) TextMuted else iconColor)
        Spacer(Modifier.width(6.dp))
        Text(
            text ?: "—",
            color = if (text == null) TextMuted else textColor,
            fontSize = 12.sp,
            fontFamily = if (kind == MetaKind.Estimate || kind == MetaKind.Worked) LocalMonoFamily.current else null,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
