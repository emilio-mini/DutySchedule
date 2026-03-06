package me.emiliomini.dutyschedule.shared.services

import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.alarm_set_for
import dutyschedule.shared.generated.resources.next_duty
import dutyschedule.shared.generated.resources.no_alarm_set
import dutyschedule.shared.generated.resources.no_upcuming_duties
import me.emiliomini.dutyschedule.shared.api.getPlatformAlarmApi
import me.emiliomini.dutyschedule.shared.api.getPlatformNotificationApi
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotification
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationPriority
import me.emiliomini.dutyschedule.shared.mappings.NotificationChannelMapping
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.toInstant
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object NotificationService {
    @OptIn(ExperimentalTime::class)
    suspend fun sendInfoNotification (){
        val upcomingDuties = StorageService.UPCOMING_DUTIES.get()?.minimalDutyDefinitions ?: emptyList()
        val notificationApi = getPlatformNotificationApi()
        val alarmApi = getPlatformAlarmApi()
        // Build notification text with fetched duties count, next duty time, and callsign
        val notificationText = if (upcomingDuties.isNotEmpty()) {
            // val dutyCount = upcomingDuties.size

            val nextDuty = upcomingDuties.firstOrNull { it.begin.toInstant() > Clock.System.now() }
            val nextDutyInstant = nextDuty?.begin?.toInstant()
            val callsign = nextDuty?.vehicle ?: "Unknown"
            val nextAlarm = alarmApi.getNextAlarm()?.format("dd.MM.YYYY HH:mm")
            // val dutiesFetchedString = getPluralString(Res.plurals.updated_duties, dutyCount,dutyCount)
            val nextDutyString = getString(Res.string.next_duty)
            val alarmSetString = getString(Res.string.alarm_set_for)
            val noAlarmString = getString(Res.string.no_alarm_set)
            // |$dutiesFetchedString
            """
                |$nextDutyString: ${nextDutyInstant?.format("dd.MM.YYYY HH:mm")} - $callsign
                |${if (nextAlarm != null) "$alarmSetString $nextAlarm." else noAlarmString}
            """.trimMargin()
        } else {
            getString(Res.string.no_upcuming_duties)
        }

        val notification = MultiplatformNotification(
            37,
            NotificationChannelMapping.PERMANENT_INFO,
            MultiplatformNotificationPriority.LOW,
            "Duty Info",
            notificationText,

            )
        notificationApi.send(notification)

    }
}