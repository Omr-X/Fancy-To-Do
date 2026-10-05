package net.omar.fancytodo.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: String = Uuid.random().toString(),
    val title: String,
    /** Days (yyyy-MM-dd) the habit was done. */
    val completions: Map<String, Boolean> = emptyMap()
)
