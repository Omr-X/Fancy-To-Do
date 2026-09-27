package net.omar.fancytodo

import kotlinx.serialization.Serializable

@Serializable
data class Reminder(val text: String = "")