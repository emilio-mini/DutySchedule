package me.emiliomini.dutyschedule.ui.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.services.api.PrepService

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
            Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ListItem(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .combinedClickable(onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        snackbarIncodeText
                                    )
                                }
                            }, onLongClick = {
                                val incode = PrepService.getIncode()
                                if (incode == null) {
                                    return@combinedClickable
                                }
                                val clipboardManager =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData = ClipData.newPlainText(
                                    "Incode", "${incode.token}:${incode.value}"
                                )
                                clipboardManager.setPrimaryClip(clipData)
                            }), colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ), headlineContent = {
                            Text(
                                PrepService.getSelf()?.name
                                    ?: stringResource(R.string.main_settings_account_user_title_fallback)
                            )
                        }, supportingContent = {
                            val incode = PrepService.getIncode()
                            if (incode != null) {
                                Text(incode.token)
                            } else {
                                Text(stringResource(R.string.main_settings_account_user_content_fallback))
                            }
                        }, leadingContent = {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        })
                    ListItem(
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable(onClick = onLogout),
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        ),
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.main_settings_section_app),
                color = MaterialTheme.colorScheme.primary
            )
            Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    VersionListItem()
                }
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