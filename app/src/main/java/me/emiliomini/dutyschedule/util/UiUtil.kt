package me.emiliomini.dutyschedule.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.ui.components.icons.Ambulance
import me.emiliomini.dutyschedule.ui.components.icons.SteeringWheel
import me.emiliomini.dutyschedule.ui.components.icons.Stethoscope
import me.emiliomini.dutyschedule.ui.components.icons.Syringe

fun RequirementProto.getIcon(): ImageVector {
    return when (this.guid) {
        Requirement.VEHICLE.value,
        Requirement.SEW.value,
        Requirement.RTW.value,
        Requirement.ITF.value,
        Requirement.HAEND.value -> Ambulance

        Requirement.EL.value,
        Requirement.HAEND_EL.value,
        Requirement.ITF_LKW.value -> SteeringWheel

        Requirement.TF.value -> Icons.Rounded.MedicalInformation

        Requirement.HAEND_DR.value -> Stethoscope

        Requirement.ITF_NFS.value,
        Requirement.RTW_NFS.value -> Syringe

        Requirement.TRAINING.value -> Icons.Rounded.School

        else -> Icons.Rounded.Badge
    }
}
