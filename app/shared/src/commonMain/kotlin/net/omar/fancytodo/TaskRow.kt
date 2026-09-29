package net.omar.fancytodo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun TaskRow(state: AppState, task: Task, onOpen: (Task) -> Unit, boxed: Boolean = true) {
    val area = state.areaFor(task)
    val color = areaColor(area?.color ?: 0)
    val shape = RoundedCornerShape(12.dp)
    val where = listOfNotNull(area?.title, state.topicFor(task)?.title?.takeIf { it != "General" }).joinToString(" · ").ifEmpty { "No area" }
    Row(
        Modifier.fillMaxWidth()
            .clip(shape)
            .then(if (boxed) Modifier.background(PanelColor).border(1.dp, BorderColor, shape) else Modifier)
            .clickable { onOpen(task) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckCircle(task.isCompleted, color) { state.toggleTask(task) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                color = if (task.isCompleted) TextLow else TextHi,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(color, 7.dp)
                Spacer(Modifier.width(6.dp))
                Text(where, color = TextMid, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                task.dueDate?.let { due ->
                    Text("  ·  ", color = TextLow, fontSize = 12.sp)
                    Text(
                        dueLabel(due, state.today),
                        color = when {
                            task.isCompleted -> TextLow
                            due < state.today -> DangerColor
                            due == state.today -> WarningColor
                            else -> TextMid
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                task.guessedTime?.let { hours ->
                    Text("  ·  ", color = TextLow, fontSize = 12.sp)
                    Text(hoursLabel(hours), color = TextMid, fontSize = 12.sp)
                }
            }
        }
    }
}
