package net.omar.fancytodo

@JsFun("() => { const d = new Date(); return [d.getFullYear(), d.getMonth() + 1, d.getDate(), d.getDay(), d.getHours(), d.getMinutes(), Math.trunc(-d.getTimezoneOffset() / 60)].join(',') }")
private external fun jsNowRaw(): String

private val dayAbbrevs = arrayOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

actual fun currentNow(): NowInfo {
    val p = jsNowRaw().split(",").map { it.toInt() }
    return NowInfo(p[0], p[1], p[2], dayAbbrevs[p[3]], p[4], p[5], "GMT" + (if (p[6] >= 0) "+" else "") + p[6])
}
