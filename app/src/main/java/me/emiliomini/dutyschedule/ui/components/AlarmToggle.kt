package me.emiliomini.dutyschedule.ui.components

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlarmAdd
import androidx.compose.material.icons.outlined.AlarmOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.services.alarm.AlarmService
import me.emiliomini.dutyschedule.services.storage.DataKeys
import me.emiliomini.dutyschedule.services.storage.StorageService
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

@Composable
fun AlarmToggle(modifier: Modifier = Modifier, dutyBegin: OffsetDateTime, guid: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentMillis = OffsetDateTime.now().toInstant().toEpochMilli()
    val dutyBeginMillis = dutyBegin.toInstant().toEpochMilli()

    var alarmBlocked by remember { mutableStateOf(false) }
    var alarmSet by remember {
        mutableStateOf(
            AlarmService.isAlarmSet(
                context.applicationContext,
                guid.hashCode()
            )
        )
    }

    if (dutyBeginMillis >= currentMillis) {
        IconButton(
            modifier = modifier,
            onClick = {
                alarmBlocked = true
                if (alarmSet) {
                    scope.launch {
                        AlarmService.deleteAlarm(
                            context.applicationContext,
                            guid.hashCode()
                        )
                        alarmSet = false
                        alarmBlocked = false
                    }
                } else {
                    scope.launch {
                        val alarmOffset = StorageService.load(DataKeys.ALARM_OFFSET)
                        var alarmOffsetMillis = 0L
                        if (alarmOffset != null) {
                            try {
                                alarmOffsetMillis = TimeUnit.MINUTES.toMillis(alarmOffset)
                            } catch (_: NumberFormatException) {
                            }
                        }

                        val timestamp = dutyBeginMillis - alarmOffsetMillis
                        AlarmService.scheduleAlarm(
                            context.applicationContext,
                            timestamp,
                            guid.hashCode()
                        )
                        alarmBlocked = false
                        alarmSet = true
                    }
                }
            }, enabled = !alarmBlocked
        ) {
            if (alarmSet) {
                Icon(Icons.Outlined.AlarmOn, contentDescription = "Reminder set")
            } else {
                Icon(Icons.Outlined.AlarmAdd, contentDescription = "Set Reminder")
            }
        }
    }
}