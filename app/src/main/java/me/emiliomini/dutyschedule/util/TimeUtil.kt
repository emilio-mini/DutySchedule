package me.emiliomini.dutyschedule.util

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object TimeUtil {

    fun isAfterOrEqualTime(instant: Instant, hour: Int, minute: Int = 0): Boolean {
        val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
        val targetTime = LocalTime.of(hour, minute)
        return !localTime.isBefore(targetTime)
    }
}