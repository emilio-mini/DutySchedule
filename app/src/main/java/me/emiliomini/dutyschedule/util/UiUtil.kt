package me.emiliomini.dutyschedule.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.models.app.Role
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.ui.components.icons.Ambulance
import me.emiliomini.dutyschedule.ui.components.icons.SteeringWheel
import me.emiliomini.dutyschedule.ui.components.icons.Stethoscope
import me.emiliomini.dutyschedule.ui.components.icons.Syringe
import kotlin.math.cos
import kotlin.math.sin

fun RequirementProto.getIcon(): ImageVector {
    if (Requirement.VEHICLES.contains(this.guid)) {
        return Ambulance
    }

    return when (this.guid) {
        Requirement.EL.value, Requirement.HAEND_EL.value, Requirement.RTW_RS.value, Requirement.ITF_LKW.value -> SteeringWheel

        Requirement.TF.value -> Icons.Rounded.MedicalInformation

        Requirement.HAEND_DR.value -> Stethoscope

        Requirement.ITF_NFS.value, Requirement.RTW_NFS.value -> Syringe

        Requirement.TRAINING.value -> Icons.Rounded.School

        else -> Icons.Rounded.Badge
    }
}
