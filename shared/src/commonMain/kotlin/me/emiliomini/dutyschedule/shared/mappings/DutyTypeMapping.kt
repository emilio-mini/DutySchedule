package me.emiliomini.dutyschedule.shared.mappings

import me.emiliomini.dutyschedule.shared.datastores.DutyType

object DutyTypeMapping {
    fun get(value: String?): DutyType {
        return when (value) {
            "[ SEW ]" -> DutyType.EMS
            "[ SEW-Hintergrund ]" -> DutyType.EMS
            "[ RTW ]" -> DutyType.EMS
            "[ SEW-N ]" -> DutyType.EMS
            "[ Schulung ]" -> DutyType.TRAINING
            "[ Besprechung ]" -> DutyType.MEET
            "[ Übung ]" -> DutyType.DRILL
            "[ Schulung-KFZ ]" -> DutyType.VEHICLE_TRAINING
            "[ Pflichtfortbildung Rezertifizierung ]" -> DutyType.RECERTIFICATION
            "[ HÄND mobil ]" -> DutyType.HAEND
            "[ Innendienst ]" -> DutyType.ADMINISTRATIVE
            "[ Öffentlichkeitsveranstaltung ]" -> DutyType.EVENT
            "[ TRS Blutspendeaktion ]" -> DutyType.BLOOD_DONATION_SERVICE
            "[ Drohnen Team ]" -> DutyType.DRONE_TEAM
            else -> DutyType.UNKNOWN
        }
    }
}