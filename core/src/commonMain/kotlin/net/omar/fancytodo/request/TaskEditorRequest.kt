package net.omar.fancytodo.request

import net.omar.fancytodo.model.Task

/** Opens the task editor: on an existing [task], or for a new one prefilled with the defaults. */
data class TaskEditorRequest(val task: Task?, val defaultDue: String? = null, val defaultTopicId: String? = null)
