package net.omar.fancytodo.model

import kotlin.uuid.Uuid
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** A group of tasks inside an area (e.g. "Push day" in Gym). */
@Serializable
data class Topic(
    val id: String = Uuid.random().toString(),
    val title: String,
    @SerialName("bookId") val areaId: String,
    /** Palette index; null means the topic uses its area's color. */
    val color: Int? = null
)
