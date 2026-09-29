package net.omar.fancytodo

import kotlin.math.roundToInt

internal data class Ymd(val y: Int, val m: Int, val d: Int)

internal val MonthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
internal val MonthShort = MonthNames.map { it.take(3) }
internal val WeekdayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
internal val WeekdayShort = WeekdayNames.map { it.take(3) }

private fun daysFromCivil(year: Int, month: Int, day: Int): Int {
    val y = if (month <= 2) year - 1 else year
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (month + (if (month > 2) -3 else 9)) + 2) / 5 + day - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146097 + doe - 719468
}

private fun civilFromDays(days: Int): Ymd {
    val z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val m = if (mp < 10) mp + 3 else mp - 9
    return Ymd(yoe + era * 400 + (if (m <= 2) 1 else 0), m, doy - (153 * mp + 2) / 5 + 1)
}

internal fun ymd(iso: String): Ymd {
    val p = iso.split("-").mapNotNull { it.toIntOrNull() }
    return if (p.size == 3) Ymd(p[0], p[1], p[2]) else Ymd(1970, 1, 1)
}

internal fun isoOf(y: Int, m: Int, d: Int) = "${y.toString().padStart(4, '0')}-${m.toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}"

private fun epochDay(iso: String) = ymd(iso).let { daysFromCivil(it.y, it.m, it.d) }

internal fun addDays(iso: String, n: Int) = civilFromDays(epochDay(iso) + n).let { isoOf(it.y, it.m, it.d) }

internal fun daysBetween(from: String, to: String) = epochDay(to) - epochDay(from)

internal fun weekday(iso: String) = ((epochDay(iso) + 3) % 7 + 7) % 7

internal fun daysInMonth(y: Int, m: Int) = when (m) {
    2 -> if (y % 4 == 0 && (y % 100 != 0 || y % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

internal fun longDate(iso: String) = ymd(iso).let { "${WeekdayNames[weekday(iso)]}, ${it.d} ${MonthNames[it.m - 1]}" }

internal fun shortDate(iso: String) = ymd(iso).let { "${it.d} ${MonthShort[it.m - 1]}" }

internal fun dueLabel(due: String, today: String): String = when (daysBetween(today, due)) {
    0 -> "Today"
    1 -> "Tomorrow"
    -1 -> "Yesterday"
    in 2..6 -> WeekdayNames[weekday(due)]
    else -> shortDate(due)
}

internal fun Task.overdue(today: String): Boolean {
    val d = dueDate ?: return false
    return !isCompleted && d < today
}

internal fun hoursLabel(hours: Double): String {
    val minutes = (hours * 60).roundToInt()
    return when {
        minutes < 60 -> "${minutes}m"
        minutes % 60 == 0 -> "${minutes / 60}h"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}
