package net.omar.fancytodo.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import net.omar.fancytodo.state.AppState
import net.omar.fancytodo.time.addDays
import net.omar.fancytodo.time.shortDate
import net.omar.fancytodo.ui.theme.AccentColor
import net.omar.fancytodo.ui.theme.LocalMonoFamily
import net.omar.fancytodo.ui.theme.RaisedPanelColor
import net.omar.fancytodo.ui.theme.TextMuted
import net.omar.fancytodo.ui.theme.TextPrimary
import net.omar.fancytodo.ui.theme.TextSecondary

internal const val HABIT_HISTORY_DAYS = 30

/** GitHub-style activity grid: one row per habit, one square per day, oldest on the left. */
@Composable
internal fun HabitActivityGrid(state: AppState) {
    if (state.habits.isEmpty()) {
        Text("No habits yet. Add some from the Home page.", color = TextSecondary, fontSize = 14.sp)
        return
    }
    val days = (HABIT_HISTORY_DAYS - 1 downTo 0).map { addDays(state.today, -it) }
    val gap = 3.dp
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val nameWidth = min(150.dp, maxWidth * 0.3f)
        val countWidth = 52.dp
        val cellsWidth = maxWidth - nameWidth - countWidth - 24.dp
        val cell: Dp = min(18.dp, (cellsWidth - gap * (HABIT_HISTORY_DAYS - 1)) / HABIT_HISTORY_DAYS)
        val gridWidth = cell * HABIT_HISTORY_DAYS + gap * (HABIT_HISTORY_DAYS - 1)

        Column(verticalArrangement = Arrangement.spacedBy(gap + 3.dp)) {
            // Date range over the squares.
            Row {
                Spacer(Modifier.width(nameWidth + 12.dp))
                Row(Modifier.width(gridWidth)) {
                    Text(shortDate(days.first()), color = TextMuted, fontSize = 11.sp)
                    Spacer(Modifier.weight(1f))
                    Text("Today", color = TextMuted, fontSize = 11.sp)
                }
            }
            state.habits.forEach { habit ->
                val count = days.count { habit.completions[it] == true }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        habit.title,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(nameWidth)
                    )
                    Spacer(Modifier.width(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        days.forEach { day ->
                            val doneThatDay = habit.completions[day] == true
                            val shape = RoundedCornerShape(3.dp)
                            Box(
                                Modifier.size(cell).clip(shape)
                                    .background(if (doneThatDay) AccentColor else RaisedPanelColor)
                                    .then(if (day == state.today) Modifier.border(1.dp, TextSecondary, shape) else Modifier)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "$count/$HABIT_HISTORY_DAYS",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = LocalMonoFamily.current,
                        modifier = Modifier.width(countWidth)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(nameWidth + 12.dp))
                Legend(RaisedPanelColor, "Missed")
                Spacer(Modifier.width(14.dp))
                Legend(AccentColor, "Done")
            }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
    Spacer(Modifier.width(6.dp))
    Text(label, color = TextMuted, fontSize = 11.sp)
}
