package net.omar.fancytodo
import kotlinx.serialization.Serializable

@Serializable
data class Home(
    val habitsToday: List<Habit>,
    val tasksDueToday: List<Task>,
    val tasksByPriority: List<Task>,
    val Reminder: Reminder
)