package me.emiliomini.dutyschedule.models.prep

import java.time.OffsetDateTime

data class Message(
    val resourceGuid: String,
    val title: String,
    val message: String,
    val priority: Int,
    val displayFrom: OffsetDateTime,
    val displayTo: OffsetDateTime
)
