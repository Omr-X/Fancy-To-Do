package net.omar.fancytodo.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Everything the server saves to disk. */
@Serializable
data class Snapshot(
    @SerialName("books") val areas: List<Area> = emptyList(),
    @SerialName("chapters") val topics: List<Topic> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val blocks: List<WorkBlock> = emptyList()
)
