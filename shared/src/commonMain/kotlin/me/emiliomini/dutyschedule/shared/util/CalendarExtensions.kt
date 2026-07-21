@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.util

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Timestamp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

data class CalendarDay(
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val hasDayShift: Boolean,
    val hasNightShift: Boolean,
    val isSelfDuty: Boolean,
    val timestamp: Timestamp,
)

data class CalendarMonth(
    val monthLabel: String,
    val weekdayLabels: List<String>,
    val weeks: List<List<CalendarDay>>,
)

private data class DutyMarker(
    val hasDayShift: Boolean = false,
    val hasNightShift: Boolean = false,
    val isSelfDuty: Boolean = false,
)

/**
 * Builds a Monday-first month grid for the current month, padded with the trailing/leading
 * days of the neighbouring months so every week row has 7 entries.
 *
 * Kept free of kotlinx-datetime types in its public signature so modules that only depend on
 * [MinimalDutyDefinition]/[Timestamp] (like the Android widget) can call it without needing
 * kotlinx-datetime on their own compile classpath.
 */
fun buildCalendarMonth(
    duties: List<MinimalDutyDefinition>,
    selfName: String?,
): CalendarMonth {
    val zone = TimeZone.currentSystemDefault()
    val now = Clock.System.now()
    val today = now.toLocalDateTime(zone).date
    val firstOfMonth = LocalDate(today.year, today.month, 1)
    val firstOfNextMonth = firstOfMonth.plus(DatePeriod(months = 1))
    val daysInMonth = firstOfMonth.daysUntil(firstOfNextMonth)

    val leadingOffset = (firstOfMonth.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber + 7) % 7
    val gridStart = firstOfMonth.minus(leadingOffset, DateTimeUnit.DAY)
    val totalCells = ((leadingOffset + daysInMonth + 6) / 7) * 7

    val dutyByDate = mutableMapOf<LocalDate, DutyMarker>()
    for (duty in duties) {
        val date = duty.begin.toInstant().toLocalDateTime(zone).date
        val isSelf = selfName != null && duty.staff.contains(selfName)
        val isNight = midpointInstant(duty.begin.toInstant(), duty.end.toInstant()).isNight(zone)
        val existing = dutyByDate[date]
        dutyByDate[date] = DutyMarker(
            hasDayShift = (existing?.hasDayShift ?: false) || !isNight,
            hasNightShift = (existing?.hasNightShift ?: false) || isNight,
            isSelfDuty = (existing?.isSelfDuty ?: false) || isSelf,
        )
    }

    val days = (0 until totalCells).map { index ->
        val date = gridStart.plus(index, DateTimeUnit.DAY)
        val marker = dutyByDate[date]
        CalendarDay(
            dayOfMonth = date.day,
            isCurrentMonth = date.month == today.month && date.year == today.year,
            isToday = date == today,
            hasDayShift = marker?.hasDayShift == true,
            hasNightShift = marker?.hasNightShift == true,
            isSelfDuty = marker?.isSelfDuty == true,
            timestamp = date.atStartOfDayIn(zone).toTimestamp(),
        )
    }

    val weekdayLabels = (0 until 7).map { index ->
        gridStart.plus(index, DateTimeUnit.DAY).atStartOfDayIn(zone).format("EEE", zone)
    }

    return CalendarMonth(
        monthLabel = now.format("MMMM yyyy", zone),
        weekdayLabels = weekdayLabels,
        weeks = days.chunked(7),
    )
}
