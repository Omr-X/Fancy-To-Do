package net.omar.fancytodo.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/** A period scheduled to work on a task: [date] (yyyy-MM-dd) from [start] to [end], in minutes after midnight. */
@Serializable
data class WorkBlock(
    val id: String = Uuid.random().toString(),
    val taskId: String,
    val date: String,
    val start: Int,
    val end: Int
)
