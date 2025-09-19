@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.util

import kotlinx.datetime.TimeZone
import me.emiliomini.dutyschedule.shared.api.getPlatformLanguageApi
import me.emiliomini.dutyschedule.shared.datastores.Timestamp
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

fun Timestamp.toInstant(): Instant {
    return Instant.fromEpochSeconds(this.seconds, this.nanos)
}

fun Instant.toTimestamp(): Timestamp {
    return Timestamp(this.epochSeconds, this.nanosecondsOfSecond)
}

fun Instant.format(pattern: String, zone: TimeZone = TimeZone.currentSystemDefault()): String {
    return getPlatformLanguageApi().formatLocalDateTime(this, pattern, zone)
}
