package me.emiliomini.dutyschedule.json.util

import com.google.protobuf.GeneratedMessageLite

/**
 * Null-safe wrapper for Proto setters. This accepts nullable values and calls the setter only when
 * the given value is not null and additional given conditions are true
 */
inline fun <V, M : GeneratedMessageLite<M, B>, B : GeneratedMessageLite.Builder<M, B>> B.s(
    v: V?,
    additionalCondition: (value: V) -> Boolean = { true },
    crossinline setter: B.(value: V) -> B
): B {
    return if (v != null && additionalCondition(v)) setter(v) else this
}
