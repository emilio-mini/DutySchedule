package me.emiliomini.dutyschedule.shared.util

import me.emiliomini.dutyschedule.shared.datastores.Incode

fun Incode?.isValid(): Boolean {
    return !isInvalid()
}

fun Incode?.isInvalid(): Boolean {
    return this == null || token.isBlank() || value.isBlank()
}