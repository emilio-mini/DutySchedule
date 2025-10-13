@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface PlatformAlarmApi {
    fun requestPermission()
    fun isPermissionGranted(): Boolean
    suspend fun setAlarm(id: Int, time: Instant, zone: TimeZone = TimeZone.currentSystemDefault())
    suspend fun cancelAlarm(id: Int)
    fun isAlarmSet(id: Int): Boolean
}

expect fun initializePlatformAlarmApi(): PlatformAlarmApi

private var api: PlatformAlarmApi? = null

fun getPlatformAlarmApi(): PlatformAlarmApi = if (api == null) {
    api = initializePlatformAlarmApi()
    api!!
} else {
    api!!
}