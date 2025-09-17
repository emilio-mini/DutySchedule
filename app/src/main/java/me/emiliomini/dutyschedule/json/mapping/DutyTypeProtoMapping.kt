package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.datastore.prep.duty.DutyTypeProto

object DutyTypeProtoMapping {
    fun get(value: String?): DutyTypeProto {
        return when (value) {
            "[ SEW ]" -> DutyTypeProto.EMS
            "[ Schulung ]" -> DutyTypeProto.TRAINING
            "[ Besprechung ]" -> DutyTypeProto.MEET
            "[ Übung ]" -> DutyTypeProto.DRILL
            "[ Schulung-KFZ ]" -> DutyTypeProto.VEHICLE_TRAINING
            "[ Pflichtfortbildung Rezertifizierung ]" -> DutyTypeProto.RECERTIFICATION
            "[ HÄND mobil ]" -> DutyTypeProto.HAEND
            "[ Innendienst ]" -> DutyTypeProto.ADMINISTRATIVE
            else -> DutyTypeProto.UNKNOWN
        }
    }
}