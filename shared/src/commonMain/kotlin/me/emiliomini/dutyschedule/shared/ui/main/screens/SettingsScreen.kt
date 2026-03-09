package me.emiliomini.dutyschedule.shared.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_settings_appearance_dynamic_color_content
import dutyschedule.shared.generated.resources.main_settings_appearance_dynamic_color_title
import dutyschedule.shared.generated.resources.main_settings_appearance_theme_dark
import dutyschedule.shared.generated.resources.main_settings_appearance_theme_light
import dutyschedule.shared.generated.resources.main_settings_appearance_theme_system
import dutyschedule.shared.generated.resources.main_settings_appearance_theme_title
import dutyschedule.shared.generated.resources.main_settings_background_updater_content
import dutyschedule.shared.generated.resources.main_settings_background_updater_title
import dutyschedule.shared.generated.resources.main_settings_notifications_permanent_content
import dutyschedule.shared.generated.resources.main_settings_notifications_permanent_title
import dutyschedule.shared.generated.resources.main_settings_section_alarms
import dutyschedule.shared.generated.resources.main_settings_section_appearance
import dutyschedule.shared.generated.resources.main_settings_section_background
import dutyschedule.shared.generated.resources.main_settings_section_notifications
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.api.getPlatformTaskSchedulerApi
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask
import me.emiliomini.dutyschedule.shared.services.NotificationService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.components.CardColumn
import me.emiliomini.dutyschedule.shared.ui.components.CardListItem
import me.emiliomini.dutyschedule.shared.ui.components.CardListItemType
import me.emiliomini.dutyschedule.shared.ui.icons.AlarmOff
import me.emiliomini.dutyschedule.shared.ui.icons.AlarmOn
import me.emiliomini.dutyschedule.shared.ui.main.components.DutyAlarmListItem
import me.emiliomini.dutyschedule.shared.ui.theme.isDynamicColorSupported
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    onThemeModeChange: (Int) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var permanentNotification by remember { mutableStateOf(true) }
    var backgroundUpdaterEnabled by remember { mutableStateOf(true) }
    var themeMode by remember { mutableIntStateOf(0) }
    var dynamicColor by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val prefs = StorageService.USER_PREFERENCES.getOrDefault()
        permanentNotification = prefs.permanentNotification
        backgroundUpdaterEnabled = prefs.backgroundUpdaterEnabled
        themeMode = prefs.themeMode
        dynamicColor = prefs.dynamicColor
    }

    Screen(modifier = modifier, paddingValues = paddingValues) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Notifications ─────────────────────────────────────────────────
            Text(
                stringResource(Res.string.main_settings_section_notifications),
                color = MaterialTheme.colorScheme.primary
            )
            CardColumn {
                CardListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.main_settings_notifications_permanent_title))
                    },
                    supportingContent = {
                        Text(stringResource(Res.string.main_settings_notifications_permanent_content))
                    },
                    trailingContent = {
                        Switch(
                            checked = permanentNotification,
                            onCheckedChange = { checked ->
                                permanentNotification = checked
                                scope.launch {
                                    StorageService.USER_PREFERENCES.update {
                                        it.copy(permanentNotification = checked)
                                    }
                                    if (checked) {
                                        NotificationService.sendInfoNotification()
                                    } else {
                                        NotificationService.cancelInfoNotification()
                                    }
                                }
                            }
                        )
                    },
                    type = CardListItemType.SINGLE
                )
            }

            // ── Background Updater ─────────────────────────────────────────────
            Text(
                stringResource(Res.string.main_settings_section_background),
                color = MaterialTheme.colorScheme.primary
            )
            CardColumn {
                CardListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.main_settings_background_updater_title))
                    },
                    supportingContent = {
                        Text(stringResource(Res.string.main_settings_background_updater_content))
                    },
                    trailingContent = {
                        Switch(
                            checked = backgroundUpdaterEnabled,
                            onCheckedChange = { checked ->
                                backgroundUpdaterEnabled = checked
                                scope.launch {
                                    StorageService.USER_PREFERENCES.update {
                                        it.copy(backgroundUpdaterEnabled = checked)
                                    }
                                    if (checked) {
                                        getPlatformTaskSchedulerApi().scheduleTask(MultiplatformTask.UpdateAlarms)
                                    } else {
                                        getPlatformTaskSchedulerApi().cancelTask(MultiplatformTask.UpdateAlarms)
                                    }
                                }
                            },
                            thumbContent = {
                                Icon(
                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                    imageVector = if (backgroundUpdaterEnabled) AlarmOn else AlarmOff,
                                    contentDescription = null
                                )
                            }
                        )
                    },
                    type = CardListItemType.SINGLE
                )
            }

            // ── Alarm Offset ───────────────────────────────────────────────────
            Text(
                stringResource(Res.string.main_settings_section_alarms),
                color = MaterialTheme.colorScheme.primary
            )
            CardColumn {
                DutyAlarmListItem()
            }

            // ── Appearance / Theme ─────────────────────────────────────────────
            Text(
                stringResource(Res.string.main_settings_section_appearance),
                color = MaterialTheme.colorScheme.primary
            )
            CardColumn {
                if (isDynamicColorSupported()) {
                    CardListItem(
                        headlineContent = {
                            Text(stringResource(Res.string.main_settings_appearance_dynamic_color_title))
                        },
                        supportingContent = {
                            Text(stringResource(Res.string.main_settings_appearance_dynamic_color_content))
                        },
                        trailingContent = {
                            Switch(
                                checked = dynamicColor,
                                onCheckedChange = { checked ->
                                    dynamicColor = checked
                                    onDynamicColorChange(checked)
                                    scope.launch {
                                        StorageService.USER_PREFERENCES.update {
                                            it.copy(dynamicColor = checked)
                                        }
                                    }
                                }
                            )
                        },
                        type = CardListItemType.TOP
                    )
                }
                CardListItem(
                    headlineContent = {
                        Text(stringResource(Res.string.main_settings_appearance_theme_title))
                    },
                    supportingContent = {
                        val themeOptions = listOf(
                            stringResource(Res.string.main_settings_appearance_theme_system),
                            stringResource(Res.string.main_settings_appearance_theme_light),
                            stringResource(Res.string.main_settings_appearance_theme_dark)
                        )
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            themeOptions.forEachIndexed { index, label ->
                                SegmentedButton(
                                    selected = themeMode == index,
                                    onClick = {
                                        themeMode = index
                                        onThemeModeChange(index)
                                        scope.launch {
                                            StorageService.USER_PREFERENCES.update {
                                                it.copy(themeMode = index)
                                            }
                                        }
                                    },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = themeOptions.size
                                    )
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    },
                    type = if (isDynamicColorSupported()) CardListItemType.BOTTOM else CardListItemType.SINGLE
                )
            }
        }
    }
}
