package ec.dx3os.core

import ec.dx3os.core.clock.ClockFormatter
import ec.dx3os.core.clock.LocalDateTimeParts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClockFormatterTest {
    private val monday = LocalDateTimeParts(2026, 10, 26, 1, 7, 5)

    @Test fun formats24h() {
        val t = ClockFormatter.format(monday, use24Hour = true)
        assertEquals("07:05", t.time)
        assertNull(t.period)
        assertEquals("Lunes, 26 de octubre", t.date)
    }

    @Test fun formats12hMidnightAndNoon() {
        assertEquals("12:00", ClockFormatter.format(monday.copy(hour = 0, minute = 0), false).time)
        assertEquals("a. m.", ClockFormatter.format(monday.copy(hour = 0, minute = 0), false).period)
        assertEquals("12:30", ClockFormatter.format(monday.copy(hour = 12, minute = 30), false).time)
        assertEquals("p. m.", ClockFormatter.format(monday.copy(hour = 12, minute = 30), false).period)
        assertEquals("11:59", ClockFormatter.format(monday.copy(hour = 23, minute = 59), false).time)
    }

    @Test fun sundayAndDecember() {
        val t = ClockFormatter.format(LocalDateTimeParts(2026, 12, 6, 7, 10, 0), true)
        assertEquals("Domingo, 6 de diciembre", t.date)
    }

    @Test fun nextMinuteTick() {
        assertEquals(60_000L, ClockFormatter.millisUntilNextMinute(120_000L))
        assertEquals(1L, ClockFormatter.millisUntilNextMinute(119_999L))
        assertEquals(30_000L, ClockFormatter.millisUntilNextMinute(90_000L))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidHour() {
        LocalDateTimeParts(2026, 1, 1, 1, 24, 0)
    }
}
