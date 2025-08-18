package me.emiliomini.dutyschedule.ui.base

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
import androidx.compose.material.icons.outlined.AlarmAdd
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.data.models.AssignedEmployee
import me.emiliomini.dutyschedule.data.models.DutyDefinition
import me.emiliomini.dutyschedule.data.models.Employee
import me.emiliomini.dutyschedule.icons.Ambulance
import me.emiliomini.dutyschedule.icons.SteeringWheel
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDutyCard(
    modifier: Modifier = Modifier,
    duty: DutyDefinition,
    selfId: String? = null,
    onEmployeeClick: (AssignedEmployee) -> Unit = {}
) {
    val emptyCar = Employee("", stringResource(R.string.base_dutycard_no_vehicle), "SEW")
    val emptySeat = Employee("", stringResource(R.string.base_dutycard_no_staff), "0000000")

    // TODO: Make sure that all slots are filled completely and not just partially
    val requirementsMet = duty.sew.isNotEmpty() && duty.el.isNotEmpty() && duty.tf.isNotEmpty()

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val localZoneId = ZoneId.systemDefault()

    val startTime = duty.begin.atZoneSameInstant(localZoneId).format(timeFormatter)
    val endTime = duty.end.atZoneSameInstant(localZoneId).format(timeFormatter)

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
                        .fillMaxHeight(0.8f) // Adjust fraction for line length
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
                if (duty.sew.isEmpty()) {
                    AppPersonnelInfo(
                        icon = Ambulance, employee = emptyCar, state = PersonnelInfoState.DISABLED
                    )
                } else {
                    duty.sew.forEachIndexed { index, assigned ->
                        AppPersonnelInfo(
                            modifier = Modifier.clickable(onClick = { onEmployeeClick(assigned) }),
                            icon = if (index == 0) Ambulance else null,
                            employee = assigned.employee,
                            state = PersonnelInfoState.DEFAULT,
                            showInfoBadge = assigned.info.isNotEmpty(),
                            info = if (!assigned.begin.isEqual(duty.begin) || !assigned.end.isEqual(
                                    duty.end
                                )
                            ) {
                                "${
                                    assigned.begin.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                } - ${
                                    assigned.end.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                }"
                            } else {
                                null
                            }
                        )
                    }
                }

                if (duty.el.isEmpty()) {
                    AppPersonnelInfo(
                        icon = SteeringWheel,
                        employee = emptySeat,
                        state = PersonnelInfoState.DISABLED
                    )
                } else {
                    duty.el.forEachIndexed { index, assigned ->
                        AppPersonnelInfo(
                            modifier = Modifier.clickable(onClick = { onEmployeeClick(assigned) }),
                            icon = if (index == 0) SteeringWheel else null,
                            employee = assigned.employee,
                            state = if (selfId != null && assigned.employee.identifier == selfId) PersonnelInfoState.HIGHLIGHTED else PersonnelInfoState.DEFAULT,
                            showInfoBadge = assigned.info.isNotEmpty(),
                            info = if (!assigned.begin.isEqual(duty.begin) || !assigned.end.isEqual(
                                    duty.end
                                )
                            ) {
                                "${
                                    assigned.begin.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                } - ${
                                    assigned.end.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                }"
                            } else {
                                null
                            }
                        )
                    }
                }

                if (duty.tf.isEmpty()) {
                    AppPersonnelInfo(
                        icon = Icons.Rounded.MedicalInformation,
                        employee = emptySeat,
                        state = PersonnelInfoState.DISABLED
                    )
                } else {
                    duty.tf.forEachIndexed { index, assigned ->
                        AppPersonnelInfo(
                            modifier = Modifier.clickable(onClick = { onEmployeeClick(assigned) }),
                            icon = if (index == 0) Icons.Rounded.MedicalInformation else null,
                            employee = assigned.employee,
                            state = if (selfId != null && assigned.employee.identifier == selfId) PersonnelInfoState.HIGHLIGHTED else PersonnelInfoState.DEFAULT,
                            showInfoBadge = assigned.info.isNotEmpty(),
                            info = if (!assigned.begin.isEqual(duty.begin) || !assigned.end.isEqual(
                                    duty.end
                                )
                            ) {
                                "${
                                    assigned.begin.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                } - ${
                                    assigned.end.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                }"
                            } else {
                                null
                            }
                        )
                    }
                }

                if (duty.rs.isEmpty()) {
                    AppPersonnelInfo(
                        icon = Icons.Rounded.Badge,
                        employee = emptySeat,
                        state = PersonnelInfoState.DISABLED
                    )
                } else {
                    duty.rs.forEachIndexed { index, assigned ->
                        AppPersonnelInfo(
                            modifier = Modifier.clickable(onClick = { onEmployeeClick(assigned) }),
                            icon = if (index == 0) Icons.Rounded.Badge else null,
                            employee = assigned.employee,
                            state = if (selfId != null && assigned.employee.identifier == selfId) PersonnelInfoState.HIGHLIGHTED else PersonnelInfoState.DEFAULT,
                            showInfoBadge = assigned.info.isNotEmpty(),
                            info = if (!assigned.begin.isEqual(duty.begin) || !assigned.end.isEqual(
                                    duty.end
                                )
                            ) {
                                "${
                                    assigned.begin.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                } - ${
                                    assigned.end.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                }"
                            } else {
                                null
                            }
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = {
                    // AlarmService.scheduleAlarm(context, duty.begin.toInstant().toEpochMilli());
                }, enabled = false) {
                    Icon(Icons.Outlined.AlarmAdd, contentDescription = "Set Reminder")
                }
                if (!requirementsMet) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = stringResource(R.string.base_dutycard_accessibility_requirements_issue),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun AppDutyCardPreview(modifier: Modifier = Modifier) {
    AppDutyCard(
        modifier = modifier,
        duty = DutyDefinition("", OffsetDateTime.now(), OffsetDateTime.now()),
        selfId = "00023456"
    )
}
