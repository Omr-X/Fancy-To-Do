package net.omar.fancytodo

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Habit(
    val id: String = Uuid.random().toString(),
    val title: String,
    val completions: Map<String, Boolean> = mapOf<String, Boolean>()
)