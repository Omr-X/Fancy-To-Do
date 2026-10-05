package net.omar.fancytodo.model

import kotlinx.serialization.Serializable

/** Response of the server's /dashboard endpoint. */
@Serializable
data class DashboardSummary(
    val totalTasks: Int,
    val completedTasks: Int,
    val overdueTasks: Int,
    val totalHabits: Int,
    val habitsDoneToday: Int
)
