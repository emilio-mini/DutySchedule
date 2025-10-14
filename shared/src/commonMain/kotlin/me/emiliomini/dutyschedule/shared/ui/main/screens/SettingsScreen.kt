package me.emiliomini.dutyschedule.shared.ui.main.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_settings_account_logout_content
import dutyschedule.shared.generated.resources.main_settings_account_logout_title
import dutyschedule.shared.generated.resources.main_settings_account_user_content_fallback
import dutyschedule.shared.generated.resources.main_settings_account_user_title_fallback
import dutyschedule.shared.generated.resources.main_settings_section_account
import dutyschedule.shared.generated.resources.main_settings_snackbar_incode
import dutyschedule.shared.generated.resources.main_settings_title
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.ui.components.CardColumn
import me.emiliomini.dutyschedule.shared.ui.components.CardListItem
import me.emiliomini.dutyschedule.shared.ui.components.CardListItemType
import me.emiliomini.dutyschedule.shared.ui.components.EmployeeAvatar
import me.emiliomini.dutyschedule.shared.ui.icons.Logout
import me.emiliomini.dutyschedule.shared.ui.icons.Person
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier, bottomBar: @Composable (() -> Unit) = {}, onLogout: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarIncodeText = stringResource(Res.string.main_settings_snackbar_incode)

    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            title = {
                Text(stringResource(Res.string.main_settings_title))
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
                stringResource(Res.string.main_settings_section_account),
                color = MaterialTheme.colorScheme.primary
            )
            CardColumn {
                CardListItem(
                    headlineContent = {
                        Text(
                            DutyScheduleService.self?.name
                                ?: stringResource(Res.string.main_settings_account_user_title_fallback)
                        )
                    }, supportingContent = {
                        if (DutyScheduleService.self != null) {
                            Text(DutyScheduleService.self?.identifier ?: "")
                        } else {
                            Text(stringResource(Res.string.main_settings_account_user_content_fallback))
                        }
                    }, leadingContent = {
                        if (DutyScheduleService.self != null) {
                            EmployeeAvatar(
                                employee = DutyScheduleService.self!!,
                                onLogout = onLogout
                            )
                        } else {
                            Icon(
                                Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }, type = CardListItemType.TOP)
                CardListItem(
                    modifier = Modifier
                        .clickable(onClick = onLogout),
                    headlineContent = {
                        Text(stringResource(Res.string.main_settings_account_logout_title))
                    },
                    supportingContent = {
                        Text(stringResource(Res.string.main_settings_account_logout_content))
                    },
                    leadingContent = {
                        Icon(
                            Logout,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    type = CardListItemType.BOTTOM
                )
            }
        }
    })
}
