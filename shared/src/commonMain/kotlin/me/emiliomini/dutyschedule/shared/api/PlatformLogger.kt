package me.emiliomini.dutyschedule.shared.api

abstract class PlatformLogger(open val tag: String) {

    fun d(message: String, throwable: Throwable? = null) {
        this.debug(message, throwable)
    }

    fun w(message: String, throwable: Throwable? = null) {
        this.warn(message, throwable)
    }

    fun e(message: String, throwable: Throwable? = null) {
        this.error(message, throwable)
    }

    abstract fun debug(message: String, throwable: Throwable? = null)
    abstract fun warn(message: String, throwable: Throwable? = null)
    abstract fun error(message: String, throwable: Throwable? = null)
}

expect fun getPlatformLogger(tag: String): PlatformLogger
