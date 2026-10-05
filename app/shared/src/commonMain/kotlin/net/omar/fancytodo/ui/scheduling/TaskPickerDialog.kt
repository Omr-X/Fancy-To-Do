package net.omar.fancytodo.ui.scheduling

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.request.ScheduleRequest
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.clockLabel
import net.omar.fancytodo.ui.components.Dot
import net.omar.fancytodo.ui.components.InputField
import net.omar.fancytodo.ui.tasks.TaskSort
import net.omar.fancytodo.ui.theme.BorderColor
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.PanelColor
import net.omar.fancytodo.ui.theme.ScrimColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary
import net.omar.fancytodo.ui.theme.paletteColor

/** Pick which task to work on during a selected range. */
@Composable
internal fun TaskPickerDialog(state: AppState, range: ScheduleRequest, onCancel: () -> Unit, onPick: (Task) -> Unit) {
    var query by remember { mutableStateOf("") }
    val options = state.tasks
        .filter { !it.isCompleted && !state.isParent(it) && it.title.contains(query.trim(), ignoreCase = true) }
        .sortedWith(TaskSort.Due.comparator(state))
    val shape = RoundedCornerShape(14.dp)
    Box(
        Modifier.fillMaxSize().background(ScrimColor)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCancel),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(16.dp).widthIn(max = 420.dp).fillMaxWidth()
                .clip(shape).background(PanelColor).border(1.dp, BorderColor, shape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .padding(vertical = 14.dp)
        ) {
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Work on a task", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(
                    "${clockLabel(range.start)} – ${clockLabel(range.end)}",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = LocalMonoFamily.current
                )
            }
            Spacer(Modifier.height(10.dp))
            InputField(query, { query = it }, "Search tasks", Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
                if (options.isEmpty()) {
                    Text("No open tasks match.", color = TextMuted, fontSize = 13.sp, modifier = Modifier.padding(10.dp))
                }
                options.forEach { task ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onPick(task) }.padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Dot(paletteColor(state.colorIndexOf(task)), 8.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(task.title, color = TextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        state.topicOf(task)?.let { Text(it.title, color = TextMuted, fontSize = 12.sp, maxLines = 1) }
                    }
                }
            }
        }
    }
}
