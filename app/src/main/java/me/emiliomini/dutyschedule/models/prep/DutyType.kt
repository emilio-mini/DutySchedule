package me.emiliomini.dutyschedule.models.prep

import me.emiliomini.dutyschedule.R

enum class DutyType {
    EMS,
    TRAINING,
    UNKNOWN;

    fun getResourceString(): Int {
        return when (this) {
            EMS -> R.string.data_dutytype_ems
            TRAINING -> R.string.data_dutytype_training
            else -> R.string.data_dutytype_unknown
        }
    }
}