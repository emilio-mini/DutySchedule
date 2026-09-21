package me.emiliomini.dutyschedule.shared.comparators

import me.emiliomini.dutyschedule.shared.datastores.DutyDefinition
import me.emiliomini.dutyschedule.shared.util.getAllocatedSlotsCount
import me.emiliomini.dutyschedule.shared.util.getVehicle
import me.emiliomini.dutyschedule.shared.util.isAllocatedTo
import me.emiliomini.dutyschedule.shared.util.isNotNullOrBlank

/**
 * [DutyDefinitionComparator] with the signed in user's own duties pulled to the front, so a day
 * with a dozen of them opens on the one they are rostered for instead of making them hunt for it.
 * Falls back to the plain ordering when there is nobody to compare against
 */
fun ownDutiesFirst(employeeGuid: String?): Comparator<DutyDefinition> {
    if (employeeGuid.isNullOrBlank()) {
        return DutyDefinitionComparator
    }

    return compareByDescending<DutyDefinition> { it.isAllocatedTo(employeeGuid) }
        .then(DutyDefinitionComparator)
}

val DutyDefinitionComparator = Comparator<DutyDefinition> { d1, d2 ->
    val d1Sew = d1.getVehicle()
    val d2Sew = d2.getVehicle()
    val d1HasSew = d1Sew != null
    val d2HasSew = d2Sew != null

    if (d1.groupGuid.isNotNullOrBlank() && d2.groupGuid.isNullOrBlank()) return@Comparator 1
    if (d1.groupGuid.isNullOrBlank() && d2.groupGuid.isNotNullOrBlank()) return@Comparator -1

    if (d1.groupGuid.isNotNullOrBlank() && d2.groupGuid.isNotNullOrBlank()) {
        val gGuid1 = d1.groupGuid
        val gGuid2 = d2.groupGuid
        val groupCmp = gGuid1.compareTo(gGuid2)
        if (groupCmp != 0) return@Comparator groupCmp
    }

    if (d1HasSew && !d2HasSew) return@Comparator -1
    if (!d1HasSew && d2HasSew) return@Comparator 1

    if (d1HasSew && d2HasSew) {
        val name1 = d1Sew.inlineEmployee!!.name
        val name2 = d2Sew.inlineEmployee!!.name
        val nameCmp = name1.compareTo(name2)
        if (nameCmp != 0) return@Comparator nameCmp
    }

    val d1Count = d1.getAllocatedSlotsCount()
    val d2Count = d2.getAllocatedSlotsCount()
    return@Comparator d1Count.compareTo(d2Count)
}
