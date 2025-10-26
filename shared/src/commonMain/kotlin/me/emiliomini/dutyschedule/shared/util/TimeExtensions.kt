@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.util

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.emiliomini.dutyschedule.shared.api.getPlatformLanguageApi
import me.emiliomini.dutyschedule.shared.datastores.Timestamp
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun Long?.toInstant(): Instant {
    if (this == null) {
        return Instant.DISTANT_PAST
    }

    return Instant.fromEpochMilliseconds(this)
}

fun Timestamp.now(): Timestamp {
    return Clock.System.now().toTimestamp()
}

fun Timestamp.toInstant(): Instant {
    return Instant.fromEpochSeconds(this.seconds, this.nanos)
}

fun Timestamp.toEpochMilliseconds(): Long {
    return this.toInstant().toEpochMilliseconds()
}

fun Timestamp.format(pattern: String, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    return this.toInstant().format(pattern, zone)
}

fun Timestamp.isNight(zone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    return this.toInstant().isNight(zone)
}

fun Instant.toTimestamp(): Timestamp {
    return Timestamp(this.epochSeconds, this.nanosecondsOfSecond)
}

fun Instant.format(pattern: String, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    return getPlatformLanguageApi().formatLocalDateTime(this, pattern, zone)
}

fun Instant.isNight(zone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val localDateTime = this.toLocalDateTime(zone)
    return localDateTime.hour < 6 || localDateTime.hour >= 18
}

fun Instant.startOfWeek(zone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val zoned = this.toLocalDateTime(zone)
    var date = zoned.date
    val daysBack = ((date.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber + 7) % 7)
    date = date.minus(daysBack, DateTimeUnit.DAY)
    val mondayMidnight = LocalDateTime(date, LocalTime(0, 0, 0, 0))
    return mondayMidnight.toInstant(zone)
}

fun midpointInstant(a: Instant, b: Instant): Instant {
    val difference = millisecondsBetween(a, b)
    return if (a.toEpochMilliseconds() < b.toEpochMilliseconds()) {
        a.plus(difference / 2, DateTimeUnit.MILLISECOND)
    } else {
        b.plus(difference / 2, DateTimeUnit.MILLISECOND)
    }
}

fun millisecondsBetween(a: Instant, b: Instant): Long {
    return abs(a.toEpochMilliseconds() - b.toEpochMilliseconds())
}

fun String.toInstant(): Instant {
    return Instant.parse(this)
}
