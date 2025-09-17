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
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SlotProto
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.ui.theme.Yellow
import me.emiliomini.dutyschedule.util.allRequirementsMet
import me.emiliomini.dutyschedule.util.format
import me.emiliomini.dutyschedule.util.getIcon
import me.emiliomini.dutyschedule.util.isNotEqual
import me.emiliomini.dutyschedule.util.resourceString
import me.emiliomini.dutyschedule.util.staffRequirementsMet
import me.emiliomini.dutyschedule.util.toEpochMilli

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDutyCard(
    modifier: Modifier = Modifier,
    duty: DutyDefinitionProto,
    onEmployeeClick: (SlotProto) -> Unit = {},
    onDutyClick: (String?, RequirementProto) -> Unit = { _, _ -> },
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

    val requirementsMetError = duty.allRequirementsMet()
    val requirementsMetWarn = duty.staffRequirementsMet()

    val selfId = DutyScheduleService.self?.guid
    val isAssignedInDuty = duty.slotsList.any { it.employeeGuid == selfId }

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
                duty.slotsList.forEachIndexed { index, slot ->
                    AppPersonnelInfo(
                        modifier = Modifier.clickable(onClick = {
                            if (slot.guid.isNotBlank() && slot.employeeGuid.isBlank() && canSelfAssign) {
                                onDutyClick(slot.guid, slot.requirement)
                            } else {
                                onEmployeeClick(slot)
                            }
                        }),
                        icon = if (index == 0 || duty.slotsList[index - 1].requirement.guid != slot.requirement.guid) slot.requirement.getIcon() else null,
                        employeeGuid = if (slot.hasEmployeeGuid()) slot.employeeGuid else if (Requirement.VEHICLES.contains(slot.requirement.guid)) emptyCar.guid else emptySeat.guid,
                        fallbackEmployee = if (slot.hasEmployeeGuid()) slot.inlineEmployee else if (Requirement.VEHICLES.contains(slot.requirement.guid)) emptyCar.toBuilder().setIdentifier(stringResource(slot.requirement.resourceString())).build() else emptySeat.toBuilder().setIdentifier(stringResource(slot.requirement.resourceString())).build(),
                        state = if (slot.employeeGuid == selfId) PersonnelInfoState.HIGHLIGHTED else if (slot.hasEmployeeGuid()) PersonnelInfoState.DEFAULT else PersonnelInfoState.DISABLED,
                        showInfoBadge = slot.info.isNotEmpty(),
                        info = slot.info,
                        customBegin = if (slot.begin.isNotEqual(duty.begin) || slot.end.isNotEqual(
                                duty.end
                            )
                        ) slot.begin else null,
                        customEnd = if (slot.begin.isNotEqual(duty.begin) || slot.end.isNotEqual(
                                duty.end
                            )
                        ) slot.end else null
                    )
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
