package me.emiliomini.dutyschedule.shared.api

import platform.Foundation.NSLog

class IosLogger(tag: String) : PlatformLogger(tag) {

    override fun debug(message: String, throwable: Throwable?) {
        NSLog("DEBUG [%s] %s", tag, message)
    }

    override fun warn(message: String, throwable: Throwable?) {
        NSLog("WARN [%s] %s", tag, message)
    }

    override fun error(message: String, throwable: Throwable?) {
        NSLog("ERROR [%s] %s", tag, message)
    }

}

actual fun getPlatformLogger(tag: String): PlatformLogger {
    return IosLogger(tag)
}
