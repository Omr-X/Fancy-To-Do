package net.omar.fancytodo.model

import kotlinx.serialization.Serializable

/** Response of the server's /home endpoint. */
@Serializable
data class HomeSummary(
    val habitsToday: List<Habit>,
    val tasksDueToday: List<Task>,
    val openTasks: List<Task>,
    val reminder: Reminder
)
