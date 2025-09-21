package me.emiliomini.dutyschedule.shared.datastores

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class Message(
    val guid: String = "",
    val resourceGuid: String = "",
    val title: String = "",
    val message: String = "",
    val priority: Int = 0,
    val displayFrom: Instant? = null,
    val displayTo: Instant? = null
)

@OptIn(ExperimentalTime::class)
fun Message.isDefault(): Boolean {
    return this == Message()
}
