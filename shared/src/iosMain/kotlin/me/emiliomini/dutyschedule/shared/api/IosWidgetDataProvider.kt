@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.CalendarMonth
import me.emiliomini.dutyschedule.shared.util.buildCalendarMonth
import me.emiliomini.dutyschedule.shared.util.toInstant
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Read-only data access for the WidgetKit extension (see `DutyScheduleWidget/` in the Xcode
 * project). The extension is a separate process from the host app, so it links against the same
 * `sharedKit` framework and calls through here rather than duplicating protobuf-decoding logic in
 * Swift. Returned Kotlin data classes ([MinimalDutyDefinition], [CalendarMonth], [CalendarDay])
 * are plain data classes and are visible to Swift as ordinary read-only properties.
 */
object IosWidgetDataProvider {

    suspend fun getNextDuty(): MinimalDutyDefinition? {
        StorageService.initialize()
        val now = Clock.System.now()
        return StorageService.UPCOMING_DUTIES.getOrDefault().minimalDutyDefinitions
            .filter { it.begin.toInstant() > now }
            .minByOrNull { it.begin.seconds }
    }

    suspend fun getSelfName(): String? {
        StorageService.initialize()
        return StorageService.SELF.get()?.name
    }

    suspend fun getCalendarMonth(): CalendarMonth {
        StorageService.initialize()
        val upcoming = StorageService.UPCOMING_DUTIES.getOrDefault().minimalDutyDefinitions
        val past = StorageService.PAST_DUTIES.getOrDefault().years.values
            .flatMap { it.minimalDutyDefinitions }
        val selfName = StorageService.SELF.get()?.name
        return buildCalendarMonth(upcoming + past, selfName)
    }
}
