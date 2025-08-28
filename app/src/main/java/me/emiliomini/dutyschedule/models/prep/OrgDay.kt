package me.emiliomini.dutyschedule.models.prep

import java.time.OffsetDateTime

data class OrgDay(
    val orgGuid: String,
    val date: OffsetDateTime,
    var dayShift: List<DutyDefinition>,
    var nightShift: List<DutyDefinition>
)
