package net.omar.fancytodo.ui.tasks

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.model.Area
import net.omar.fancytodo.model.Task
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

internal enum class TaskSort(val label: String) {
    Due("Due date"), Name("Name"), Estimate("Estimate"), Area("Area");

    fun comparator(state: AppState): Comparator<Task> = when (this) {
        Due -> compareBy({ state.effectiveDue(it) == null }, { state.effectiveDue(it) })
        Name -> compareBy { it.title.lowercase() }
        Estimate -> compareBy({ it.estimatedHours == null }, { -(it.estimatedHours ?: 0.0) })
        Area -> compareBy({ state.areaOf(it) == null }, { state.areaOf(it)?.title?.lowercase() }, { state.topicOf(it)?.title?.lowercase() })
    }
}

/** "Sort: Due date" label opening a menu of sort orders. */
@Composable
internal fun SortMenu(sort: TaskSort, onChange: (TaskSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier.clip(RoundedCornerShape(8.dp)).clickable { open = true }.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sort: ", color = TextMuted, fontSize = 12.sp)
            Text(sort.label, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            TaskSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, color = if (option == sort) TextPrimary else TextSecondary) },
                    onClick = { onChange(option); open = false }
                )
            }
        }
    }
}
