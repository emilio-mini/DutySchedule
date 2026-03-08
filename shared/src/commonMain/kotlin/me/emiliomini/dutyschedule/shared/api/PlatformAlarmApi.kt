@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface PlatformAlarmApi {
    fun requestPermission(): Boolean
    fun isPermissionGranted(): Boolean
    suspend fun setAlarm(guid: String, time: Instant, zone: TimeZone = TimeZone.currentSystemDefault(), edited: Boolean)
    suspend fun cancelAlarm(guid: String)
    fun isAlarmSet(guid: String): Boolean
    fun getNextAlarm(): Instant?
}

expect fun initializePlatformAlarmApi(): PlatformAlarmApi

private var api: PlatformAlarmApi? = null

fun getPlatformAlarmApi(): PlatformAlarmApi = if (api == null) {
    api = initializePlatformAlarmApi()
    api!!
} else {
    api!!
}