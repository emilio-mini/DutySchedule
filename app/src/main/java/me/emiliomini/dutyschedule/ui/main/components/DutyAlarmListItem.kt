package me.emiliomini.dutyschedule.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.services.storage.DataKeys
import me.emiliomini.dutyschedule.services.storage.StorageService
import me.emiliomini.dutyschedule.ui.components.CardListItem
import me.emiliomini.dutyschedule.ui.components.WheelSelector
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DutyAlarmListItem(modifier: Modifier = Modifier) {
    LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var selectedDurationMin by remember { mutableLongStateOf(90L) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val value = StorageService.load(DataKeys.ALARM_OFFSET)
        if (value == null) {
            return@LaunchedEffect
        }

        selectedDurationMin = value
    }

    CardListItem(
        modifier = modifier.clickable(
            onClick = {
                showDialog = true
            }
        ),
        headlineContent = {
            Text(stringResource(R.string.main_settings_alarms_pre_event_begin_title))
        },
        supportingContent = {
            Text(
                stringResource(
                    R.string.main_settings_alarms_pre_event_begin_content,
                    "${abs(selectedDurationMin / 60)}h ${selectedDurationMin % 60}min"
                )
            )
        },
        leadingContent = {
            Icon(
                Icons.Rounded.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )

    if (showDialog) {
        var hour by remember { mutableStateOf("${abs(selectedDurationMin / 60)}") }
        var minute by remember { mutableStateOf("${selectedDurationMin % 60}") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            icon = {
                Icon(
                    Icons.Rounded.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.main_settings_alarms_pre_event_begin_dialog_title))
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    WheelSelector(
                        modifier = Modifier.width(48.dp),
                        items = (0..23).map {
                            var str = it.toString()
                            while (str.length < 2) {
                                str = "0$str"
                            }
                            str
                        },
                        selectedColor = MaterialTheme.colorScheme.primary,
                        onValueChange = {
                            hour = it.toString()
                        },
                        selectedIndex = hour.toInt(),
                        textStyle = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = "tnum"),
                        alignment = Alignment.End,
                        monospaced = true,
                        monospacedLetterWidth = 24.dp
                    )
                    Text(
                        ":",
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    WheelSelector(
                        modifier = Modifier.width(48.dp),
                        items = (0..59).map {
                            var str = it.toString()
                            while (str.length < 2) {
                                str = "0$str"
                            }
                            str
                        },
                        selectedColor = MaterialTheme.colorScheme.primary,
                        onValueChange = {
                            minute = it.toString()
                        },
                        selectedIndex = minute.toInt(),
                        textStyle = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = "tnum"),
                        alignment = Alignment.Start,
                        monospaced = true,
                        monospacedLetterWidth = 24.dp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDurationMin = (hour.toInt() * 60 + minute.toInt()).toLong()
                        showDialog = false

                        scope.launch {
                            StorageService.save(
                                DataKeys.ALARM_OFFSET,
                                selectedDurationMin
                            )
                        }
                    }
                ) {
                    Text(stringResource(R.string.main_settings_alarms_pre_event_begin_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.main_settings_alarms_pre_event_begin_dialog_dismiss))
                }
            }
        )
    }
}