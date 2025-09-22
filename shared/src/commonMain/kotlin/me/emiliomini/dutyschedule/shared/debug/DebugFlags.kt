package me.emiliomini.dutyschedule.shared.debug

enum class DebugFlags(private val value: Boolean) {
    SHOW_DEBUG_INFO(false);

    fun active(): Boolean {
        return this.value
    }

    fun inactive(): Boolean {
        return !this.value
    }
}