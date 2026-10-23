package com.kuota.backend

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.MonthDay
import java.time.ZoneId

val LIMA_ZONE: ZoneId = ZoneId.of("America/Lima")

// [VALIDACIÓN LEGAL] Feriados nacionales fijos (Ley 31816 y modificatorias); los feriados extraordinarios
// decretados por el Ejecutivo se agregan con KUOTA_EXTRA_HOLIDAYS=2026-12-24,2026-12-31.
object PeruBusinessCalendar {
    private val fixedHolidays = setOf(
        MonthDay.of(1, 1),
        MonthDay.of(5, 1),
        MonthDay.of(6, 7),
        MonthDay.of(6, 29),
        MonthDay.of(7, 23),
        MonthDay.of(7, 28),
        MonthDay.of(7, 29),
        MonthDay.of(8, 6),
        MonthDay.of(8, 30),
        MonthDay.of(10, 8),
        MonthDay.of(11, 1),
        MonthDay.of(12, 8),
        MonthDay.of(12, 9),
        MonthDay.of(12, 25)
    )

    private val extraHolidays: Set<LocalDate> = System.getenv("KUOTA_EXTRA_HOLIDAYS")
        .orEmpty()
        .split(',')
        .mapNotNull { runCatching { LocalDate.parse(it.trim()) }.getOrNull() }
        .toSet()

    fun isHoliday(date: LocalDate): Boolean {
        if (MonthDay.from(date) in fixedHolidays || date in extraHolidays) return true
        val easter = easterSunday(date.year)
        return date == easter.minusDays(3) || date == easter.minusDays(2)
    }

    fun isBusinessDay(date: LocalDate): Boolean =
        date.dayOfWeek != DayOfWeek.SATURDAY && date.dayOfWeek != DayOfWeek.SUNDAY && !isHoliday(date)

    /** El día de recepción no cuenta: el plazo corre desde el día hábil siguiente. */
    fun addBusinessDays(start: LocalDate, businessDays: Int): LocalDate {
        var date = start
        var remaining = businessDays
        while (remaining > 0) {
            date = date.plusDays(1)
            if (isBusinessDay(date)) remaining--
        }
        return date
    }

    // Algoritmo anónimo gregoriano (Meeus/Jones/Butcher).
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
