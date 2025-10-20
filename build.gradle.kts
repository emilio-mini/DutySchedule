import java.io.ByteArrayOutputStream

val commitNumber = gitCommitCount()

// This represents the version number across all platforms
// For iOS the version has to be set manually because the whole platform is a huge pile of dogshit
// that doesnt allow you to programmatically set version tags #FUCKAPPLE
// Hours wasted on this shit: 3
val appVersionName: String get() = "1.1"
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

tasks.register("printVersionName") {
    doLast { println(appVersionName) }
}

tasks.register("printVersionCode") {
    doLast { println(appVersionCode) }
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