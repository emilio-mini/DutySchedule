package me.emiliomini.dutyschedule.models.prep

import java.time.OffsetDateTime

data class MinimalDutyDefinition(
    // WARNING: GUIDs of MinimalDutyDefinitions are NOT EQUAL to their full DutyDefinition counterparts!!
    val guid: String,
    val begin: OffsetDateTime,
    val end: OffsetDateTime,
    val type: DutyType,
    val vehicle: String?,
    val staff: List<String>,
    val duration: Int
)
