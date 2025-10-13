package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.datastores.DutyType

object DutyTypeMapping {
    fun get(value: String?): DutyType {
        return when (value) {
            "[ SEW ]" -> DutyType.EMS
            "[ Schulung ]" -> DutyType.TRAINING
            "[ Besprechung ]" -> DutyType.MEET
            "[ Übung ]" -> DutyType.DRILL
            "[ Schulung-KFZ ]" -> DutyType.VEHICLE_TRAINING
            "[ Pflichtfortbildung Rezertifizierung ]" -> DutyType.RECERTIFICATION
            "[ HÄND mobil ]" -> DutyType.HAEND
            "[ Innendienst ]" -> DutyType.ADMINISTRATIVE
            "[ Öffentlichkeitsveranstaltung ]" -> DutyType.EVENT
            else -> DutyType.UNKNOWN
        }
    }
}