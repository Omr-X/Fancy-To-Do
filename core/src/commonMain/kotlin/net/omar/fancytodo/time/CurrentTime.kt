package net.omar.fancytodo.time

/** The local date and time as read from the device clock. */
data class CurrentTime(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,
    val minute: Int,
    /** e.g. "GMT-4" */
    val utcOffsetLabel: String
) {
    val isoDate: String get() = isoDate(year, month, day)
    val minuteOfDay: Int get() = hour * 60 + minute
}
