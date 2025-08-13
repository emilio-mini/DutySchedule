package me.emiliomini.dutyschedule.data.models

import java.time.OffsetDateTime

sealed interface TimelineItem {
    data class Date(val date: OffsetDateTime) : TimelineItem
    data class Duty(val duty: DutyDefinition) : TimelineItem
}