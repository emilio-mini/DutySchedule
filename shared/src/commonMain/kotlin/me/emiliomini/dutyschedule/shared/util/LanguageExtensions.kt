package me.emiliomini.dutyschedule.shared.util

import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.data_dutytype_administrative
import dutyschedule.shared.generated.resources.data_dutytype_drill
import dutyschedule.shared.generated.resources.data_dutytype_ems
import dutyschedule.shared.generated.resources.data_dutytype_event
import dutyschedule.shared.generated.resources.data_dutytype_haend
import dutyschedule.shared.generated.resources.data_dutytype_meet
import dutyschedule.shared.generated.resources.data_dutytype_recertification
import dutyschedule.shared.generated.resources.data_dutytype_training
import dutyschedule.shared.generated.resources.data_dutytype_unknown
import dutyschedule.shared.generated.resources.data_dutytype_vehicle_training
import dutyschedule.shared.generated.resources.data_requirement_el
import dutyschedule.shared.generated.resources.data_requirement_haend
import dutyschedule.shared.generated.resources.data_requirement_haend_dr
import dutyschedule.shared.generated.resources.data_requirement_haend_el
import dutyschedule.shared.generated.resources.data_requirement_itf
import dutyschedule.shared.generated.resources.data_requirement_itf_el
import dutyschedule.shared.generated.resources.data_requirement_itf_nfs
import dutyschedule.shared.generated.resources.data_requirement_kfz
import dutyschedule.shared.generated.resources.data_requirement_none
import dutyschedule.shared.generated.resources.data_requirement_rs
import dutyschedule.shared.generated.resources.data_requirement_rtw
import dutyschedule.shared.generated.resources.data_requirement_rtw_nfs
import dutyschedule.shared.generated.resources.data_requirement_rtw_rs
import dutyschedule.shared.generated.resources.data_requirement_sew
import dutyschedule.shared.generated.resources.data_requirement_tf
import dutyschedule.shared.generated.resources.data_requirement_training
import dutyschedule.shared.generated.resources.data_requirement_vehicle
import dutyschedule.shared.generated.resources.data_role_developer
import dutyschedule.shared.generated.resources.data_role_first_user
import dutyschedule.shared.generated.resources.data_role_info_developer
import dutyschedule.shared.generated.resources.data_role_info_first_user
import dutyschedule.shared.generated.resources.data_skill_azubi
import dutyschedule.shared.generated.resources.data_skill_fk
import dutyschedule.shared.generated.resources.data_skill_haend
import dutyschedule.shared.generated.resources.data_skill_nfs
import dutyschedule.shared.generated.resources.data_skill_nkv
import dutyschedule.shared.generated.resources.data_skill_pa
import dutyschedule.shared.generated.resources.data_skill_rs
import dutyschedule.shared.generated.resources.data_skill_sef
import me.emiliomini.dutyschedule.shared.datastores.DutyType
import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.datastores.Skill
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.mappings.Role
import me.emiliomini.dutyschedule.shared.mappings.MappedSkills
import org.jetbrains.compose.resources.StringResource

fun DutyType.resourceString(): StringResource {
    return when (this) {
        DutyType.EMS -> Res.string.data_dutytype_ems
        DutyType.TRAINING -> Res.string.data_dutytype_training
        DutyType.MEET -> Res.string.data_dutytype_meet
        DutyType.DRILL -> Res.string.data_dutytype_drill
        DutyType.VEHICLE_TRAINING -> Res.string.data_dutytype_vehicle_training
        DutyType.RECERTIFICATION -> Res.string.data_dutytype_recertification
        DutyType.HAEND -> Res.string.data_dutytype_haend
        DutyType.ADMINISTRATIVE -> Res.string.data_dutytype_administrative
        DutyType.EVENT -> Res.string.data_dutytype_event
        else -> Res.string.data_dutytype_unknown
    }
}


fun Role.resourceString(): StringResource? {
    return when (this) {
        Role.DEVELOPER -> Res.string.data_role_developer
        Role.FIRST_USER -> Res.string.data_role_first_user
        else -> null
    }
}

fun Role.infoResourceString(): StringResource? {
    return when (this) {
        Role.DEVELOPER -> Res.string.data_role_info_developer
        Role.FIRST_USER -> Res.string.data_role_info_first_user
        else -> null
    }
}

fun Requirement.resourceString(): StringResource {
    return getRequirementResourceString(this.guid)
}

private fun getRequirementResourceString(guid: String): StringResource {
    return when (guid) {
        RequirementMapping.DRILL.value,
        RequirementMapping.TRAINING.value -> Res.string.data_requirement_training
        RequirementMapping.KFZ_3.value,
        RequirementMapping.KFZ_2.value,
        RequirementMapping.KFZ.value -> Res.string.data_requirement_kfz

        RequirementMapping.VEHICLE.value -> Res.string.data_requirement_vehicle
        RequirementMapping.SEW.value -> Res.string.data_requirement_sew
        RequirementMapping.RTW.value -> Res.string.data_requirement_rtw
        RequirementMapping.ITF.value -> Res.string.data_requirement_itf
        RequirementMapping.HAEND.value -> Res.string.data_requirement_haend
        RequirementMapping.EL.value -> Res.string.data_requirement_el
        RequirementMapping.TF.value -> Res.string.data_requirement_tf
        RequirementMapping.RS.value -> Res.string.data_requirement_rs
        RequirementMapping.HAEND_EL.value -> Res.string.data_requirement_haend_el
        RequirementMapping.HAEND_DR.value -> Res.string.data_requirement_haend_dr
        RequirementMapping.ITF_LKW.value -> Res.string.data_requirement_itf_el
        RequirementMapping.ITF_NFS.value -> Res.string.data_requirement_itf_nfs
        RequirementMapping.RTW_NFS.value -> Res.string.data_requirement_rtw_nfs
        RequirementMapping.RTW_RS.value -> Res.string.data_requirement_rtw_rs
        else -> Res.string.data_requirement_none
    }
}

fun Skill.resourceString(): StringResource {
    return when (this.guid) {
        MappedSkills.RS.value -> Res.string.data_skill_rs
        MappedSkills.AZUBI.value -> Res.string.data_skill_azubi
        MappedSkills.HAEND.value -> Res.string.data_skill_haend
        MappedSkills.FK.value -> Res.string.data_skill_fk
        MappedSkills.PA.value -> Res.string.data_skill_pa
        MappedSkills.SEF.value -> Res.string.data_skill_sef
        MappedSkills.NOTKOMPETENZ.value -> Res.string.data_skill_nkv
        MappedSkills.NFS.value -> Res.string.data_skill_nfs
        else -> Res.string.data_requirement_none
    }
}