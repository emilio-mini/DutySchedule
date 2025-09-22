@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.components

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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.base_dutycard_accessibility_requirements_issue
import dutyschedule.shared.generated.resources.base_dutycard_no_staff
import dutyschedule.shared.generated.resources.base_dutycard_no_vehicle
import me.emiliomini.dutyschedule.shared.datastores.DutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.mappings.MappedSkills
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.ui.icons.Warning
import me.emiliomini.dutyschedule.shared.util.allRequirementsMet
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.getIcon
import me.emiliomini.dutyschedule.shared.util.isNotNullOrBlank
import me.emiliomini.dutyschedule.shared.util.resourceString
import me.emiliomini.dutyschedule.shared.util.staffRequirementsMet
import me.emiliomini.dutyschedule.shared.util.toEpochMilliseconds
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDutyCard(
    modifier: Modifier = Modifier,
    duty: DutyDefinition,
    onEmployeeClick: (Slot) -> Unit = {},
    onDutyClick: (String?, Requirement) -> Unit = { _, _ -> },
) {
    val emptyCar = Employee(name = stringResource(Res.string.base_dutycard_no_vehicle))
    val emptySeat = Employee(name = stringResource(Res.string.base_dutycard_no_staff))

    val requirementsMetError = duty.allRequirementsMet()
    val requirementsMetWarn = duty.staffRequirementsMet()

    val selfId = DutyScheduleService.self?.guid
    val selfSkills = DutyScheduleService.self?.skills
    val isAssignedInDuty = duty.slots.any { it.employeeGuid == selfId }

    val timeFormatter = "HH:mm"

    val startTime = duty.begin.format(timeFormatter)
    val endTime = duty.end.format(timeFormatter)

    val canSelfAssign = !isAssignedInDuty && duty.begin.toEpochMilliseconds() >= Clock.System.now()
        .toEpochMilliseconds()

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier.padding(16.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = startTime,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    fontWeight = FontWeight.Light
                )
                Box(
                    modifier = Modifier.width(1.dp).fillMaxHeight(0.8f)
                        .background(color = MaterialTheme.colorScheme.outline)
                )
                Text(
                    text = endTime,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                    fontWeight = FontWeight.Light
                )
            }
            Column(
                Modifier.padding(16.dp).weight(2f), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                duty.slots.forEachIndexed { index, slot ->
                    AppPersonnelInfo(
                        modifier = Modifier.clickable(onClick = {
                            if (slot.guid.isNotBlank() && slot.employeeGuid.isNullOrBlank() && !slot.info?.uppercase()
                                    ?.contains("SD")
                                    .let { it ?: false } && !RequirementMapping.VEHICLES.contains(
                                    slot.requirement.guid
                                ) && canSelfAssign
                            ) {

                                if (RequirementMapping.NFS_SLOTS.contains(slot.requirement.guid)) {
                                    if (selfSkills?.contains(MappedSkills.NFS.asSkill()) == true) {
                                        onDutyClick(slot.guid, slot.requirement)
                                    }
                                } else {
                                    onDutyClick(slot.guid, slot.requirement)
                                }
                            } else {
                                onEmployeeClick(slot)
                            }
                        }),
                        icon = if (index == 0 || duty.slots[index - 1].requirement.guid != slot.requirement.guid) slot.requirement.getIcon() else null,
                        employeeGuid = if (slot.employeeGuid.isNotNullOrBlank()) slot.employeeGuid else if (RequirementMapping.VEHICLES.contains(
                                slot.requirement.guid
                            )
                        ) emptyCar.guid else emptySeat.guid,
                        fallbackEmployee = if (slot.employeeGuid.isNotNullOrBlank()) slot.inlineEmployee else if (RequirementMapping.VEHICLES.contains(
                                slot.requirement.guid
                            )
                        ) emptyCar.copy(identifier = stringResource(slot.requirement.resourceString())) else emptySeat.copy(
                            identifier = stringResource(slot.requirement.resourceString())
                        ),
                        state = if (slot.employeeGuid == selfId) PersonnelInfoState.HIGHLIGHTED else if (slot.employeeGuid.isNotNullOrBlank()) PersonnelInfoState.DEFAULT else PersonnelInfoState.DISABLED,
                        showInfoBadge = slot.info.isNotNullOrBlank(),
                        info = slot.info,
                        customBegin = if (slot.begin != duty.begin || slot.end != duty.end) slot.begin else null,
                        customEnd = if (slot.begin != duty.begin || slot.end != duty.end) slot.end else null
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(48.dp).padding(top = 16.dp, end = 16.dp)
            ) {
                if (!requirementsMetWarn || !requirementsMetError) {
                    Icon(
                        Warning,
                        contentDescription = stringResource(Res.string.base_dutycard_accessibility_requirements_issue),
                        tint = if (!requirementsMetError) MaterialTheme.colorScheme.error else Yellow
                    )
                }
            }
        }
    }
}
