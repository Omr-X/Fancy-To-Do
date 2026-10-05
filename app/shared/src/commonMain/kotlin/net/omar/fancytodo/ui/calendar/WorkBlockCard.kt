package net.omar.fancytodo.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.model.WorkBlock
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.MINUTES_PER_DAY
import net.omar.fancytodo.time.clockLabel
import net.omar.fancytodo.ui.components.CloseIcon
import net.omar.fancytodo.ui.scheduling.SNAP_MINUTES
import net.omar.fancytodo.ui.scheduling.snap
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.paletteColor

@Composable
internal fun WorkBlockCard(state: AppState, block: WorkBlock, task: Task, hourPx: Float, onOpenTask: (Task) -> Unit) {
    val current by rememberUpdatedState(block)
    // Pending drag offsets in minutes; applied (snapped) when the drag ends.
    var moveBy by remember(block.id) { mutableStateOf(0f) }
    var stretchBy by remember(block.id) { mutableStateOf(0f) }
    val length = block.end - block.start
    val start = (block.start + snap(moveBy)).coerceIn(0, MINUTES_PER_DAY - length)
    val end = (start + length + snap(stretchBy)).coerceIn(start + SNAP_MINUTES, MINUTES_PER_DAY)
    val color = paletteColor(state.colorIndexOf(task))
    val shape = RoundedCornerShape(6.dp)
    val tall = end - start >= 45

    Box(
        Modifier.offset(y = minutesToDp(start)).padding(start = LabelGutter, end = 4.dp)
            .fillMaxWidth().height(minutesToDp(end - start))
            .clip(shape).background(color.copy(alpha = if (task.isCompleted) 0.1f else 0.24f))
            .pointerInput(block.id) {
                detectDragGestures(
                    onDrag = { change, amount ->
                        change.consume()
                        moveBy += amount.y / hourPx * 60
                    },
                    onDragEnd = {
                        val b = current
                        val len = b.end - b.start
                        val newStart = (b.start + snap(moveBy)).coerceIn(0, MINUTES_PER_DAY - len)
                        if (newStart != b.start) state.updateBlock(b.copy(start = newStart, end = newStart + len))
                        moveBy = 0f
                    },
                    onDragCancel = { moveBy = 0f }
                )
            }
            .clickable { onOpenTask(task) }
    ) {
        Row(Modifier.fillMaxSize()) {
            Box(Modifier.width(3.dp).fillMaxHeight().background(color))
            Column(Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text(
                    task.title,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = if (tall) 2 else 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (tall) {
                    Text("${clockLabel(start)} – ${clockLabel(end)}", color = TextSecondary, fontSize = 11.sp, fontFamily = LocalMonoFamily.current)
                }
            }
            Box(
                Modifier.padding(top = 3.dp, end = 3.dp).size(18.dp).clip(RoundedCornerShape(5.dp))
                    .clickable { state.deleteBlock(block.id) },
                contentAlignment = Alignment.Center
            ) {
                CloseIcon(TextSecondary, 8.dp)
            }
        }
        // Bottom edge: drag to make the block longer or shorter.
        Box(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(8.dp)
                .pointerInput(block.id) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dy ->
                            change.consume()
                            stretchBy += dy / hourPx * 60
                        },
                        onDragEnd = {
                            val b = current
                            val newEnd = (b.end + snap(stretchBy)).coerceIn(b.start + SNAP_MINUTES, MINUTES_PER_DAY)
                            if (newEnd != b.end) state.updateBlock(b.copy(end = newEnd))
                            stretchBy = 0f
                        },
                        onDragCancel = { stretchBy = 0f }
                    )
                }
        ) {
            Box(Modifier.align(Alignment.Center).width(24.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(color.copy(alpha = 0.7f)))
        }
    }
}
