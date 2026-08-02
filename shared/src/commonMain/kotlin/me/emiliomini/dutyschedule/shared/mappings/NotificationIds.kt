package me.emiliomini.dutyschedule.shared.mappings

object NotificationIds {
    const val ALARM_RINGING = 1

    const val PERMANENT_INFO = 37

    const val DUTY_UPDATE = 38

    private val RESERVED = setOf(ALARM_RINGING, PERMANENT_INFO, DUTY_UPDATE)

    /** Stable per-alarm id so several countdowns can be shown side by side */
    fun countdown(guid: String): Int {
        val id = guid.hashCode()

        return if (id in RESERVED) id + RESERVED.size else id
    }
}
