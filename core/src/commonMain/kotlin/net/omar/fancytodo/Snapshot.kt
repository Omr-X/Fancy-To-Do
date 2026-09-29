package net.omar.fancytodo

import kotlinx.serialization.Serializable

@Serializable
data class Snapshot(
    val books: List<Book> = emptyList(),
    val chapters: List<Chapter> = emptyList(),
    val tasks: List<Task> = emptyList(),
    val habits: List<Habit> = emptyList()
)
