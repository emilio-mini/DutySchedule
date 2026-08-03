package me.emiliomini.dutyschedule.shared

import platform.Foundation.NSBundle

actual fun platform() = "ios"

actual fun versionCode(): Long = bundleValue("CFBundleVersion")?.toLongOrNull() ?: -1

actual fun versionName(): String = bundleValue("CFBundleShortVersionString") ?: "unknown"

/**
 * Bridged NSString values arrive as Kotlin strings, so casting to NSString silently missed every
 * time and both versions came back as their fallbacks.
 */
private fun bundleValue(key: String): String? =
    NSBundle.mainBundle.infoDictionary?.get(key) as? String