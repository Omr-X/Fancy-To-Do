package net.omar.fancytodo.ui.home

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
import net.omar.fancytodo.model.Habit
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.ui.components.CheckSquare
import net.omar.fancytodo.ui.components.InputField
import net.omar.fancytodo.ui.components.PlusIcon
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

@Composable
internal fun HabitList(state: AppState) {
    var adding by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }

    if (state.habits.isEmpty() && !adding) {
        Text("No habits yet.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
    state.habits.forEach { habit -> HabitRow(state, habit) }
    if (adding) {
        InputField(
            draft,
            { draft = it },
            "Habit name",
            Modifier.fillMaxWidth().padding(6.dp),
            onDone = {
                if (draft.isNotBlank()) state.addHabit(draft)
                draft = ""
                adding = false
            }
        )
    } else {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { adding = true }.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlusIcon(AccentColor, 14.dp)
            Spacer(Modifier.width(10.dp))
            Text("Add habit", color = AccentColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun HabitRow(state: AppState, habit: Habit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(habit.title, color = TextPrimary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        CheckSquare(habit.completions[state.today] == true, AccentColor, outline = AccentColor) { state.toggleHabit(habit, state.today) }
    }
}
