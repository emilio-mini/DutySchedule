package me.emiliomini.dutyschedule.shared.api

abstract class PlatformLogger(open val tag: String) {
    abstract fun log(message: String, throwable: Throwable? = null)
    abstract fun warn(message: String, throwable: Throwable? = null)
    abstract fun error(message: String, throwable: Throwable? = null)
}

expect fun getPlatformLogger(tag: String): PlatformLogger
