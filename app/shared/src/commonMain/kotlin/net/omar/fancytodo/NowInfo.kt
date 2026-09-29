package net.omar.fancytodo

data class NowInfo(
    val year: Int,
    val month: Int,
    val dayNumber: Int,
    val dayAbbrev: String,
    val hour: Int,
    val minute: Int,
    val offsetLabel: String
)

expect fun currentNow(): NowInfo

fun todayIso(): String = currentNow().let { isoOf(it.year, it.month, it.dayNumber) }
