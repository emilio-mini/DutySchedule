@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_alarms_none
import dutyschedule.shared.generated.resources.main_alarms_title
import dutyschedule.shared.generated.resources.main_alarms_upcoming
import dutyschedule.shared.generated.resources.main_settings_section_alarms
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.emiliomini.dutyschedule.shared.api.getPlatformAlarmApi
import me.emiliomini.dutyschedule.shared.datastores.Alarm
import me.emiliomini.dutyschedule.shared.datastores.AlarmItems
import me.emiliomini.dutyschedule.shared.services.storage.ListViewModel
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.components.CardColumn
import me.emiliomini.dutyschedule.shared.ui.icons.AlarmOff
import me.emiliomini.dutyschedule.shared.ui.icons.AlarmOn
import me.emiliomini.dutyschedule.shared.ui.icons.NightsStay
import me.emiliomini.dutyschedule.shared.ui.icons.Sunny
import me.emiliomini.dutyschedule.shared.ui.main.components.DutyAlarmListItem
import me.emiliomini.dutyschedule.shared.util.format
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmsScreen(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {},
    viewModel: ListViewModel<AlarmItems, Alarm> = viewModel { ListViewModel(StorageService.ALARM_ITEMS) { it.alarms } }
) {
    LaunchedEffect(Unit) {
        // AlarmService.clean()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val timeFormat = "HH:mm"
    val dateFormat = "dd/MM/yyyy"
    val alarms by viewModel.flow.collectAsStateWithLifecycle(
        initialValue = emptyList()
    )

    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            title = {
                Text(stringResource(Res.string.main_alarms_title))
            },
        )
    }, bottomBar = bottomBar, content = { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                stringResource(Res.string.main_settings_section_alarms),
                color = MaterialTheme.colorScheme.primary
            )
            CardColumn {
                DutyAlarmListItem()
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (alarms.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(AlarmOff, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(Res.string.main_alarms_none),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        stringResource(Res.string.main_alarms_upcoming),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            itemsIndexed(
                                items = alarms, key = { _, alarm -> alarm.code }) { index, alarm ->
                                var active by remember {
                                    mutableStateOf(
                                        getPlatformAlarmApi().isAlarmSet(alarm.code)
                                    )
                                }
                                var blocked by remember { mutableStateOf(false) }
                                val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
                                val date = Instant.fromEpochMilliseconds(alarm.timestamp)

                                SwipeToDismissBox(
                                    state = swipeToDismissBoxState,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    color = MaterialTheme.colorScheme.errorContainer,
                                                    shape = RoundedCornerShape(
                                                        topStart = if (index == 0) 12.dp else 4.dp,
                                                        topEnd = if (index == 0) 12.dp else 4.dp,
                                                        bottomStart = if (index == alarms.lastIndex) 12.dp else 4.dp,
                                                        bottomEnd = if (index == alarms.lastIndex) 12.dp else 4.dp
                                                    )
                                                )
                                        )
                                    },
                                    onDismiss = {
                                        scope.launch {
                                            blocked = true
                                            getPlatformAlarmApi().cancelAlarm(alarm.code)
                                            blocked = false
                                        }
                                    }) {
                                    ListItem(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                shape = RoundedCornerShape(
                                                    topStart = if (index == 0) 12.dp else 4.dp,
                                                    topEnd = if (index == 0) 12.dp else 4.dp,
                                                    bottomStart = if (index == alarms.lastIndex) 12.dp else 4.dp,
                                                    bottomEnd = if (index == alarms.lastIndex) 12.dp else 4.dp
                                                )
                                            )
                                            .clickable(onClick = {
                                                if (blocked) {
                                                    return@clickable
                                                }

                                                blocked = true
                                                active = !active

                                                scope.launch {
                                                    setAlarm(alarm, active)
                                                    blocked = false
                                                }
                                            }), colors = ListItemDefaults.colors(
                                            containerColor = Color.Transparent
                                        ), headlineContent = {
                                            Text(date.format(timeFormat))
                                        }, supportingContent = {
                                            Text(date.format(dateFormat))
                                        }, leadingContent = {
                                            Icon(
                                                imageVector = if (date.toLocalDateTime(TimeZone.currentSystemDefault()).hour >= 15) {
                                                    NightsStay
                                                } else {
                                                    Sunny
                                                },
                                                contentDescription = null
                                            )

                                        }, trailingContent = {
                                            Switch(checked = active, thumbContent = {
                                                Icon(
                                                    modifier = Modifier.size(SwitchDefaults.IconSize),
                                                    imageVector = if (active) AlarmOn else AlarmOff,
                                                    contentDescription = null
                                                )
                                            }, onCheckedChange = {
                                                if (blocked) {
                                                    return@Switch
                                                }

                                                blocked = true
                                                active = !active

                                                scope.launch {
                                                    setAlarm(alarm, active)
                                                    blocked = false
                                                }
                                            })
                                        })
                                }
                            }
                        }
                    }
                }
            }
        }
    })
}

private suspend fun setAlarm(alarm: Alarm, enabled: Boolean) {
    if (enabled) {
        getPlatformAlarmApi().setAlarm(alarm.code, Instant.fromEpochMilliseconds(alarm.timestamp))
    } else {
        getPlatformAlarmApi().cancelAlarm(alarm.code)
    }
}
