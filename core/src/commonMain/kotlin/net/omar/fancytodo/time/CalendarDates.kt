package net.omar.fancytodo.time

import kotlin.math.roundToInt

// Dates travel as ISO strings (yyyy-MM-dd); these helpers do the calendar math on them.

val MONTH_NAMES = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
val MONTH_ABBREVIATIONS = MONTH_NAMES.map { it.take(3) }

/** Weeks start on Monday. */
val WEEKDAY_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
val WEEKDAY_ABBREVIATIONS = WEEKDAY_NAMES.map { it.take(3) }

const val MINUTES_PER_DAY = 24 * 60

fun parseDate(iso: String): CalendarDate {
    val parts = iso.split("-").mapNotNull { it.toIntOrNull() }
    return if (parts.size == 3) CalendarDate(parts[0], parts[1], parts[2]) else CalendarDate(1970, 1, 1)
}

fun isoDate(year: Int, month: Int, day: Int) =
    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"

/** Days since 1970-01-01; handy for comparing moments across days. */
fun epochDay(iso: String) = parseDate(iso).let { daysFromCivil(it.year, it.month, it.day) }

fun addDays(iso: String, days: Int) = civilFromDays(epochDay(iso) + days).let { isoDate(it.year, it.month, it.day) }

fun daysBetween(from: String, to: String) = epochDay(to) - epochDay(from)

/** 0 = Monday ... 6 = Sunday. */
fun weekdayIndex(iso: String) = ((epochDay(iso) + 3) % 7 + 7) % 7

fun daysInMonth(year: Int, month: Int) = when (month) {
    2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}

/** "Saturday, 3 October" */
fun longDate(iso: String) = parseDate(iso).let { "${WEEKDAY_NAMES[weekdayIndex(iso)]}, ${it.day} ${MONTH_NAMES[it.month - 1]}" }

/** "3 Oct" */
fun shortDate(iso: String) = parseDate(iso).let { "${it.day} ${MONTH_ABBREVIATIONS[it.month - 1]}" }

/** Due date relative to today: "Today", "Tomorrow", a weekday this week, else a short date. */
fun dueLabel(due: String, today: String): String = when (daysBetween(today, due)) {
    0 -> "Today"
    1 -> "Tomorrow"
    -1 -> "Yesterday"
    in 2..6 -> WEEKDAY_NAMES[weekdayIndex(due)]
    else -> shortDate(due)
}

/** "09:30" for a minute of the day. */
fun clockLabel(minuteOfDay: Int) =
    "${(minuteOfDay / 60).toString().padStart(2, '0')}:${(minuteOfDay % 60).toString().padStart(2, '0')}"

/** "45m", "2h", "1h 30m" */
fun hoursLabel(hours: Double): String {
    val minutes = (hours * 60).roundToInt()
    return when {
        minutes < 60 -> "${minutes}m"
        minutes % 60 == 0 -> "${minutes / 60}h"
        else -> "${minutes / 60}h ${minutes % 60}m"
    }
}

// Days <-> civil date conversion (Howard Hinnant's algorithm), proleptic Gregorian calendar.

private fun daysFromCivil(year: Int, month: Int, day: Int): Int {
    val y = if (month <= 2) year - 1 else year
    val era = (if (y >= 0) y else y - 399) / 400
    val yearOfEra = y - era * 400
    val dayOfYear = (153 * (month + (if (month > 2) -3 else 9)) + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097 + dayOfEra - 719468
}

private fun civilFromDays(days: Int): CalendarDate {
    val z = days + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val dayOfEra = z - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val mp = (5 * dayOfYear + 2) / 153
    val month = if (mp < 10) mp + 3 else mp - 9
    return CalendarDate(yearOfEra + era * 400 + (if (month <= 2) 1 else 0), month, dayOfYear - (153 * mp + 2) / 5 + 1)
}
