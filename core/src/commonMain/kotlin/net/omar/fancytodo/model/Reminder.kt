package net.omar.fancytodo.model

import kotlinx.serialization.Serializable

@Serializable
data class Reminder(val text: String = "")
