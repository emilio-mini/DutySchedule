package me.emiliomini.dutyschedule.models.prep

import java.time.OffsetDateTime

data class AssignedEmployee(
    var employee: Employee,
    val requirement: Requirement,
    val begin: OffsetDateTime,
    val end: OffsetDateTime,
    var info: String = "",
    var skill: Skill
)
