package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.employee.AssignedEmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.ui.components.icons.Ambulance
import me.emiliomini.dutyschedule.ui.components.icons.SteeringWheel
import me.emiliomini.dutyschedule.ui.theme.Yellow
import me.emiliomini.dutyschedule.util.format
import me.emiliomini.dutyschedule.util.getIcon
import me.emiliomini.dutyschedule.util.isNotEqual
import me.emiliomini.dutyschedule.util.toEpochMilli

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDutyCard(
    modifier: Modifier = Modifier,
    duty: DutyDefinitionProto,
    onEmployeeClick: (AssignedEmployeeProto) -> Unit = {},
    onDutyClick: (String?) -> Unit = {}
) {
    val emptyCar = EmployeeProto.newBuilder()
        .setGuid("")
        .setName(stringResource(R.string.base_dutycard_no_vehicle))
        .setIdentifier("SEW")
        .build()
    val emptySeat = EmployeeProto.newBuilder()
        .setGuid("")
        .setName(stringResource(R.string.base_dutycard_no_staff))
        .setIdentifier("0000000")
        .build()

    val requirementsMetError =
        (duty.elList.isNotEmpty() && duty.tfList.isNotEmpty()) || (duty.elList.isNotEmpty() && !duty.tfList.any { person -> person.requirement.guid == Requirement.HAEND.value })
    val requirementsMetWarn =
        duty.sewList.isNotEmpty() && duty.elList.isNotEmpty() && duty.tfList.isNotEmpty() || (duty.elList.isNotEmpty() && !duty.tfList.any { person -> person.requirement.guid == Requirement.HAEND.value })

    val selfId = DutyScheduleService.self?.guid
    val isAssignedInDuty = duty.elList.any { it.employeeGuid == selfId } ||
            duty.tfList.any { it.employeeGuid == selfId } ||
            duty.rsList.any { it.employeeGuid == selfId }

    val timeFormatter = "HH:mm"

    val startTime = duty.begin.format(timeFormatter)
    val endTime = duty.end.format(timeFormatter)

    val canSelfAssign = !isAssignedInDuty && duty.begin.toEpochMilli() >= System.currentTimeMillis()

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = startTime,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    fontWeight = FontWeight.Light
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.8f)
                        .background(color = MaterialTheme.colorScheme.outline)
                )
                Text(
                    text = endTime,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    fontWeight = FontWeight.Light
                )
            }
            Column(
                Modifier
                    .padding(16.dp)
                    .weight(2f), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (duty.sewList.isEmpty()) {
                    AppPersonnelInfo(
                        icon = Ambulance,
                        employeeGuid = emptyCar.guid,
                        fallbackEmployee = emptyCar,
                        state = PersonnelInfoState.DISABLED
                    )
                } else {
                    duty.sewList.forEachIndexed { index, assigned ->
                        AppPersonnelInfo(
                            modifier = Modifier.clickable(onClick = { onEmployeeClick(assigned) }),
                            icon = if (index == 0) assigned.requirement.getIcon() else null,
                            employeeGuid = assigned.employeeGuid,
                            fallbackEmployee = assigned.inlineEmployee,
                            state = PersonnelInfoState.DEFAULT,
                            showInfoBadge = assigned.info.isNotEmpty(),
                            info = assigned.info,
                            customBegin = if (assigned.begin.isNotEqual(duty.begin) || assigned.end.isNotEqual(duty.end)) assigned.begin else null,
                            customEnd = if (assigned.begin.isNotEqual(duty.begin) || assigned.end.isNotEqual(duty.end)) assigned.end else null
                        )
                    }
                }

                if (duty.elList.isEmpty()) {
                    AppPersonnelInfo(
                        icon = SteeringWheel,
                        employeeGuid = emptySeat.guid,
                        fallbackEmployee = emptySeat,
                        state = PersonnelInfoState.DISABLED,
                        modifier = if (!duty.elSlotId.isNullOrBlank() && canSelfAssign) Modifier.clickable(onClick = {
                            onDutyClick(
                                duty.elSlotId
                            )
                        }) else Modifier,
                    )
                } else {
                    duty.elList.forEachIndexed { index, assigned ->
                        AppPersonnelInfo(
                            modifier = Modifier.clickable(onClick = { onEmployeeClick(assigned) }),
                            icon = if (index == 0) assigned.requirement.getIcon() else null,
                            employeeGuid = assigned.employeeGuid,
                            fallbackEmployee = assigned.inlineEmployee,
                            state = if (assigned.employeeGuid == selfId) PersonnelInfoState.HIGHLIGHTED else PersonnelInfoState.DEFAULT,
                            showInfoBadge = assigned.info.isNotEmpty(),
                            info = assigned.info,
                            customBegin = if (assigned.begin.isNotEqual(duty.begin) || assigned.end.isNotEqual(duty.end)) assigned.begin else null,
                            customEnd = if (assigned.begin.isNotEqual(duty.begin) || assigned.end.isNotEqual(duty.end)) assigned.end else null
                        )
                    }
                }

                if (duty.tfList.isEmpty()) {
                    AppPersonnelInfo(
                        icon = Icons.Rounded.MedicalInformation,
                        employeeGuid = emptySeat.guid,
                        fallbackEmployee = emptySeat,
                        state = PersonnelInfoState.DISABLED,
                        modifier = if (!duty.tfSlotId.isNullOrBlank() && canSelfAssign) Modifier.clickable(onClick = {
                            onDutyClick(
                                duty.tfSlotId
                            )
                        }) else Modifier,
                    )
                } else {
                    duty.tfList.forEachIndexed { index, assigned ->
                        AppPersonnelInfo(
                            modifier = Modifier.clickable(onClick = { onEmployeeClick(assigned) }),
                            icon = if (index == 0) assigned.requirement.getIcon() else null,
                            employeeGuid = assigned.employeeGuid,
                            fallbackEmployee = assigned.inlineEmployee,
                            state = if (assigned.employeeGuid == selfId) PersonnelInfoState.HIGHLIGHTED else PersonnelInfoState.DEFAULT,
                            showInfoBadge = assigned.info.isNotEmpty() && assigned.requirement.guid != Requirement.HAEND_DR.value,
                            info = assigned.info,
                            customBegin = if (assigned.begin.isNotEqual(duty.begin) || assigned.end.isNotEqual(duty.end)) assigned.begin else null,
                            customEnd = if (assigned.begin.isNotEqual(duty.begin) || assigned.end.isNotEqual(duty.end)) assigned.end else null
                        )
                    }
                }

                if (duty.rsList.isEmpty()) {
                    AppPersonnelInfo(
                        icon = Icons.Rounded.Badge,
                        employeeGuid = emptySeat.guid,
                        fallbackEmployee = emptySeat,
                        state = PersonnelInfoState.DISABLED,
                        modifier = if (!duty.rsSlotId.isNullOrBlank() && canSelfAssign) Modifier.clickable(onClick = {
                            onDutyClick(
                                duty.rsSlotId
                            )
                        }) else Modifier
                    )
                } else {
                    duty.rsList.forEachIndexed { index, assigned ->
                        AppPersonnelInfo(
                            modifier = Modifier.clickable(onClick = { onEmployeeClick(assigned) }),
                            icon = if (index == 0) assigned.requirement.getIcon() else null,
                            employeeGuid = assigned.employeeGuid,
                            fallbackEmployee = assigned.inlineEmployee,
                            state = if (assigned.employeeGuid == selfId) PersonnelInfoState.HIGHLIGHTED else PersonnelInfoState.DEFAULT,
                            showInfoBadge = assigned.info.isNotEmpty(),
                            info = assigned.info,
                            customBegin = if (assigned.begin.isNotEqual(duty.begin) || assigned.end.isNotEqual(duty.end)) assigned.begin else null,
                            customEnd = if (assigned.begin.isNotEqual(duty.begin) || assigned.end.isNotEqual(duty.end)) assigned.end else null
                        )
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(48.dp)
                    .padding(top = 16.dp, end = 16.dp)
            ) {
                if (!requirementsMetWarn || !requirementsMetError) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = stringResource(R.string.base_dutycard_accessibility_requirements_issue),
                        tint = if (!requirementsMetError) MaterialTheme.colorScheme.error else Yellow
                    )
                }
            }
        }
    }
}
