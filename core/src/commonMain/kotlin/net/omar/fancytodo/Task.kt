package net.omar.fancytodo

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Task(
    val id: String = Uuid.random().toString(),
    val title: String,
    val description: String? = null,
    val parentTaskId: String? = null,
    val chapterId: String,
    val blockedIds: MutableList<String> =  mutableListOf(),
    val dueDate: String? = null,
    val guessedTime: Double? = null,
    var actualTime: Double = 0.0,
    var isCompleted: Boolean = false
)