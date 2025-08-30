package me.emiliomini.dutyschedule.util

import com.google.protobuf.Timestamp
import java.time.Instant
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object TimeUtil {

    fun isAfterOrEqualTime(instant: Instant, hour: Int, minute: Int = 0): Boolean {
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        val targetTime = LocalTime.of(hour, minute)
        return !localTime.isBefore(targetTime)
    }
}

fun OffsetDateTime.toTimestamp(): Timestamp {
    val instant = this.toInstant()
    return Timestamp.newBuilder()
        .setSeconds(instant.epochSecond)
        .setNanos(instant.nano)
        .build()
}

fun Timestamp.toOffsetDateTime(): OffsetDateTime {
    val instant = Instant.ofEpochSecond(this.seconds, this.nanos.toLong())
    return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC)
}
