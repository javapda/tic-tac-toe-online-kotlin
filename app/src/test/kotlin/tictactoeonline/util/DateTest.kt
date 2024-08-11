package tictactoeonline.util

import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import kotlin.test.assertEquals

class DateTest {

    @Test
    fun `test Date`() {
        val date = LocalDate.parse("2024-08-09")
        val tz = ZoneId.of("America/Phoenix")
        val d: Date = Date.from(date.atStartOfDay(tz).toInstant())
        assertEquals(1723186800000, d.time)
    }

    @Test
    fun `test date and time`() {

    }

    @Test
    fun `test Epoch Date at GMT`() {
        val date = LocalDate.parse("1970-01-01")
        val tz = ZoneId.of("GMT")
        val d: Date = Date.from(date.atStartOfDay(tz).toInstant())
        assertEquals(0, d.time)

    }

    @Test
    fun `test LocalDate`() {
        // yyyy-MM-dd
        val date = LocalDate.parse("2024-08-09")
        assertEquals(2024, date.year)
        assertEquals(8, date.monthValue /* 1-based month */)
        assertEquals(9, date.dayOfMonth)
        assertEquals("AUGUST", date.month.name)
        assertEquals(8, date.month.value /* 0-based month */)
    }
}