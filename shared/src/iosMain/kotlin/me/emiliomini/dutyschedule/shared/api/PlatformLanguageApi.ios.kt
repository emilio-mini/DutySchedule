@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class IosLanguageApi : PlatformLanguageApi {

    override fun formatLocalDateTime(
        time: Instant,
        pattern: String,
        zone: TimeZone
    ): String {
        // TODO: Implement
        return "-- not yet implemented --"
    }

}

actual fun initializePlatformLanguageApi(): PlatformLanguageApi {
    return IosLanguageApi()
}
