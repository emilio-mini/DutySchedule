package me.emiliomini.dutyschedule.shared.util

fun Int.toOrdinalSuffix(): String {
    return if (this in 11..13) {
        "th"
    } else {
        when (this % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
}