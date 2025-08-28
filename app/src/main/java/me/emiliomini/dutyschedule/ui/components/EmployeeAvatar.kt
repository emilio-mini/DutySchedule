package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Phone
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.services.network.PrepService
import java.time.format.DateTimeFormatter

@Composable
fun EmployeeAvatar(
    modifier: Modifier = Modifier,
    employee: Employee,
    canExpand: Boolean = true,
    onLogout: () -> Unit
) {
    val employeeNames = employee.name.split(" ")
    val initial = if (employeeNames.size >= 2) employeeNames[1][0] else employeeNames[0][0]

    val isSelf = employee.guid == PrepService.getSelf()?.guid

    var expanded by remember { mutableStateOf(false) }
    var employeeOrg by remember { mutableStateOf<OrgProto?>(null) }

    LaunchedEffect(employee) {
        employeeOrg = PrepService.getOrg(employee.defaultOrg)
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
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isSelf) {
                            Text(stringResource(R.string.base_avatar_dialog_title))
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
                                }
                            )
                        }
                        CardColumn {
                            CardListItem(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                leadingContent = {
                                    Icon(Icons.Rounded.Phone, contentDescription = null)
                                },
                                headlineContent = {
                                    Text(employee.phone)
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.base_avatar_dialog_phone))
                                }
                            )
                            CardListItem(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                leadingContent = {
                                    Icon(Icons.Rounded.AlternateEmail, contentDescription = null)
                                },
                                headlineContent = {
                                    Text(employee.email)
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.base_avatar_dialog_mail))
                                }
                            )
                            if (employee.birthdate != null) {
                                CardListItem(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    leadingContent = {
                                        Icon(Icons.Rounded.Cake, contentDescription = null)
                                    },
                                    headlineContent = {
                                        Text(
                                            employee.birthdate!!.format(
                                                DateTimeFormatter.ofPattern("d MMMM yyyy")
                                            )
                                        )
                                    },
                                    supportingContent = {
                                        Text(stringResource(R.string.base_avatar_dialog_birthdate))
                                    }
                                )
                            }
                            CardListItem(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                leadingContent = {
                                    Icon(Icons.Rounded.Business, contentDescription = null)
                                },
                                headlineContent = {
                                    Text(
                                        if (employeeOrg != null) employeeOrg!!.title else employee.defaultOrg
                                    )
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.base_avatar_dialog_primary))
                                }
                            )
                        }
                        if (isSelf) {
                            CardColumn {
                                CardListItem(
                                    modifier = Modifier.clickable(true, onClick = {
                                        onLogout()
                                    }),
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    leadingContent = {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.Logout,
                                            contentDescription = null
                                        )
                                    },
                                    headlineContent = {
                                        Text(stringResource(R.string.base_avatar_dialog_action_logout))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}