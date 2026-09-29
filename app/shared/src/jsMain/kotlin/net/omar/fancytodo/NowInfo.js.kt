package net.omar.fancytodo

import kotlin.js.Date

private val dayAbbrevs = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

actual fun currentNow(): NowInfo {
    val date = Date()
    val offsetHours = -date.getTimezoneOffset() / 60
    val offsetLabel = "GMT" + (if (offsetHours >= 0) "+" else "") + offsetHours
    return NowInfo(date.getFullYear(), date.getMonth() + 1, date.getDate(), dayAbbrevs[date.getDay()], date.getHours(), date.getMinutes(), offsetLabel)
}
