package net.omar.fancytodo

data class NowInfo(
    val dayAbbrev: String,
    val dayNumber: Int,
    val hour: Int,
    val minute: Int,
    val offsetLabel: String
)

expect fun currentNow(): NowInfo
