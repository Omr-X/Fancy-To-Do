package net.omar.fancytodo

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class Book(
    val id: String = Uuid .random().toString(),
    val title: String
)