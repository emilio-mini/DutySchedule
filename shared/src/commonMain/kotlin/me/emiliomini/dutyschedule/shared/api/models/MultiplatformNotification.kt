package me.emiliomini.dutyschedule.shared.api.models

data class MultiplatformNotification(
    val id: Int,
    val channel: MultiplatformNotificationChannel,
    val priority: MultiplatformNotificationPriority,
    val title: String,
    val content: String,
    val leftAction: MultiplatformNotificationAction? = null,
    val rightAction: MultiplatformNotificationAction? = null,
    val onDismiss: (() -> Unit)? = null
)

data class MultiplatformNotificationAction(
    val title: String = "",
    val action: () -> Unit
)

data class MultiplatformNotificationChannel(
    val id: String,
    val title: String,
    val description: String,
    val priority: MultiplatformNotificationPriority
)

enum class MultiplatformNotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}
