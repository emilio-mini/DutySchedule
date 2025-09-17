package me.emiliomini.dutyschedule.ui.main.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.ui.components.CardColumn
import me.emiliomini.dutyschedule.ui.components.CardListItem
import me.emiliomini.dutyschedule.ui.components.EmployeeAvatar
import me.emiliomini.dutyschedule.ui.main.components.DutyAlarmListItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier, bottomBar: @Composable (() -> Unit) = {}, onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val snackbarIncodeText = stringResource(R.string.main_settings_snackbar_incode)

    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            title = {
                Text(stringResource(R.string.main_settings_title))
            },
        )
    }, bottomBar = bottomBar, content = { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(R.string.main_settings_section_account),
                color = MaterialTheme.colorScheme.primary
            )
            CardColumn {
                CardListItem(
                    headlineContent = {
                        Text(
                            DutyScheduleService.self?.name
                                ?: stringResource(R.string.main_settings_account_user_title_fallback)
                        )
                    }, supportingContent = {
                        if (DutyScheduleService.self != null) {
                            Text(DutyScheduleService.self!!.identifier)
                        } else {
                            Text(stringResource(R.string.main_settings_account_user_content_fallback))
                        }
                    }, leadingContent = {
                        if (DutyScheduleService.self != null) {
                            EmployeeAvatar(
                                employee = DutyScheduleService.self!!,
                                onLogout = onLogout
                            )
                        } else {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    })
                CardListItem(
                    modifier = Modifier
                        .clickable(onClick = onLogout),
                    headlineContent = {
                        Text(stringResource(R.string.main_settings_account_logout_title))
                    },
                    supportingContent = {
                        Text(stringResource(R.string.main_settings_account_logout_content))
                    },
                    leadingContent = {
                        Icon(
                            Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                )
            }
        }
    })
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun SettingsScreenPreview() {
    SettingsScreen(
        modifier = Modifier
    ) {}
}