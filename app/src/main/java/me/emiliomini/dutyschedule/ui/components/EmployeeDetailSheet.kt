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
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Class
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.employee.AssignedEmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeItemsProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.models.prep.Skill
import me.emiliomini.dutyschedule.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.services.storage.ProtoMapViewModel
import me.emiliomini.dutyschedule.services.storage.ProtoMapViewModelFactory
import me.emiliomini.dutyschedule.services.storage.ViewModelKeys
import me.emiliomini.dutyschedule.util.format
import me.emiliomini.dutyschedule.util.getIcon
import me.emiliomini.dutyschedule.util.resourceString
import java.time.OffsetDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmployeeDetailSheet(
    assignedEmployee: AssignedEmployeeProto?,
    orgs: List<OrgProto>,
    onDismiss: () -> Unit,
    viewModel: ProtoMapViewModel<EmployeeItemsProto, EmployeeProto> = viewModel(
        key = ViewModelKeys.EMPLOYEES,
        factory = ProtoMapViewModelFactory<EmployeeItemsProto, EmployeeProto>(DataStores.EMPLOYEES) { it.employeesMap }
    )
) {
    val employees by viewModel.flow.collectAsStateWithLifecycle(
        initialValue = emptyMap<String, EmployeeProto>()
    )

    var employee by remember { mutableStateOf<EmployeeProto?>(null) }

    LaunchedEffect(assignedEmployee, employees) {
        if (assignedEmployee != null) {
            employee = employees[assignedEmployee.employeeGuid] ?: assignedEmployee.inlineEmployee
        }
    }

    if (employee == null) return
    val sheetState = rememberModalBottomSheetState()
    val timeFormatter = "HH:mm"
    val detailActionsScrollState = rememberScrollState()
    val context = LocalContext.current

    if (employee != null && assignedEmployee != null) {
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    employee!!.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    employee!!.identifier ?: "",
                    modifier = Modifier.padding(bottom = 4.dp),
                    style = MaterialTheme.typography.bodyMediumEmphasized
                )
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (assignedEmployee.info.isNotBlank()) {
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
                                    Text(assignedEmployee.info)
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
                                Icon(
                                    assignedEmployee.requirement.getIcon(),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            headlineContent = {
                                Text(
                                    stringResource(assignedEmployee.requirement.resourceString())
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
                                        assignedEmployee.begin.format(timeFormatter)
                                    } - ${
                                        assignedEmployee.end.format(timeFormatter)
                                    }"
                                )
                            },
                            supportingContent = {
                                Text(
                                    assignedEmployee.begin.format("d MMMM yyyy")
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
                    DutyScheduleService.getMessages()[employee!!.guid]
                        ?: emptyList()
                val filteredMessages = messages.filter {
                    (it.displayTo == null ||
                            it.displayTo.toInstant()
                                .toEpochMilli() >= nowMillis) &&
                            (it.displayFrom == null ||
                                    it.displayFrom.toInstant()
                                        .toEpochMilli() <= nowMillis)
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
                        if (employee!!.defaultOrg.isNotBlank()) {
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
                                        orgs.firstOrNull { it.abbreviation == employee!!.defaultOrg }
                                    if (primaryOrg == null) {
                                        primaryOrg =
                                            orgs.firstOrNull { it.identifier == employee!!.defaultOrg }
                                    }

                                    Text(if (primaryOrg != null) primaryOrg.title else employee!!.defaultOrg)
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.main_schedule_infobox_primary))
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                        if (employee!!.skillsList.isNotEmpty()) {

                            val skills = employee!!.skillsList
                            val filteredSkills =
                                skills.filter { Skill.parse(it.guid) != Skill.INVALID }
                                    .toMutableList()

                            if (skills.contains(Skill.RS.asProto()) && skills.contains(Skill.AZUBI.asProto())) {
                                filteredSkills.remove(Skill.AZUBI.asProto())
                            }
                            if (skills.contains(Skill.NFS.asProto()) && skills.contains(Skill.RS.asProto())) {
                                filteredSkills.remove(Skill.RS.asProto())
                            }
                            val skillTxt =
                                filteredSkills.map { stringResource(it.resourceString()) }
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
                        if (employee!!.hasBirthdate()) {
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
                                        employee!!.birthdate!!.format("d MMMM yyyy")
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
                if (assignedEmployee.requirement.guid != Requirement.SEW.value) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(detailActionsScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                val uri = "tel:${employee!!.phone}".toUri()
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
                            enabled = employee!!.phone.isNotBlank()
                        )
                        AssistChip(
                            onClick = {
                                val uri = "smsto:${employee!!.phone}".toUri()
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
                            enabled = employee!!.phone.isNotBlank()
                        )
                        AssistChip(
                            onClick = {
                                val fullNumber =
                                    employee!!.phone.filter { it.isDigit() }
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
                            enabled = employee!!.phone.isNotBlank()
                        )
                        AssistChip(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:".toUri()
                                    putExtra(
                                        Intent.EXTRA_EMAIL,
                                        arrayOf(employee!!.email)
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
                            enabled = employee!!.email.isNotBlank()
                        )
                    }
                }
            }
        }
    }
}
