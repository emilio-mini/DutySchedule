package me.emiliomini.dutyschedule.shared.api

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class AndroidLanguageApi : PlatformLanguageApi {

    @OptIn(ExperimentalTime::class)
    override fun formatLocalDateTime(time: Instant, pattern: String, zone: TimeZone): String {
        val jInstant = time.toJavaInstant()
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return jInstant.atZone(ZoneId.of(zone.id)).format(formatter)
    }

}

actual fun getPlatformLanguageApi(): PlatformLanguageApi {
    return AndroidLanguageApi()
}