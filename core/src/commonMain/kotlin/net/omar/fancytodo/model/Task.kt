package net.omar.fancytodo.model

import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Serial names keep the JSON keys of earlier versions so saved data still loads.
@Serializable
data class Task(
    val id: String = Uuid.random().toString(),
    val title: String,
    val description: String? = null,
    /** Set for subtasks: the task this one belongs to. */
    val parentTaskId: String? = null,
    @SerialName("chapterId") val topicId: String,
    /** Tasks that must be finished before this one can be. */
    @SerialName("blockedIds") val blockedByIds: List<String> = emptyList(),
    /** yyyy-MM-dd */
    val dueDate: String? = null,
    @SerialName("guessedTime") val estimatedHours: Double? = null,
    /** Hours of scheduled work that have passed; frozen once the task is completed. */
    @SerialName("actualTime") val loggedHours: Double = 0.0,
    val isCompleted: Boolean = false
)
