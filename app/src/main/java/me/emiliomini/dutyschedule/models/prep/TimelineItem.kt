package me.emiliomini.dutyschedule.models.prep

import java.time.OffsetDateTime

sealed interface TimelineItem {
    data class Date(val date: OffsetDateTime) : TimelineItem
    data class Duty(val duty: DutyDefinition) : TimelineItem
}