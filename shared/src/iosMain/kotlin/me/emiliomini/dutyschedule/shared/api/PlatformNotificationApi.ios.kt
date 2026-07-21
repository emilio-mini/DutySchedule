package me.emiliomini.dutyschedule.shared.api

import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotification
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationPriority
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotification
import platform.UserNotifications.UNNotificationAction
import platform.UserNotifications.UNNotificationActionOptionNone
import platform.UserNotifications.UNNotificationCategory
import platform.UserNotifications.UNNotificationCategoryOptionNone
import platform.UserNotifications.UNNotificationInterruptionLevel
import platform.UserNotifications.UNNotificationPresentationOptionBanner
import platform.UserNotifications.UNNotificationPresentationOptionList
import platform.UserNotifications.UNNotificationPresentationOptionSound
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationResponse
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNUserNotificationCenterDelegateProtocol
import platform.darwin.NSObject

private const val ACTION_LEFT = "ACTION_LEFT"
private const val ACTION_RIGHT = "ACTION_RIGHT"
private const val DEFAULT_CATEGORY = "default"

/**
 * Notification actions are plain closures (see [MultiplatformNotification.leftAction]), which -
 * exactly like Android's `NotificationActionRegistry` - only live for the current process
 * lifetime. This registry mirrors that same limitation on iOS rather than pretending to solve it:
 * an action tapped after the app process was killed will not fire its closure.
 */
private val pendingNotifications = mutableMapOf<String, MultiplatformNotification>()

class IosNotificationApi : PlatformNotificationApi {
    private val logger = getPlatformLogger("IosNotificationApi")
    private val center = UNUserNotificationCenter.currentNotificationCenter()
    private val registeredCategoryIds = mutableSetOf(DEFAULT_CATEGORY)

    init {
        center.delegate = IosNotificationDelegate()
    }

    override fun requestPermission(): Boolean = IosNotificationAuthorization.request()

    override fun isPermissionGranted(): Boolean = IosNotificationAuthorization.isGranted()

    override fun send(notification: MultiplatformNotification) {
        val identifier = notification.id.toString()
        pendingNotifications[identifier] = notification

        val content = UNMutableNotificationContent().also {
            it.title = notification.title
            it.body = notification.content
            it.categoryIdentifier = ensureCategory(notification)
            it.interruptionLevel = interruptionLevel(notification.priority)
            if (notification.priority != MultiplatformNotificationPriority.LOW) {
                it.sound = UNNotificationSound.defaultSound
            }
        }

        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = identifier,
            content = content,
            trigger = null
        )

        center.addNotificationRequest(request) { error ->
            if (error != null) {
                logger.warn("Failed to schedule notification $identifier: ${error.localizedDescription}")
            }
        }
    }

    override fun dismiss(notification: MultiplatformNotification) {
        val identifier = notification.id.toString()
        pendingNotifications.remove(identifier)
        center.removeDeliveredNotificationsWithIdentifiers(listOf(identifier))
        center.removePendingNotificationRequestsWithIdentifiers(listOf(identifier))
    }

    private fun interruptionLevel(priority: MultiplatformNotificationPriority): UNNotificationInterruptionLevel =
        when (priority) {
            MultiplatformNotificationPriority.LOW -> UNNotificationInterruptionLevel.UNNotificationInterruptionLevelPassive
            MultiplatformNotificationPriority.NORMAL -> UNNotificationInterruptionLevel.UNNotificationInterruptionLevelActive
            // No critical-alerts entitlement, so URGENT is the best we can do: time-sensitive
            // breaks through most Focus filters without requiring special Apple approval.
            MultiplatformNotificationPriority.HIGH,
            MultiplatformNotificationPriority.URGENT -> UNNotificationInterruptionLevel.UNNotificationInterruptionLevelTimeSensitive
        }

    private fun ensureCategory(notification: MultiplatformNotification): String {
        val left = notification.leftAction
        val right = notification.rightAction
        if (left == null && right == null) return DEFAULT_CATEGORY

        // Categories carry fixed action titles, so a distinct title pair needs its own category.
        val categoryId = "cat_${left?.title.orEmpty()}_${right?.title.orEmpty()}".replace(" ", "_")
        if (!registeredCategoryIds.add(categoryId)) return categoryId

        val actions = buildList {
            left?.let { add(UNNotificationAction.actionWithIdentifier(ACTION_LEFT, it.title, UNNotificationActionOptionNone)) }
            right?.let { add(UNNotificationAction.actionWithIdentifier(ACTION_RIGHT, it.title, UNNotificationActionOptionNone)) }
        }

        val category = UNNotificationCategory.categoryWithIdentifier(
            categoryId,
            actions,
            emptyList<String>(),
            UNNotificationCategoryOptionNone
        )

        center.getNotificationCategoriesWithCompletionHandler { existing ->
            @Suppress("UNCHECKED_CAST")
            val updated = ((existing as? Set<UNNotificationCategory>) ?: emptySet()) + category
            center.setNotificationCategories(updated.toSet())
        }

        return categoryId
    }
}

private class IosNotificationDelegate : NSObject(), UNUserNotificationCenterDelegateProtocol {

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        willPresentNotification: UNNotification,
        withCompletionHandler: (ULong) -> Unit
    ) {
        withCompletionHandler(
            UNNotificationPresentationOptionBanner or UNNotificationPresentationOptionSound or UNNotificationPresentationOptionList
        )
    }

    override fun userNotificationCenter(
        center: UNUserNotificationCenter,
        didReceiveNotificationResponse: UNNotificationResponse,
        withCompletionHandler: () -> Unit
    ) {
        val identifier = didReceiveNotificationResponse.notification.request.identifier
        val notification = pendingNotifications[identifier]
        when (didReceiveNotificationResponse.actionIdentifier) {
            ACTION_LEFT -> notification?.leftAction?.action?.invoke()
            ACTION_RIGHT -> notification?.rightAction?.action?.invoke()
        }
        withCompletionHandler()
    }
}

actual fun initializePlatformNotificationApi(): PlatformNotificationApi {
    return IosNotificationApi()
}
