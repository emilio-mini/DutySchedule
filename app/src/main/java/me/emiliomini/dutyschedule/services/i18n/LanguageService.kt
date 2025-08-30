package me.emiliomini.dutyschedule.services.i18n

import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyTypeProto

fun DutyTypeProto.resourceString(): Int {
    return when (this) {
        DutyTypeProto.EMS -> R.string.data_dutytype_ems
        DutyTypeProto.TRAINING -> R.string.data_dutytype_training
        else -> R.string.data_dutytype_unknown
    }
}