package me.emiliomini.dutyschedule.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material.icons.rounded.Whatsapp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.prep.AssignedEmployee
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.models.prep.Skill
import me.emiliomini.dutyschedule.services.network.PrepService
import me.emiliomini.dutyschedule.ui.components.icons.Ambulance
import me.emiliomini.dutyschedule.ui.components.icons.SteeringWheel
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmployeeDetailSheet(
    employee: AssignedEmployee?,
    orgs: List<OrgProto>,
    onDismiss: () -> Unit
) {
    if (employee == null) return
    val sheetState = rememberModalBottomSheetState()
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val localZoneId = ZoneId.systemDefault()
    val detailActionsScrollState = rememberScrollState()
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {

        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                employee.employee.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                employee.employee.identifier ?: "",
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.bodyMediumEmphasized
            )
            Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (employee.info.isNotBlank()) {
                        ListItem(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            leadingContent = {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null
                                )
                            },
                            headlineContent = {
                                Text(employee.info)
                            },
                            supportingContent = {
                                Text(stringResource(R.string.main_schedule_infobox_info))
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                    ListItem(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        leadingContent = {
                            when (employee.requirement) {
                                Requirement.SEW -> Icon(
                                    Ambulance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Requirement.EL -> Icon(
                                    SteeringWheel,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                Requirement.TF -> Icon(
                                    Icons.Rounded.MedicalInformation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )

                                else -> Icon(
                                    Icons.Rounded.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        headlineContent = {
                            Text(
                                stringResource(employee.requirement.getResourceString())
                            )
                        },
                        supportingContent = {
                            Text(stringResource(R.string.main_schedule_infobox_assigned_role))
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                    ListItem(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        leadingContent = {
                            Icon(
                                Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        headlineContent = {
                            Text(
                                "${
                                    employee.begin.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                } - ${
                                    employee.end.atZoneSameInstant(localZoneId)
                                        .format(timeFormatter)
                                }"
                            )
                        },
                        supportingContent = {
                            Text(
                                employee.begin.format(
                                    DateTimeFormatter.ofPattern("d MMMM yyyy")
                                )
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            }

            val nowMillis = OffsetDateTime.now().toInstant().toEpochMilli()
            val messages =
                PrepService.getMessages()[employee.employee.resourceTypeGuid]
                    ?: emptyList()
            val filteredMessages = messages.filter {
                it.displayFrom.toInstant()
                    .toEpochMilli() <= nowMillis && it.displayTo.toInstant()
                    .toEpochMilli() >= nowMillis
            }
            if (filteredMessages.isNotEmpty()) {
                Spacer(Modifier)
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (message in messages) {

                            ListItem(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                leadingContent = {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.Notes,
                                        contentDescription = null
                                    )
                                },
                                headlineContent = {
                                    Text(message.message)
                                },
                                supportingContent = {
                                    Text(message.title)
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )

                        }
                    }
                }
            }
            Spacer(Modifier)
            Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (employee.employee.defaultOrg.isNotBlank()) {
                        ListItem(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            leadingContent = {
                                Icon(Icons.Rounded.Business, contentDescription = null)
                            },
                            headlineContent = {
                                var primaryOrg =
                                    orgs.firstOrNull { it.abbreviation == employee.employee.defaultOrg }
                                if (primaryOrg == null) {
                                    primaryOrg =
                                        orgs.firstOrNull { it.identifier == employee.employee.defaultOrg }
                                }

                                Text(if (primaryOrg != null) primaryOrg.title else employee.employee.defaultOrg)
                            },
                            supportingContent = {
                                Text(stringResource(R.string.main_schedule_infobox_primary))
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                    if (employee.employee.skill.isNotEmpty()) {

                        val skills = employee.employee.skill
                        val filteredSkills = skills.toMutableList()

                        if (skills.contains(Skill.RS) && skills.contains(Skill.AZUBI)) {
                            filteredSkills.remove(Skill.AZUBI)
                        }
                        if (skills.contains(Skill.NFS) && skills.contains(Skill.RS)) {
                            filteredSkills.remove(Skill.RS)
                        }
                        val skillTxt =
                            filteredSkills.map { stringResource(it.getResourceString()) }
                                .joinToString(", ")

                        ListItem(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            leadingContent = {
                                Icon(Icons.Rounded.Class, contentDescription = null)
                            },
                            headlineContent = {
                                Text(skillTxt)
                            },
                            supportingContent = {
                                Text(stringResource(R.string.main_schedule_infobox_skill))
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                    if (employee.employee.birthdate != null) {
                        ListItem(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            leadingContent = {
                                Icon(Icons.Rounded.Cake, contentDescription = null)
                            },
                            headlineContent = {
                                Text(
                                    employee.employee.birthdate!!.format(
                                        DateTimeFormatter.ofPattern("d MMMM yyyy")
                                    )
                                )
                            },
                            supportingContent = {
                                Text(stringResource(R.string.main_schedule_infobox_birthday))
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }
            }
            if (employee.requirement != Requirement.SEW) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(detailActionsScrollState),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {
                            val uri = "tel:${employee.employee.phone}".toUri()
                            val intent = Intent(Intent.ACTION_DIAL, uri)
                            context.startActivity(intent)
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    AssistChipDefaults.IconSize
                                )
                            )
                        },
                        label = {
                            Text(stringResource(R.string.main_schedule_infobox_actions_call))
                        },
                        enabled = employee.employee.phone.isNotBlank()
                    )
                    AssistChip(
                        onClick = {
                            val uri = "smsto:${employee.employee.phone}".toUri()
                            val intent = Intent(Intent.ACTION_SENDTO, uri)
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Textsms,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    AssistChipDefaults.IconSize
                                )
                            )
                        },
                        label = {
                            Text(stringResource(R.string.main_schedule_infobox_actions_sms))
                        },
                        enabled = employee.employee.phone.isNotBlank()
                    )
                    AssistChip(
                        onClick = {
                            val fullNumber =
                                employee.employee.phone.filter { it.isDigit() }
                            val uri =
                                "https://wa.me/$fullNumber/".toUri()

                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = uri
                                setPackage("com.whatsapp")
                            }

                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Whatsapp,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    AssistChipDefaults.IconSize
                                )
                            )
                        },
                        label = {
                            Text(stringResource(R.string.main_schedule_infobox_actions_whatsapp))
                        },
                        enabled = employee.employee.phone.isNotBlank()
                    )
                    AssistChip(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = "mailto:".toUri()
                                putExtra(
                                    Intent.EXTRA_EMAIL,
                                    arrayOf(employee.employee.email)
                                )
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                context.startActivity(intent)
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.AlternateEmail,
                                contentDescription = null,
                                modifier = Modifier.size(
                                    AssistChipDefaults.IconSize
                                )
                            )
                        },
                        label = {
                            Text(stringResource(R.string.main_schedule_infobox_actions_mail))
                        },
                        enabled = employee.employee.email.isNotBlank()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun EmployeeDetailSheetPreview() {
    EmployeeDetailSheet(
        employee = AssignedEmployee(
            employee = Employee(
                guid = "blah",
                name = "blah",
                identifier = "blah",
                phone = "blah",
                email = "blah",
                defaultOrg = "blah",
                birthdate = OffsetDateTime.now(),
                resourceTypeGuid = "blah",
                skill = mutableListOf()
            ),
            requirement = Requirement.RS,
            begin = OffsetDateTime.now(),
            end = OffsetDateTime.now(),
            info = "blah"
        ),
        orgs = emptyList(),
        onDismiss = {}
    )
}
