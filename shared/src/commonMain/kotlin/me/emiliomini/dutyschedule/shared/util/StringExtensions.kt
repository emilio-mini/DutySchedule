package me.emiliomini.dutyschedule.shared.util

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
fun String?.isNotNullOrBlank(): Boolean {
    contract {
        returns(true) implies (this@isNotNullOrBlank != null)
    }

    return !isNullOrBlank()
}

fun String.trimLeadingZeros(): String {
    val regex = "^0+".toRegex()
    return this.trim().replace(regex, "")
}

fun String?.nullIfBlank(): String? {
    return this?.ifBlank { null }
}
