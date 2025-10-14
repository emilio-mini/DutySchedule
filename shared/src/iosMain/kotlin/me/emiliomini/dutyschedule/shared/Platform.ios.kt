package me.emiliomini.dutyschedule.shared

import platform.Foundation.NSBundle
import platform.Foundation.NSString

actual fun platform() = "ios"

actual fun versionCode(): Long {
    return NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion")
        .let { if (it != null) it as Long else -1 }
}

actual fun versionName(): String {
    val info = NSBundle.mainBundle.infoDictionary
    return (info?.get("CFBundleShortVersionString") as? NSString)?.toString() ?: "unknown"
}