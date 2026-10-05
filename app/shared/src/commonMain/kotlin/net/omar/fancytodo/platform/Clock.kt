package net.omar.fancytodo.platform

import net.omar.fancytodo.time.CurrentTime

/** Reads the device's local date and time (implemented per web target). */
expect fun currentTime(): CurrentTime

/** Today as yyyy-MM-dd. */
fun todayIso(): String = currentTime().isoDate
