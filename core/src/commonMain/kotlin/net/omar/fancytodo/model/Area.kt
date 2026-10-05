package net.omar.fancytodo.model

import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

/** How many colors areas and topics can pick from (the app defines the actual colors). */
const val PALETTE_SIZE = 8

/** A big part of life (School, Gym, a hobby). Holds topics, which hold tasks. */
@Serializable
data class Area(
    val id: String = Uuid.random().toString(),
    val title: String,
    /** Index into the app's color palette. */
    val color: Int = 0
)
