package me.emiliomini.dutyschedule.shared.api.notifications

import me.emiliomini.dutyschedule.shared.api.models.MultiplatformNotificationAction
import java.util.concurrent.ConcurrentHashMap

object NotificationActionRegistry {
    private val actions = ConcurrentHashMap<Int, MultiplatformNotificationAction>()

    fun register(action: MultiplatformNotificationAction): Int {
        var key: Int
        do {
            key = (1..Int.MAX_VALUE).random()
        } while (actions.containsKey(key))
        actions[key] = action
        return key
    }

    fun consume(key: Int): MultiplatformNotificationAction? = actions.remove(key)
}