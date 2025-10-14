package me.emiliomini.dutyschedule.shared

import androidx.core.content.pm.PackageInfoCompat
import me.emiliomini.dutyschedule.shared.api.APPLICATION_CONTEXT

actual fun platform() = "android"

actual fun versionCode(): Long {
    return PackageInfoCompat.getLongVersionCode(
        APPLICATION_CONTEXT.packageManager.getPackageInfo(APPLICATION_CONTEXT.packageName, 0)
    )
}

actual fun versionName(): String {
    return APPLICATION_CONTEXT.packageManager.getPackageInfo(
        APPLICATION_CONTEXT.packageName,
        0
    ).versionName ?: "unknown"
}
