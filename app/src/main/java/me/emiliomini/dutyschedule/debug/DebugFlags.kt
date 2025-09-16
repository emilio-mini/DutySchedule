package me.emiliomini.dutyschedule.debug

enum class DebugFlags(private val value: Boolean) {
    SHOW_DEBUG_INFO(false),
    BYPASS_CACHE(false),
    AVOID_PREP_API(false);

    fun active(): Boolean {
        return this.value
    }

    fun inactive(): Boolean {
        return !this.value
    }
}