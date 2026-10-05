package net.omar.fancytodo.time

import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarDatesTest {

    @Test
    fun addDaysCrossesMonthsAndLeapYears() {
        assertEquals("2026-11-01", addDays("2026-10-31", 1))
        assertEquals("2024-02-29", addDays("2024-02-28", 1))
        assertEquals("2023-12-31", addDays("2024-01-01", -1))
    }

    @Test
    fun weekStartsOnMonday() {
        assertEquals(0, weekdayIndex("2026-09-28")) // Monday
        assertEquals(6, weekdayIndex("2026-10-04")) // Sunday
    }

    @Test
    fun dueLabelIsRelativeToToday() {
        assertEquals("Today", dueLabel("2026-10-03", "2026-10-03"))
        assertEquals("Tomorrow", dueLabel("2026-10-04", "2026-10-03"))
        assertEquals("Yesterday", dueLabel("2026-10-02", "2026-10-03"))
        assertEquals("12 Oct", dueLabel("2026-10-12", "2026-10-03"))
    }

    @Test
    fun formatsTimes() {
        assertEquals("09:05", clockLabel(9 * 60 + 5))
        assertEquals("45m", hoursLabel(0.75))
        assertEquals("1h 30m", hoursLabel(1.5))
    }
}
