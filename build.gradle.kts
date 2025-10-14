import java.io.ByteArrayOutputStream

// Top-level build file where you can add configuration options common to all sub-projects/modules.
val commitNumber = gitCommitCount()
val appVersionName: String get() = "1.0"
val appVersionCode: Int get() = commitNumber

version = appVersionName
extra["appVersionCode"] = appVersionCode

fun gitCommitCount(): Int {
    return try {
        val stdout = ByteArrayOutputStream()
        exec {
            commandLine = listOf("git", "rev-list", "--count", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim().toInt()
    } catch (e: Exception) {
        logger.warn("Unable to determine git commit count – defaulting versionCode to -1")
        1
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.jetbrains.compose) apply false
}