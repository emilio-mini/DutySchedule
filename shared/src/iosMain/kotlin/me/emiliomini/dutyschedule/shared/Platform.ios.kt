package me.emiliomini.dutyschedule.shared

import me.emiliomini.dutyschedule.shared.api.getPlatformLogger
import platform.Foundation.NSBundle
import platform.Foundation.NSString

actual fun platform() = "ios"

actual fun versionCode(): Long {
    getPlatformLogger("VERSION_TAG").d(NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as String)
    return (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? NSString)
        .let { if (it != null) it.toString().toLong() else -1 }
}

actual fun versionName(): String {
    val info = NSBundle.mainBundle.infoDictionary
    return (info?.get("CFBundleShortVersionString") as? NSString)?.toString() ?: "unknown"
}