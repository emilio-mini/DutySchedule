@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.api.getPlatformAlarmApi
import me.emiliomini.dutyschedule.shared.services.AlarmService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.icons.AlarmAdd
import me.emiliomini.dutyschedule.shared.ui.icons.AlarmOn
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Composable
fun AlarmToggle(modifier: Modifier = Modifier, dutyBegin: Instant, guid: String, snackbarHostState: SnackbarHostState?) {
    val scope = rememberCoroutineScope()
    val currentMillis = Clock.System.now().toEpochMilliseconds()
    val dutyBeginMillis = dutyBegin.toEpochMilliseconds()

    var alarmBlocked by remember { mutableStateOf(false) }
    var alarmSet by remember {
        mutableStateOf(
            getPlatformAlarmApi().isAlarmSet(guid)
        )
    }

    if (dutyBeginMillis >= currentMillis && snackbarHostState != null) {
        IconButton(
            modifier = modifier, onClick = {
                alarmBlocked = true
                if (alarmSet) {
                    scope.launch {
                        getPlatformAlarmApi().cancelAlarm(guid)

                        alarmSet = false
                        alarmBlocked = false
                    }
                } else {
                    scope.launch {
                        val alarmOffset =
                            StorageService.USER_PREFERENCES.getOrDefault().alarmOffsetMin
                        val alarmOffsetMillis = alarmOffset * 60_000L

                        val timestamp = dutyBeginMillis - alarmOffsetMillis
                        AlarmService.setAlarm(
                            guid,
                            Instant.fromEpochMilliseconds(timestamp),
                            onError = {
                                snackbarHostState.showSnackbar(it)
                            },
                            edited = true
                        )
                        alarmBlocked = false
                        alarmSet = true
                    }
                }
            }, enabled = !alarmBlocked
        ) {
            if (alarmSet) {
                Icon(AlarmOn, contentDescription = "Reminder set")
            } else {
                Icon(AlarmAdd, contentDescription = "Set Reminder")
            }
        }
    }
}