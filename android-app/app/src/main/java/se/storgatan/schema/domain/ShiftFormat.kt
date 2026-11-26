package se.storgatan.schema.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields

/** Svensk formatering av datum och pass. Egna namn så att det inte beror på enhetens språkdata. */
object ShiftFormat {
    val ZONE: ZoneId = ZoneId.of("Europe/Stockholm")

    private val shortDays = mapOf(
        DayOfWeek.MONDAY to "mån", DayOfWeek.TUESDAY to "tis", DayOfWeek.WEDNESDAY to "ons",
        DayOfWeek.THURSDAY to "tor", DayOfWeek.FRIDAY to "fre", DayOfWeek.SATURDAY to "lör",
        DayOfWeek.SUNDAY to "sön",
    )
    private val longDays = mapOf(
        DayOfWeek.MONDAY to "måndag", DayOfWeek.TUESDAY to "tisdag", DayOfWeek.WEDNESDAY to "onsdag",
        DayOfWeek.THURSDAY to "torsdag", DayOfWeek.FRIDAY to "fredag", DayOfWeek.SATURDAY to "lördag",
        DayOfWeek.SUNDAY to "söndag",
    )
    private val months = listOf("jan", "feb", "mars", "apr", "maj", "juni", "juli", "aug", "sep", "okt", "nov", "dec")

    /** "tis 1 dec" */
    fun shortDate(date: LocalDate): String = "${shortDays.getValue(date.dayOfWeek)} ${date.dayOfMonth} ${months[date.monthValue - 1]}"

    /** "Tisdag 1 dec" */
    fun longDate(date: LocalDate): String =
        "${longDays.getValue(date.dayOfWeek).replaceFirstChar { it.uppercase() }} ${date.dayOfMonth} ${months[date.monthValue - 1]}"

    fun shortDay(date: LocalDate): String = shortDays.getValue(date.dayOfWeek)

    fun week(date: LocalDate): Int = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

    /** "Lunch" / "Middag" */
    fun type(type: ShiftType): String = when (type) {
        ShiftType.LUNCH -> "Lunch"
        ShiftType.MIDDAG -> "Middag"
    }

    /** "16–23" eller "16.30–23" */
    fun time(start: LocalTime, end: LocalTime): String = "${clock(start)}–${clock(end)}"

    private fun clock(t: LocalTime): String = if (t.minute == 0) "${t.hour}" else "${t.hour}.%02d".format(t.minute)


    /** "tis 1 dec, middag 16–23" */
    fun describe(shift: Shift): String =
        "${shortDate(shift.date)}, ${type(shift.type).lowercase()} ${time(shift.start, shift.end)}"

    /** "för 5 min sedan", "i går 18.40", "25 nov" */
    fun relative(instant: Instant, now: Instant): String {
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val then = instant.atZone(ZONE)
        val today = now.atZone(ZONE).toLocalDate()
        val hhmm = "${then.hour}.%02d".format(then.minute)
        return when {
            minutes < 1 -> "nyss"
            minutes < 60 -> "för $minutes min sedan"
            then.toLocalDate() == today -> "i dag $hhmm"
            then.toLocalDate() == today.minusDays(1) -> "i går $hhmm"
            else -> "${then.dayOfMonth} ${months[then.monthValue - 1]}"
        }
    }
}
