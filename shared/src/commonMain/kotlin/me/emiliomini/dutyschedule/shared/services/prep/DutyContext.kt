package me.emiliomini.dutyschedule.shared.services.prep

import me.emiliomini.dutyschedule.shared.datastores.DutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Slot

/**
 * An upcoming duty as it stands in the plan, with the vehicle handovers around it. [handoverFrom]
 * and [handoverTo] are the duties the same vehicle runs immediately before and after this one, so
 * a null on either side means the vehicle comes out of, or goes back to, the garage.
 */
data class DutyContext(
    val orgGuid: String,
    val duty: DutyDefinition,
    val vehicle: Slot? = null,
    val handoverFrom: DutyDefinition? = null,
    val handoverTo: DutyDefinition? = null
)
