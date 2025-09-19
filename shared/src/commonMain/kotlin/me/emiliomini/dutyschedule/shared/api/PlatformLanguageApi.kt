package me.emiliomini.dutyschedule.shared.api

import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

interface PlatformLanguageApi {
    @OptIn(ExperimentalTime::class)
    fun formatLocalDateTime(time: Instant, pattern: String, zone: TimeZone): String
}

expect fun getPlatformLanguageApi(): PlatformLanguageApi
