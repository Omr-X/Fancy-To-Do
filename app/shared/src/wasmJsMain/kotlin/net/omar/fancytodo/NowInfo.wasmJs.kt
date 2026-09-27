package net.omar.fancytodo

@JsFun("() => { const d = new Date(); return d.getDay() + ',' + d.getDate() + ',' + d.getHours() + ',' + d.getMinutes() + ',' + (-d.getTimezoneOffset()/60) }")
private external fun jsNowRaw(): String

private val dayAbbrevs = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

actual fun currentNow(): NowInfo {
    val parts = jsNowRaw().split(",")
    val offsetHours = parts[4].toInt()
    val offsetLabel = "GMT" + (if (offsetHours >= 0) "+" else "") + offsetHours
    return NowInfo(dayAbbrevs[parts[0].toInt()], parts[1].toInt(), parts[2].toInt(), parts[3].toInt(), offsetLabel)
}
