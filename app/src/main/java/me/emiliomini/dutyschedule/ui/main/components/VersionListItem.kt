package me.emiliomini.dutyschedule.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.BuildConfig
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.data.models.vc.GithubRelease
import me.emiliomini.dutyschedule.services.api.NetworkService
import me.emiliomini.dutyschedule.services.util.UtilService
import okhttp3.Headers

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun VersionListItem() {
    var showDetails by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var latestRelease by remember { mutableStateOf<GithubRelease?>(null) }
    var isCheckingLatest by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }
    var updateProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        latestRelease = NetworkService.getLatestVersion().getOrNull()
        isCheckingLatest = false
    }

    if (showDetails && latestRelease != null) {
        AlertDialog(
            onDismissRequest = {
                showDetails = false
            },
            title = {
                Text(text = latestRelease!!.tag)
            },
            text = {
                MarkdownText(markdown = latestRelease!!.description)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDetails = false
                        if (latestRelease!!.tag == BuildConfig.VERSION_NAME) {
                            return@TextButton
                        }

                        isUpdating = true
                        scope.launch {
                            val updateFile = NetworkService.downloadFileWithProgress(
                                context, latestRelease!!.downloadUrl,
                                Headers.Builder()
                                    .add("Authorization", "Bearer ${BuildConfig.GITHUB_API_TOKEN}")
                                    .add("Accept", "application/octet-stream")
                                    .build(), "DutySchedule.apk"
                            ) {
                                updateProgress = it.toFloat() / 100f
                            }

                            if (updateFile == null) {
                                isUpdating = false
                                updateProgress = 0f
                                return@launch
                            }

                            UtilService.installApk(context, updateFile)
                            isUpdating = false
                            updateProgress = 0f
                        }
                    }
                ) {
                    if (latestRelease!!.tag != BuildConfig.VERSION_NAME) {
                        Text(stringResource(R.string.main_settings_app_update_action_update))
                    } else {
                        Text(stringResource(R.string.main_settings_app_update_action_confirm))
                    }
                }
            }
        )
    }

    if (latestRelease != null && latestRelease!!.tag != BuildConfig.VERSION_NAME) {
        ListItem(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable(onClick = {
                    if (isUpdating) {
                        return@clickable
                    }

                    showDetails = true
                }), colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ), headlineContent = {
                if (isUpdating) {
                    Text(stringResource(R.string.main_settings_app_update_title_progress))
                } else {
                    Text(stringResource(R.string.main_settings_app_update_title))
                }
            }, supportingContent = {
                if (isUpdating) {
                    LinearWavyProgressIndicator(
                        progress = {
                            updateProgress
                        }
                    )
                } else {
                    Text(latestRelease!!.tag)
                }
            }, leadingContent = {
                Icon(
                    Icons.Rounded.Update,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }, trailingContent = {
                if (latestRelease == null) {
                    LoadingIndicator()
                }
            })
    } else {
        ListItem(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(4.dp)
                )
                .combinedClickable(onClick = {
                    if (latestRelease != null) {
                        showDetails = true
                    }
                }, onLongClick = {
                    scope.launch {
                        isCheckingLatest = true
                        latestRelease = NetworkService.getLatestVersion(true).getOrNull()
                        isCheckingLatest = false
                    }
                }), colors = ListItemDefaults.colors(
                containerColor = Color.Transparent
            ), headlineContent = {
                if (isCheckingLatest) {
                    Text(stringResource(R.string.main_settings_app_version_title_checking))
                } else {
                    Text(stringResource(R.string.main_settings_app_version_title))
                }
            }, supportingContent = {
                Text(BuildConfig.VERSION_NAME)
            }, leadingContent = {
                if (isCheckingLatest) {
                    LoadingIndicator(modifier = Modifier.scale(1.4f).width(24.dp))
                } else {
                    Icon(
                        Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            })
    }
}