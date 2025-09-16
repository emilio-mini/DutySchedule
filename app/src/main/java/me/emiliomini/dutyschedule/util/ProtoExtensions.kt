package me.emiliomini.dutyschedule.util

import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SlotProto
import me.emiliomini.dutyschedule.models.prep.Requirement

fun DutyDefinitionProto.getVehicle(): SlotProto? {
    return slotsList.firstOrNull { it.employeeGuid.isNotBlank() && Requirement.VEHICLES.contains(it.requirement.guid) }
}

fun DutyDefinitionProto.getAllVehicles(requirements: List<String> = Requirement.VEHICLES): List<SlotProto> {
    return slotsList.filter { requirements.contains(it.requirement.guid) }
}

fun DutyDefinitionProto.isEmsDuty(): Boolean {
    var hasVehicleSlot = false
    var hasDriverSlot = false
    var hasPassengerSlot = false

    for (slot in slotsList) {
        if (Requirement.VEHICLES.contains(slot.requirement.guid)) {
            hasVehicleSlot = true
        } else if (Requirement.DRIVERS.contains(slot.requirement.guid)) {
            hasDriverSlot = true
        } else if (Requirement.PASSENGERS.contains(slot.requirement.guid)) {
            hasPassengerSlot = true
        }

        if (hasVehicleSlot && hasDriverSlot && hasPassengerSlot) {
            break
        }
    }

    return hasVehicleSlot && hasDriverSlot && hasPassengerSlot
}

fun RequirementProto.getPriority(): Int {
    return Requirement.parse(guid).priority
}

fun DutyDefinitionProto.staffRequirementsMet(): Boolean {
    return if (isEmsDuty()) slotsList.filter { it.hasEmployeeGuid() }.map { it.requirement }.distinct().count() >= 2 else true
}

fun DutyDefinitionProto.allRequirementsMet(): Boolean {
    return if (isEmsDuty()) getVehicle() != null && staffRequirementsMet() else true
}

fun DutyDefinitionProto.getAllocatedSlotsCount(): Int {
    return slotsList.count { it.hasEmployeeGuid() }
}