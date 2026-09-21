package me.emiliomini.dutyschedule.shared.util

import androidx.compose.ui.graphics.vector.ImageVector
import me.emiliomini.dutyschedule.shared.datastores.DutyType
import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.ui.icons.Ambulance
import me.emiliomini.dutyschedule.shared.ui.icons.Badge
import me.emiliomini.dutyschedule.shared.ui.icons.Coffee
import me.emiliomini.dutyschedule.shared.ui.icons.Drone
import me.emiliomini.dutyschedule.shared.ui.icons.EcgHeart
import me.emiliomini.dutyschedule.shared.ui.icons.EmojiPeople
import me.emiliomini.dutyschedule.shared.ui.icons.Exercise
import me.emiliomini.dutyschedule.shared.ui.icons.Festival
import me.emiliomini.dutyschedule.shared.ui.icons.MedicalInformation
import me.emiliomini.dutyschedule.shared.ui.icons.MedicalServices
import me.emiliomini.dutyschedule.shared.ui.icons.QuestionMark
import me.emiliomini.dutyschedule.shared.ui.icons.School
import me.emiliomini.dutyschedule.shared.ui.icons.SteeringWheel
import me.emiliomini.dutyschedule.shared.ui.icons.Stethoscope
import me.emiliomini.dutyschedule.shared.ui.icons.Syringe
import me.emiliomini.dutyschedule.shared.ui.icons.VolunteerActivism

fun DutyType.getIcon(): ImageVector {
    return when (this) {
        DutyType.EMS -> Ambulance
        DutyType.TRAINING -> School
        DutyType.MEET -> EmojiPeople
        DutyType.DRILL -> Exercise
        DutyType.VEHICLE_TRAINING -> SteeringWheel
        DutyType.RECERTIFICATION -> EcgHeart
        DutyType.HAEND -> MedicalServices
        DutyType.ADMINISTRATIVE -> Coffee
        DutyType.EVENT -> Festival
        DutyType.BLOOD_DONATION_SERVICE -> VolunteerActivism
        DutyType.DRONE_TEAM -> Drone
        else -> QuestionMark
    }
}

fun Requirement.getIcon(): ImageVector {
    if (RequirementMapping.VEHICLES.contains(this.guid)) {
        return Ambulance
    }

    return when (this.guid) {
        RequirementMapping.EL.value, RequirementMapping.HAEND_EL.value, RequirementMapping.RTW_RS.value, RequirementMapping.ITF_LKW.value -> SteeringWheel

        RequirementMapping.TF.value -> MedicalInformation

        RequirementMapping.HAEND_DR.value -> Stethoscope

        RequirementMapping.ITF_NFS.value, RequirementMapping.RTW_NFS.value -> Syringe

        RequirementMapping.DRILL.value, RequirementMapping.TRAINING.value -> School

        else -> Badge
    }
}
