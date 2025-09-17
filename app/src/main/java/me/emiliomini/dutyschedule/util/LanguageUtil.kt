package me.emiliomini.dutyschedule.util

import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyTypeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SkillProto
import me.emiliomini.dutyschedule.models.app.Role
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.models.prep.Skill.AZUBI
import me.emiliomini.dutyschedule.models.prep.Skill.FK
import me.emiliomini.dutyschedule.models.prep.Skill.HAEND
import me.emiliomini.dutyschedule.models.prep.Skill.NFS
import me.emiliomini.dutyschedule.models.prep.Skill.NOTKOMPETENZ
import me.emiliomini.dutyschedule.models.prep.Skill.PA
import me.emiliomini.dutyschedule.models.prep.Skill.RS
import me.emiliomini.dutyschedule.models.prep.Skill.SEF

fun DutyTypeProto.resourceString(): Int {
    return when (this) {
        DutyTypeProto.EMS -> R.string.data_dutytype_ems
        DutyTypeProto.TRAINING -> R.string.data_dutytype_training
        DutyTypeProto.MEET -> R.string.data_dutytype_meet
        DutyTypeProto.DRILL -> R.string.data_dutytype_drill
        DutyTypeProto.VEHICLE_TRAINING -> R.string.data_dutytype_vehicle_training
        DutyTypeProto.RECERTIFICATION -> R.string.data_dutytype_recertification
        DutyTypeProto.HAEND -> R.string.data_dutytype_haend
        DutyTypeProto.ADMINISTRATIVE -> R.string.data_dutytype_administrative
        else -> R.string.data_dutytype_unknown
    }
}

fun Requirement.resourceString(): Int {
    return getRequirementResourceString(this.value)
}

fun Role.resourceString(): Int {
    return when (this) {
        Role.DEVELOPER -> R.string.data_role_developer
        else -> 0
    }
}

fun Role.infoResourceString(): Int {
    return when (this) {
        Role.DEVELOPER -> R.string.data_role_info_developer
        else -> 0
    }
}

fun RequirementProto.resourceString(): Int {
    return getRequirementResourceString(this.guid)
}

private fun getRequirementResourceString(guid: String): Int {
    return when (guid) {
        Requirement.TRAINING.value -> R.string.data_requirement_training
        Requirement.KFZ_2.value,
        Requirement.KFZ.value -> R.string.data_requirement_kfz
        Requirement.VEHICLE.value -> R.string.data_requirement_vehicle
        Requirement.SEW.value -> R.string.data_requirement_sew
        Requirement.RTW.value -> R.string.data_requirement_rtw
        Requirement.ITF.value -> R.string.data_requirement_itf
        Requirement.HAEND.value -> R.string.data_requirement_haend
        Requirement.EL.value -> R.string.data_requirement_el
        Requirement.TF.value -> R.string.data_requirement_tf
        Requirement.RS.value -> R.string.data_requirement_rs
        Requirement.HAEND_EL.value -> R.string.data_requirement_haend_el
        Requirement.HAEND_DR.value -> R.string.data_requirement_haend_dr
        Requirement.ITF_LKW.value -> R.string.data_requirement_itf_el
        Requirement.ITF_NFS.value -> R.string.data_requirement_itf_nfs
        Requirement.RTW_NFS.value -> R.string.data_requirement_rtw_nfs
        Requirement.RTW_RS.value -> R.string.data_requirement_rtw_rs
        else -> R.string.data_requirement_none
    }
}

fun SkillProto.resourceString(): Int {
    return when (this.guid) {
        RS.value -> R.string.data_skill_rs
        AZUBI.value -> R.string.data_skill_azubi
        HAEND.value -> R.string.data_skill_haend
        FK.value -> R.string.data_skill_fk
        PA.value -> R.string.data_skill_pa
        SEF.value -> R.string.data_skill_sef
        NOTKOMPETENZ.value -> R.string.data_skill_nkv
        NFS.value -> R.string.data_skill_nfs
        else -> R.string.data_requirement_none
    }
}