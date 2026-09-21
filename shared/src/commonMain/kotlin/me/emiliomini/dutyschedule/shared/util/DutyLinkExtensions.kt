@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.util

import me.emiliomini.dutyschedule.shared.datastores.DutyLink
import me.emiliomini.dutyschedule.shared.services.scaffold.ScheduleFocus
import kotlin.time.ExperimentalTime

fun DutyLink.toScheduleFocus(): ScheduleFocus {
    return ScheduleFocus(
        orgGuid = this.orgGuid,
        dutyBegin = this.begin.toEpochMilliseconds(),
        planDutyGuid = this.planDutyGuid
    )
}
