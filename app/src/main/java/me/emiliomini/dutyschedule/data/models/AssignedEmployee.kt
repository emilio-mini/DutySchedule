package me.emiliomini.dutyschedule.data.models

import me.emiliomini.dutyschedule.data.models.mapping.Requirement
import java.time.OffsetDateTime

data class AssignedEmployee(
    var employee: Employee,
    val requirement: Requirement,
    val begin: OffsetDateTime,
    val end: OffsetDateTime
)
