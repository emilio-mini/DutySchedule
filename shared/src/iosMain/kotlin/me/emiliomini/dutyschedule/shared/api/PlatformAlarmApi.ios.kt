@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class IosAlarmApi : PlatformAlarmApi {
    override suspend fun setAlarm(
        id: Int,
        time: Instant,
        zone: TimeZone
    ) {
        // TODO: Implement
    }

    override suspend fun cancelAlarm(id: Int) {
        // TODO: Implement
    }

    override fun isAlarmSet(id: Int): Boolean {
        // TODO: Implement
        return false
    }

}


actual fun initializePlatformAlarmApi(): PlatformAlarmApi {
    return IosAlarmApi()
}
