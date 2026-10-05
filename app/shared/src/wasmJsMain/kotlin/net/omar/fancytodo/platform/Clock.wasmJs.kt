package net.omar.fancytodo.platform

import net.omar.fancytodo.time.CurrentTime

// Wasm can't call kotlin.js.Date, so the browser clock is read as "year,month,day,hour,minute,offset".
@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => { const d = new Date(); return [d.getFullYear(), d.getMonth() + 1, d.getDate(), d.getHours(), d.getMinutes(), Math.trunc(-d.getTimezoneOffset() / 60)].join(',') }")
private external fun readBrowserClock(): String

actual fun currentTime(): CurrentTime {
    val parts = readBrowserClock().split(",").map { it.toInt() }
    val offsetHours = parts[5]
    return CurrentTime(
        year = parts[0],
        month = parts[1],
        day = parts[2],
        hour = parts[3],
        minute = parts[4],
        utcOffsetLabel = "GMT" + (if (offsetHours >= 0) "+" else "") + offsetHours
    )
}
