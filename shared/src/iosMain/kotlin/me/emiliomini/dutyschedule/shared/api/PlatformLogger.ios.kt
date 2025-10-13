package me.emiliomini.dutyschedule.shared.api

class IosLogger(tag: String) : PlatformLogger(tag) {

    override fun debug(message: String, throwable: Throwable?) {
        // TODO: Implement
    }

    override fun warn(message: String, throwable: Throwable?) {
        // TODO: Implement
    }

    override fun error(message: String, throwable: Throwable?) {
        // TODO: Implement
    }

}

actual fun getPlatformLogger(tag: String): PlatformLogger {
    return IosLogger(tag)
}
