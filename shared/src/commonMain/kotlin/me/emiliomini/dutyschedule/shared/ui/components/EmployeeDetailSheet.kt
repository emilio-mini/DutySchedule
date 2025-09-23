@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_schedule_infobox_actions_call
import dutyschedule.shared.generated.resources.main_schedule_infobox_actions_mail
import dutyschedule.shared.generated.resources.main_schedule_infobox_actions_sms
import dutyschedule.shared.generated.resources.main_schedule_infobox_actions_whatsapp
import dutyschedule.shared.generated.resources.main_schedule_infobox_assigned_role
import dutyschedule.shared.generated.resources.main_schedule_infobox_birthday
import dutyschedule.shared.generated.resources.main_schedule_infobox_info
import dutyschedule.shared.generated.resources.main_schedule_infobox_primary
import dutyschedule.shared.generated.resources.main_schedule_infobox_skill
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.api.getPlatformClipboardApi
import me.emiliomini.dutyschedule.shared.api.getPlatformLogger
import me.emiliomini.dutyschedule.shared.api.getPlatformRedirectApi
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.Org
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.debug.DebugFlags
import me.emiliomini.dutyschedule.shared.mappings.MappedSkills
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.mappings.Role
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.icons.AlternateEmail
import me.emiliomini.dutyschedule.shared.ui.icons.BugReport
import me.emiliomini.dutyschedule.shared.ui.icons.Cake
import me.emiliomini.dutyschedule.shared.ui.icons.Call
import me.emiliomini.dutyschedule.shared.ui.icons.Domain
import me.emiliomini.dutyschedule.shared.ui.icons.Info
import me.emiliomini.dutyschedule.shared.ui.icons.Notes
import me.emiliomini.dutyschedule.shared.ui.icons.PersonPlay
import me.emiliomini.dutyschedule.shared.ui.icons.Schedule
import me.emiliomini.dutyschedule.shared.ui.icons.Sms
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.getIcon
import me.emiliomini.dutyschedule.shared.util.infoResourceString
import me.emiliomini.dutyschedule.shared.util.isNotNullOrBlank
import me.emiliomini.dutyschedule.shared.util.nullIfBlank
import me.emiliomini.dutyschedule.shared.util.resourceString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmployeeDetailSheet(
    slot: Slot?,
    orgs: List<Org>,
    onDismiss: () -> Unit
) {
    val employeeItems by StorageService.EMPLOYEES.collectAsState()

    val logger = getPlatformLogger("EmployeeDetailSheet")
    var employee by remember { mutableStateOf<Employee?>(null) }
    var source by remember { mutableStateOf("<none>") }

    LaunchedEffect(slot, employeeItems) {
        if (slot != null) {
            val local = employeeItems.employees[slot.employeeGuid]
            employee = if(local != null) {
                source = "Loaded from server"
                local
            } else {
                logger.d("No stored employee with id ${slot.employeeGuid} found in available ids: ${employeeItems.employees.keys.joinToString(";")}")
                source = "Inline"
                slot.inlineEmployee
            }
        }
    }

    if (employee == null) return
    val sheetState = rememberModalBottomSheetState()
    val timeFormatter = "HH:mm"
    val detailActionsScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    if (employee != null && slot != null) {
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {

            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val role = Role.of(employee!!.guid)
                if (role != Role.NONE) {
                    AnimatedGradientText(
                        employee!!.name,
                        colors = role.colors(),
                        textStyle = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Text(
                        employee!!.name, style = MaterialTheme.typography.titleLarge
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (role != Role.NONE) {
                        Text(
                            text = stringResource(role.resourceString()!!) + " | ",
                            style = MaterialTheme.typography.bodyMediumEmphasized
                        )
                    }
                    Text(
                        text = employee!!.identifier ?: "?????",
                        style = MaterialTheme.typography.bodyMediumEmphasized
                    )
                }
                if (role != Role.NONE) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(role.infoResourceString()!!),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(4.dp))
                if (DebugFlags.SHOW_DEBUG_INFO.active()) {
                    CardColumn {
                        CardListItem(
                            headlineContent = { Text(source) },
                            supportingContent = { Text("Data source") },
                            leadingContent = {
                                Icon(
                                    BugReport, contentDescription = null
                                )
                            })
                        CardListItem(
                            modifier = Modifier.clickable(onClick = {
                                scope.launch {
                                    getPlatformClipboardApi().copyToClipboard(
                                        label = "employeeGuid", text = slot.employeeGuid ?: ""
                                    )
                                }
                            }),
                            headlineContent = { Text(slot.employeeGuid ?: "<null>") },
                            supportingContent = { Text("Employee GUID") },
                            leadingContent = {
                                Icon(
                                    BugReport, contentDescription = null
                                )
                            })
                        CardListItem(modifier = Modifier.clickable(onClick = {
                            scope.launch {
                                getPlatformClipboardApi().copyToClipboard(
                                    label = "requirementGuid", text = slot.requirement.guid
                                )
                            }
                        }), headlineContent = {
                            Text(
                                slot.requirement.guid.nullIfBlank() ?: "<null>"
                            )
                        }, supportingContent = { Text("Requirement GUID") }, leadingContent = {
                            Icon(
                                BugReport, contentDescription = null
                            )
                        })
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (slot.info.isNotNullOrBlank()) {
                            ListItem(
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ), leadingContent = {
                                    Icon(
                                        Info, contentDescription = null
                                    )
                                }, headlineContent = {
                                    Text(slot.info)
                                }, supportingContent = {
                                    Text(stringResource(Res.string.main_schedule_infobox_info))
                                }, colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                        ListItem(
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(4.dp)
                            ), leadingContent = {
                                Icon(
                                    slot.requirement.getIcon(),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }, headlineContent = {
                                Text(
                                    stringResource(slot.requirement.resourceString())
                                )
                            }, supportingContent = {
                                Text(stringResource(Res.string.main_schedule_infobox_assigned_role))
                            }, colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                        ListItem(
                            modifier = Modifier.background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(4.dp)
                            ), leadingContent = {
                                Icon(
                                    Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }, headlineContent = {
                                Text(
                                    "${
                                        slot.begin.format(timeFormatter)
                                    } - ${
                                        slot.end.format(timeFormatter)
                                    }"
                                )
                            }, supportingContent = {
                                Text(
                                    slot.begin.format("d MMMM yyyy")
                                )
                            }, colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }

                val nowMillis = Clock.System.now().toEpochMilliseconds()
                val messages = DutyScheduleService.getMessages()[employee!!.guid] ?: emptyList()
                val filteredMessages = messages.filter {
                    (it.displayTo == null || it.displayTo.toEpochMilliseconds() >= nowMillis) && (it.displayFrom == null || it.displayFrom.toEpochMilliseconds() <= nowMillis)
                }
                if (filteredMessages.isNotEmpty()) {
                    Spacer(Modifier)
                    Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (message in messages) {

                                ListItem(
                                    modifier = Modifier.background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = RoundedCornerShape(4.dp)
                                    ), leadingContent = {
                                        Icon(
                                            Notes, contentDescription = null
                                        )
                                    }, headlineContent = {
                                        Text(message.message)
                                    }, supportingContent = {
                                        Text(message.title)
                                    }, colors = ListItemDefaults.colors(
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
                        if (employee!!.defaultOrg.isNotNullOrBlank()) {
                            ListItem(
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ), leadingContent = {
                                    Icon(Domain, contentDescription = null)
                                }, headlineContent = {
                                    var primaryOrg =
                                        orgs.firstOrNull { it.abbreviation == employee!!.defaultOrg }
                                    if (primaryOrg == null) {
                                        primaryOrg =
                                            orgs.firstOrNull { it.identifier == employee!!.defaultOrg }
                                    }

                                    Text(primaryOrg?.title ?: (employee!!.defaultOrg ?: ""))
                                }, supportingContent = {
                                    Text(stringResource(Res.string.main_schedule_infobox_primary))
                                }, colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                        if (employee!!.skills.isNotEmpty()) {

                            val skills = employee!!.skills
                            val filteredSkills =
                                skills.filter { MappedSkills.parse(it.guid) != MappedSkills.INVALID }
                                    .toMutableList()

                            if (skills.contains(MappedSkills.RS.asSkill()) && skills.contains(
                                    MappedSkills.AZUBI.asSkill()
                                )
                            ) {
                                filteredSkills.remove(MappedSkills.AZUBI.asSkill())
                            }
                            if (skills.contains(MappedSkills.NFS.asSkill()) && skills.contains(
                                    MappedSkills.RS.asSkill()
                                )
                            ) {
                                filteredSkills.remove(MappedSkills.RS.asSkill())
                            }
                            val skillTxt =
                                filteredSkills.map { stringResource(it.resourceString()) }
                                    .joinToString(", ")

                            ListItem(
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ), leadingContent = {
                                    Icon(PersonPlay, contentDescription = null)
                                }, headlineContent = {
                                    Text(skillTxt)
                                }, supportingContent = {
                                    Text(stringResource(Res.string.main_schedule_infobox_skill))
                                }, colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                        if (employee!!.birthdate != null) {
                            ListItem(
                                modifier = Modifier.background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ), leadingContent = {
                                    Icon(Cake, contentDescription = null)
                                }, headlineContent = {
                                    Text(
                                        employee!!.birthdate!!.format("d MMMM yyyy")
                                    )
                                }, supportingContent = {
                                    Text(stringResource(Res.string.main_schedule_infobox_birthday))
                                }, colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
                if (slot.requirement.guid != RequirementMapping.SEW.value) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .horizontalScroll(detailActionsScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                getPlatformRedirectApi().dialPhone(employee?.phone ?: "")
                            }, leadingIcon = {
                                Icon(
                                    Call,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        AssistChipDefaults.IconSize
                                    )
                                )
                            }, label = {
                                Text(stringResource(Res.string.main_schedule_infobox_actions_call))
                            }, enabled = employee!!.phone.isNotNullOrBlank()
                        )
                        AssistChip(
                            onClick = {
                                getPlatformRedirectApi().sendSms(employee?.phone ?: "")
                            }, leadingIcon = {
                                Icon(
                                    Sms,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        AssistChipDefaults.IconSize
                                    )
                                )
                            }, label = {
                                Text(stringResource(Res.string.main_schedule_infobox_actions_sms))
                            }, enabled = employee!!.phone.isNotNullOrBlank()
                        )
                        AssistChip(
                            onClick = {
                                getPlatformRedirectApi().sendWA(employee?.phone ?: "")
                            }, leadingIcon = {
                                Icon(
                                    Sms, // TODO: Add proper WA icon
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        AssistChipDefaults.IconSize
                                    )
                                )
                            }, label = {
                                Text(stringResource(Res.string.main_schedule_infobox_actions_whatsapp))
                            }, enabled = employee!!.phone.isNotNullOrBlank()
                        )
                        AssistChip(
                            onClick = {
                                getPlatformRedirectApi().sendEmail(employee?.email ?: "")
                            }, leadingIcon = {
                                Icon(
                                    AlternateEmail,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        AssistChipDefaults.IconSize
                                    )
                                )
                            }, label = {
                                Text(stringResource(Res.string.main_schedule_infobox_actions_mail))
                            }, enabled = employee!!.email.isNotNullOrBlank()
                        )
                    }
                }
            }
        }
    }
}
