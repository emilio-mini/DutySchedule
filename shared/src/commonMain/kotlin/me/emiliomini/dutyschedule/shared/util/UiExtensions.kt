package me.emiliomini.dutyschedule.shared.util

import androidx.compose.ui.graphics.vector.ImageVector
import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.ui.icons.Ambulance
import me.emiliomini.dutyschedule.shared.ui.icons.Badge
import me.emiliomini.dutyschedule.shared.ui.icons.MedicalInformation
import me.emiliomini.dutyschedule.shared.ui.icons.School
import me.emiliomini.dutyschedule.shared.ui.icons.SteeringWheel
import me.emiliomini.dutyschedule.shared.ui.icons.Stethoscope
import me.emiliomini.dutyschedule.shared.ui.icons.Syringe

fun Requirement.getIcon(): ImageVector {
    if (RequirementMapping.VEHICLES.contains(this.guid)) {
        return Ambulance
    }

    return when (this.guid) {
        RequirementMapping.EL.value, RequirementMapping.HAEND_EL.value, RequirementMapping.RTW_RS.value, RequirementMapping.ITF_LKW.value -> SteeringWheel

        RequirementMapping.TF.value -> MedicalInformation

        RequirementMapping.HAEND_DR.value -> Stethoscope

        RequirementMapping.ITF_NFS.value, RequirementMapping.RTW_NFS.value -> Syringe

        RequirementMapping.TRAINING.value -> School

        else -> Badge
    }
}
