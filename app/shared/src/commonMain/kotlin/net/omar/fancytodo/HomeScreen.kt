package net.omar.fancytodo

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HomeScreen(state: AppState, compact: Boolean, onOpenTask: (Task) -> Unit) {
    ScreenScaffold(compact) {
        if (compact) {
            HabitsSection(state)
            Spacer(Modifier.height(36.dp))
            DueTodaySection(state, onOpenTask)
            Spacer(Modifier.height(36.dp))
            TasksSection(state, onOpenTask)
        } else {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) { HabitsSection(state) }
                Spacer(Modifier.width(48.dp))
                Column(Modifier.weight(1f)) { DueTodaySection(state, onOpenTask) }
            }
            Spacer(Modifier.height(40.dp))
            TasksSection(state, onOpenTask)
        }
    }
}

@Composable
private fun HabitsSection(state: AppState) {
    var adding by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }

    SectionTitle("Habits")
    Spacer(Modifier.height(16.dp))
    if (state.habits.isEmpty() && !adding) {
        Text("No habits yet.", color = TextMid, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
    }
    state.habits.forEach { habit ->
        HabitRow(state, habit)
        Spacer(Modifier.height(8.dp))
    }
    if (adding) {
        InputField(
            draft,
            { draft = it },
            "Habit name",
            Modifier.fillMaxWidth(),
            onDone = {
                if (draft.isNotBlank()) state.addHabit(draft)
                draft = ""
                adding = false
            }
        )
    } else {
        Row(
            Modifier.clip(RoundedCornerShape(10.dp)).clickable { adding = true }.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlusIcon(TextMid, 14.dp)
            Spacer(Modifier.width(8.dp))
            Text("Add habit", color = TextMid, fontSize = 14.sp)
        }
    }
}

@Composable
private fun HabitRow(state: AppState, habit: Habit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(PanelColor).padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckCircle(habit.completions[state.today] == true, SuccessColor) { state.toggleHabit(habit, state.today) }
        Spacer(Modifier.width(14.dp))
        Text(habit.title, color = TextHi, fontSize = 15.sp)
    }
}

@Composable
private fun DueTodaySection(state: AppState, onOpenTask: (Task) -> Unit) {
    val list = state.tasks.filter { !it.isCompleted && it.dueDate == state.today }
    SectionTitle("Due Today")
    Spacer(Modifier.height(16.dp))
    if (list.isEmpty()) {
        Text("Nothing due today.", color = TextMid, fontSize = 14.sp)
    } else {
        list.forEach {
            TaskRow(state, it, onOpenTask)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TasksSection(state: AppState, onOpenTask: (Task) -> Unit) {
    val list = state.tasks.filter { !it.isCompleted }.sortedWith(compareBy({ it.dueDate == null }, { it.dueDate }))
    SectionTitle("Tasks")
    Spacer(Modifier.height(16.dp))
    if (list.isEmpty()) {
        Text("No tasks yet.", color = TextMid, fontSize = 14.sp)
    } else {
        list.forEach {
            TaskRow(state, it, onOpenTask)
            Spacer(Modifier.height(8.dp))
        }
    }
}
