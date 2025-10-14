package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.base_avatar_dialog_action_bugreport
import dutyschedule.shared.generated.resources.base_avatar_dialog_action_logout
import dutyschedule.shared.generated.resources.base_avatar_dialog_birthdate
import dutyschedule.shared.generated.resources.base_avatar_dialog_mail
import dutyschedule.shared.generated.resources.base_avatar_dialog_phone
import dutyschedule.shared.generated.resources.base_avatar_dialog_primary
import dutyschedule.shared.generated.resources.base_avatar_dialog_title
import dutyschedule.shared.generated.resources.base_avatar_dialog_version
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.api.getPlatformClipboardApi
import me.emiliomini.dutyschedule.shared.api.getPlatformRedirectApi
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.Org
import me.emiliomini.dutyschedule.shared.platform
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.ui.icons.AlternateEmail
import me.emiliomini.dutyschedule.shared.ui.icons.BugReport
import me.emiliomini.dutyschedule.shared.ui.icons.Cake
import me.emiliomini.dutyschedule.shared.ui.icons.Call
import me.emiliomini.dutyschedule.shared.ui.icons.Close
import me.emiliomini.dutyschedule.shared.ui.icons.Domain
import me.emiliomini.dutyschedule.shared.ui.icons.Info
import me.emiliomini.dutyschedule.shared.ui.icons.Logout
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.versionCode
import me.emiliomini.dutyschedule.shared.versionName
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmployeeAvatar(
    modifier: Modifier = Modifier,
    employee: Employee,
    canExpand: Boolean = true,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    val employeeNames = employee.name.split(" ")
    val initial =
        if (employeeNames.size >= 2) employeeNames[1][0] else if (employeeNames.isNotEmpty() && employeeNames[0].isNotBlank()) employeeNames[0][0] else ""

    val isSelf = employee.guid == DutyScheduleService.self?.guid

    var expanded by remember { mutableStateOf(false) }
    var employeeOrg by remember { mutableStateOf<Org?>(null) }

    LaunchedEffect(employee) {
        employeeOrg = DutyScheduleService.getOrg(employee.defaultOrg ?: "")
    }

    InitialsAvatar(
        modifier = modifier,
        initials = "$initial",
        enabled = canExpand,
        onClick = {
            expanded = !expanded
        }
    )

    if (expanded) {
        Dialog(
            onDismissRequest = {
                expanded = false
            }
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box {
                    IconButton(
                        onClick = {
                            expanded = false
                        }
                    ) {
                        Icon(Close, contentDescription = null)
                    }
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSelf) {
                            Text(stringResource(Res.string.base_avatar_dialog_title))
                        }
                        CardColumn {
                            CardListItem(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                leadingContent = {
                                    InitialsAvatar(initials = "$initial")
                                },
                                headlineContent = {
                                    Text(employee.name)
                                },
                                supportingContent = {
                                    Text(employee.identifier ?: "")
                                },
                                type = CardListItemType.SINGLE
                            )
                        }
                        CardColumn {
                            CardListItem(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                leadingContent = {
                                    Icon(Call, contentDescription = null)
                                },
                                headlineContent = {
                                    Text(employee.phone ?: "")
                                },
                                supportingContent = {
                                    Text(stringResource(Res.string.base_avatar_dialog_phone))
                                },
                                type = CardListItemType.TOP
                            )
                            CardListItem(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                leadingContent = {
                                    Icon(AlternateEmail, contentDescription = null)
                                },
                                headlineContent = {
                                    Text(employee.email ?: "")
                                },
                                supportingContent = {
                                    Text(stringResource(Res.string.base_avatar_dialog_mail))
                                }
                            )
                            if (employee.birthdate != null) {
                                CardListItem(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    leadingContent = {
                                        Icon(Cake, contentDescription = null)
                                    },
                                    headlineContent = {
                                        Text(
                                            employee.birthdate.format("d MMMM yyyy")
                                        )
                                    },
                                    supportingContent = {
                                        Text(stringResource(Res.string.base_avatar_dialog_birthdate))
                                    }
                                )
                            }
                            CardListItem(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                leadingContent = {
                                    Icon(Domain, contentDescription = null)
                                },
                                headlineContent = {
                                    Text(
                                        if (employeeOrg != null) employeeOrg!!.title else employee.defaultOrg
                                            ?: ""
                                    )
                                },
                                supportingContent = {
                                    Text(stringResource(Res.string.base_avatar_dialog_primary))
                                },
                                type = CardListItemType.BOTTOM
                            )
                        }
                        if (isSelf) {
                            CardColumn {
                                val versionCode = versionCode()
                                val versionName = versionName()
                                val platform = platform()
                                CardListItem(
                                    modifier = Modifier.clickable(true, onClick = {
                                        scope.launch {
                                            getPlatformClipboardApi().copyToClipboard(
                                                "$versionName-$versionCode-$platform",
                                                "app-version"
                                            )
                                        }
                                    }),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    leadingContent = {
                                        Icon(
                                            Info,
                                            contentDescription = null
                                        )
                                    },
                                    headlineContent = {
                                        Text("$versionName (build-$versionCode)")
                                    },
                                    supportingContent = {
                                        Text(stringResource(Res.string.base_avatar_dialog_version))
                                    },
                                    type = CardListItemType.TOP
                                )
                                CardListItem(
                                    modifier = Modifier.clickable(true, onClick = {
                                        scope.launch {
                                            getPlatformClipboardApi().copyToClipboard(
                                                "$versionName-$versionCode-$platform",
                                                "app-version"
                                            )
                                            getPlatformRedirectApi().sendEmail("support@emilio-mini.me")
                                        }
                                    }),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    leadingContent = {
                                        Icon(
                                            BugReport,
                                            contentDescription = null
                                        )
                                    },
                                    headlineContent = {
                                        Text(stringResource(Res.string.base_avatar_dialog_action_bugreport))
                                    },
                                    type = CardListItemType.BOTTOM
                                )
                            }
                            CardColumn {
                                CardListItem(
                                    modifier = Modifier.clickable(true, onClick = {
                                        onLogout()
                                    }),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    leadingContent = {
                                        Icon(
                                            Logout,
                                            contentDescription = null
                                        )
                                    },
                                    headlineContent = {
                                        Text(stringResource(Res.string.base_avatar_dialog_action_logout))
                                    },
                                    type = CardListItemType.SINGLE
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}