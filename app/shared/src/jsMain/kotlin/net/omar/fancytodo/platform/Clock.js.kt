package net.omar.fancytodo.platform

import kotlin.js.Date
import net.omar.fancytodo.time.CurrentTime

actual fun currentTime(): CurrentTime {
    val date = Date()
    val offsetHours = -date.getTimezoneOffset() / 60
    return CurrentTime(
        year = date.getFullYear(),
        month = date.getMonth() + 1,
        day = date.getDate(),
        hour = date.getHours(),
        minute = date.getMinutes(),
        utcOffsetLabel = "GMT" + (if (offsetHours >= 0) "+" else "") + offsetHours
    )
}
