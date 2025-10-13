package me.emiliomini.dutyschedule.shared.debug

enum class DebugFlags(private val value: Boolean) {
    DISABLE_COOKIE_PERSISTENCE(true),
    SHOW_DEBUG_INFO(false);

    fun active(): Boolean {
        return this.value
    }

    fun inactive(): Boolean {
        return !this.value
    }
}