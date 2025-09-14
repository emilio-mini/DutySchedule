package me.emiliomini.dutyschedule.json.mapping

import me.emiliomini.dutyschedule.datastore.prep.duty.DutyTypeProto

object DutyTypeProtoMapping {
    fun get(value: String?): DutyTypeProto {
        return when (value) {
            "[ SEW ]" -> DutyTypeProto.EMS
            "[ Schulung ]" -> DutyTypeProto.TRAINING
            else -> DutyTypeProto.UNKNOWN
        }
    }
}