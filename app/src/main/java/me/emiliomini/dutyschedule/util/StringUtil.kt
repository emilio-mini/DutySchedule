package me.emiliomini.dutyschedule.util

fun String.trimLeadingZeros(): String {
    val regex = "^0+".toRegex()
    return this.trim().replace(regex, "")
}
