package com.kouta.app.core.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay

/** Espejo del calendario del backend para mostrar un plazo estimado mientras la hoja no tiene número oficial. */
object PeruBusinessCalendar {
    private val fixedHolidays = setOf(
        MonthDay.of(1, 1), MonthDay.of(5, 1), MonthDay.of(6, 7), MonthDay.of(6, 29),
        MonthDay.of(7, 23), MonthDay.of(7, 28), MonthDay.of(7, 29), MonthDay.of(8, 6),
        MonthDay.of(8, 30), MonthDay.of(10, 8), MonthDay.of(11, 1), MonthDay.of(12, 8),
        MonthDay.of(12, 9), MonthDay.of(12, 25)
    )

    fun isBusinessDay(date: LocalDate): Boolean {
        if (date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY) return false
        if (MonthDay.from(date) in fixedHolidays) return false
        val easter = easterSunday(date.year)
        return date != easter.minusDays(3) && date != easter.minusDays(2)
    }

    fun addBusinessDays(start: LocalDate, businessDays: Int): LocalDate {
        var date = start
        var remaining = businessDays
        while (remaining > 0) {
            date = date.plusDays(1)
            if (isBusinessDay(date)) remaining--
        }
        return date
    }

    fun businessDaysUntil(today: LocalDate, due: LocalDate): Int {
        var count = 0
        var cursor = today
        while (cursor.isBefore(due)) {
            cursor = cursor.plusDays(1)
            if (isBusinessDay(cursor)) count++
        }
        return count
    }

    private fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = ((h + l - 7 * m + 114) % 31) + 1
        return LocalDate.of(year, month, day)
    }
}
