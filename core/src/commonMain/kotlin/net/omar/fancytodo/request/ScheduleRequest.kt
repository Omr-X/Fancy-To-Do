package net.omar.fancytodo.request

/** A time range picked on the calendar ([start]/[end] in minutes after midnight) waiting for a task. */
data class ScheduleRequest(val date: String, val start: Int, val end: Int)
