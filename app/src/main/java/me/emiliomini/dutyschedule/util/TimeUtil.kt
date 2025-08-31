package me.emiliomini.dutyschedule.util

import com.google.protobuf.Timestamp
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import java.time.Instant
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object TimeUtil {

    fun isAfterOrEqualTime(instant: Instant, hour: Int, minute: Int = 0): Boolean {
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        val targetTime = LocalTime.of(hour, minute)
        return !localTime.isBefore(targetTime)
    }
}

fun String.toTimestamp(): Timestamp {
    val instant: Instant = try {
        Instant.parse(this) // "2025-08-31T14:22:05Z"
    } catch (e: DateTimeParseException) {
        try {
            OffsetDateTime.parse("${this}T00:00:00Z").toInstant() // Fallback if time is missing
        } catch (e: DateTimeParseException) {
            ZonedDateTime.parse(this).toInstant() // Fallback if timestamp contains ZoneId
        }
    }

    return instant.toTimestamp()
}

fun OffsetDateTime.toTimestamp(): Timestamp {
    return this.toInstant().toTimestamp()
}

fun Instant.toTimestamp(): Timestamp {
    return Timestamp.newBuilder()
        .setSeconds(this.epochSecond)
        .setNanos(this.nano)
        .build()
}

fun Timestamp.toOffsetDateTime(): OffsetDateTime {
    val instant = this.toInstant()
    return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault())
}

fun Timestamp.toInstant(): Instant {
    return Instant.ofEpochSecond(this.seconds, this.nanos.toLong())
}

fun Timestamp.toLocalTime(): LocalTime {
    return this.toOffsetDateTime().toLocalTime()
}

fun Timestamp.toEpochMilli(): Long {
    return this.toInstant().toEpochMilli()
}

fun Timestamp.format(pattern: String, zoneId: ZoneId = ZoneId.systemDefault()): String {
    val instant = this.toInstant()
    val formatter = DateTimeFormatter.ofPattern(pattern)
    return formatter.withZone(zoneId).format(instant)
}

fun Timestamp.isEqual(other: Timestamp): Boolean {
    return this.seconds == other.seconds && this.nanos == other.nanos
}

fun Timestamp.isNotEqual(other: Timestamp): Boolean {
    return this.seconds != other.seconds || this.nanos != other.nanos
}

fun DutyDefinitionProto.isNightShift(): Boolean {
    return begin.toLocalTime() >= LocalTime.of(19, 0) || end.toLocalTime() <= LocalTime.of(7, 0)
}
