@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import kotlinx.datetime.TimeZone
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localeWithLocaleIdentifier
import platform.Foundation.systemTimeZone
import platform.Foundation.timeZoneWithName
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class IosLanguageApi : PlatformLanguageApi {

    override fun formatLocalDateTime(
        time: Instant,
        pattern: String,
        zone: TimeZone
    ): String {
        val epochSeconds = time.epochSeconds.toDouble() +
                (time.nanosecondsOfSecond / 1_000_000_000.0)

        val date = NSDate.dateWithTimeIntervalSince1970(epochSeconds)

        val formatter = NSDateFormatter().apply {
            dateFormat = pattern
            timeZone = NSTimeZone.timeZoneWithName(zone.id) ?: NSTimeZone.systemTimeZone
            locale = NSLocale.currentLocale
        }

        return formatter.stringFromDate(date)
    }

}

actual fun initializePlatformLanguageApi(): PlatformLanguageApi {
    return IosLanguageApi()
}
