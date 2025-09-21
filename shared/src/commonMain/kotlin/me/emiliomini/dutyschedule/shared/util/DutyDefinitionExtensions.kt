@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.util

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.emiliomini.dutyschedule.shared.datastores.DutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import kotlin.time.ExperimentalTime

fun DutyDefinition.getVehicle(): Slot? {
    return slots.firstOrNull { !it.employeeGuid.isNullOrBlank() && RequirementMapping.VEHICLES.contains(it.requirement.guid) }
}

fun DutyDefinition.getAllVehicles(requirements: List<String> = RequirementMapping.VEHICLES): List<Slot> {
    return slots.filter { requirements.contains(it.requirement.guid) }
}

fun DutyDefinition.isEmsDuty(): Boolean {
    var hasVehicleSlot = false
    var hasDriverSlot = false
    var hasPassengerSlot = false

    for (slot in slots) {
        if (RequirementMapping.VEHICLES.contains(slot.requirement.guid)) {
            hasVehicleSlot = true
        } else if (RequirementMapping.DRIVERS.contains(slot.requirement.guid)) {
            hasDriverSlot = true
        } else if (RequirementMapping.PASSENGERS.contains(slot.requirement.guid)) {
            hasPassengerSlot = true
        }

        if (hasVehicleSlot && hasDriverSlot && hasPassengerSlot) {
            break
        }
    }

    return hasVehicleSlot && hasDriverSlot && hasPassengerSlot
}

fun DutyDefinition.staffRequirementsMet(): Boolean {
    return if (isEmsDuty()) slots.filter { !it.employeeGuid.isNullOrBlank() }.map { it.requirement }.distinct().count() >= 2 else true
}

fun DutyDefinition.allRequirementsMet(): Boolean {
    return if (isEmsDuty()) getVehicle() != null && staffRequirementsMet() else true
}

fun DutyDefinition.getAllocatedSlotsCount(): Int {
    return slots.count { !it.employeeGuid.isNullOrBlank() }
}

fun DutyDefinition.isNightShift(zone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val beginTime = this.begin.toInstant().toLocalDateTime(zone)
    val endTime = this.end.toInstant().toLocalDateTime(zone)

    return beginTime.hour >= 19 || endTime.hour <= 7
}
