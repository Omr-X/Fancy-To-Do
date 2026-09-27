package net.omar.fancytodo

import kotlinx.serialization.Serializable

@Serializable
data class Dashboard(
    val totalTasks: Int,
    val completedTasks: Int,
    val overdueTasks: Int,
    val totalHabits: Int,
    val habitsDoneToday: Int
)