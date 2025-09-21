package me.emiliomini.dutyschedule.shared.api

import android.util.Log

class AndroidLogging(override val tag: String) : PlatformLogger(tag) {
    override fun debug(message: String, throwable: Throwable?) {
        Log.d(tag, message, throwable)
    }

    override fun warn(message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    override fun error(message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}

actual fun getPlatformLogger(tag: String): PlatformLogger {
    return AndroidLogging(tag)
}