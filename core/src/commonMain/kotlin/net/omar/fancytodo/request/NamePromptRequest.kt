package net.omar.fancytodo.request

/** Asks the user for the name of a new area or topic. */
sealed class NamePromptRequest(val title: String, val placeholder: String) {
    data object NewArea : NamePromptRequest("New area", "e.g. Gym")
    data class NewTopic(val areaId: String, val areaTitle: String) : NamePromptRequest("New topic in $areaTitle", "e.g. Push day")
}
