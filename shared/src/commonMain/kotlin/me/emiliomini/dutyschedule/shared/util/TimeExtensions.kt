@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.util

import kotlinx.datetime.TimeZone
import me.emiliomini.dutyschedule.shared.api.getPlatformLanguageApi
import me.emiliomini.dutyschedule.shared.datastores.Timestamp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

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

fun Instant.toTimestamp(): Timestamp {
    return Timestamp(this.epochSeconds, this.nanosecondsOfSecond)
}

fun Instant.format(pattern: String, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    return getPlatformLanguageApi().formatLocalDateTime(this, pattern, zone)
}

fun String.toInstant(): Instant {
    return Instant.parse(this)
}
