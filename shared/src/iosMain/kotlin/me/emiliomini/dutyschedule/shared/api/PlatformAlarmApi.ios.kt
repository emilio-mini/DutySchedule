@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.api

import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.notifications_alarms_duty_content
import dutyschedule.shared.generated.resources.notifications_alarms_duty_title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import me.emiliomini.dutyschedule.shared.datastores.Alarm
import me.emiliomini.dutyschedule.shared.datastores.AlarmItems
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.systemTimeZone
import platform.Foundation.timeZoneWithName
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationInterruptionLevel
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val ALARM_IDENTIFIER_PREFIX = "alarm_"

private val alarmScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

class IosAlarmApi : PlatformAlarmApi {
    private val logger = getPlatformLogger("IosAlarmApi")
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    // iOS has no synchronous "is a notification pending"/"what's the next one" query, so this
    // in-memory cache backs isAlarmSet()/getNextAlarm(). Seeded from storage at startup and kept
    // in sync by setAlarm()/cancelAlarm(), mirroring what's persisted in StorageService.ALARM_ITEMS.
    private val activeAlarms = mutableMapOf<String, Instant>()

    init {
        alarmScope.launch {
            StorageService.ALARM_ITEMS.getOrDefault().alarms
                .filter { it.active }
                .forEach { activeAlarms[it.guid] = Instant.fromEpochMilliseconds(it.timestamp) }
        }
    }

    override fun requestPermission(): Boolean = IosNotificationAuthorization.request()

    override fun isPermissionGranted(): Boolean = IosNotificationAuthorization.isGranted()

    override suspend fun setAlarm(guid: String, time: Instant, zone: TimeZone, edited: Boolean) {
        if (time < Clock.System.now()) {
            return
        }

        val identifier = ALARM_IDENTIFIER_PREFIX + guid

        val content = UNMutableNotificationContent().also {
            it.title = getString(Res.string.notifications_alarms_duty_title)
            it.body = getString(Res.string.notifications_alarms_duty_content)
            it.sound = UNNotificationSound.defaultSound
            it.interruptionLevel = UNNotificationInterruptionLevel.UNNotificationInterruptionLevelTimeSensitive
        }

        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = dateComponents(time, zone),
            repeats = false
        )

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = trigger
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                logger.warn("Failed to schedule alarm $identifier: ${error.localizedDescription}")
            }
        }

        activeAlarms[guid] = time

        StorageService.ALARM_ITEMS.update { alarmItems ->
            val alarms = alarmItems.alarms.toMutableList()
            val index = alarms.indexOfFirst { it.guid == guid }
            if (index != -1) {
                alarms[index] = alarms[index].copy(active = true, timestamp = time.toEpochMilliseconds(), edited = edited)
            } else {
                alarms.add(
                    Alarm(
                        active = true,
                        timestamp = time.toEpochMilliseconds(),
                        code = guid.hashCode(),
                        edited = edited,
                        guid = guid
                    )
                )
            }
            AlarmItems(alarms)
        }

        logger.debug("Alarm $identifier set for $time")
    }

    override suspend fun cancelAlarm(guid: String) {
        val identifier = ALARM_IDENTIFIER_PREFIX + guid
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
        center.removeDeliveredNotificationsWithIdentifiers(listOf(identifier))
        activeAlarms.remove(guid)

        StorageService.ALARM_ITEMS.update { alarmItems ->
            val alarms = alarmItems.alarms.toMutableList()
            val index = alarms.indexOfFirst { it.guid == guid }
            if (index != -1) {
                alarms[index] = alarms[index].copy(active = false)
            }
            AlarmItems(alarms)
        }

        logger.debug("Alarm $identifier cancelled")
    }

    override fun isAlarmSet(guid: String): Boolean = activeAlarms.containsKey(guid)

    override fun getNextAlarm(): Instant? {
        val now = Clock.System.now()
        return activeAlarms.values.filter { it > now }.minOrNull()
    }

    private fun dateComponents(time: Instant, zone: TimeZone): platform.Foundation.NSDateComponents {
        val epochSeconds = time.epochSeconds.toDouble() + (time.nanosecondsOfSecond / 1_000_000_000.0)
        val date = NSDate.dateWithTimeIntervalSince1970(epochSeconds)
        val calendar = NSCalendar.currentCalendar.apply {
            timeZone = NSTimeZone.timeZoneWithName(zone.id) ?: NSTimeZone.systemTimeZone
        }
        return calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
            fromDate = date
        )
    }
}

actual fun initializePlatformAlarmApi(): PlatformAlarmApi {
    return IosAlarmApi()
}
