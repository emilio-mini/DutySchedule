package me.emiliomini.dutyschedule.ui.main.screens

import android.content.Context
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.AlarmProto
import me.emiliomini.dutyschedule.services.alarm.AlarmService
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.services.storage.viewmodels.AlarmListViewModel
import me.emiliomini.dutyschedule.services.storage.viewmodels.AlarmListViewModelFactory
import me.emiliomini.dutyschedule.util.TimeUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmsScreen(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {},
    viewModel: AlarmListViewModel = viewModel(
        factory = AlarmListViewModelFactory(DataStores.ALARM_ITEMS)
    )
) {
    LaunchedEffect(Unit) {
        AlarmService.clean()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val alarms by viewModel.alarmsFlow.collectAsStateWithLifecycle(
        initialValue = emptyList<AlarmProto>()
    )

    Scaffold(modifier = modifier, snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            title = {
                Text(stringResource(R.string.main_alarms_title))
            },
        )
    }, bottomBar = bottomBar, content = { innerPadding ->
        if (alarms.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.AlarmOff, contentDescription = null)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.main_alarms_none),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    stringResource(R.string.main_alarms_upcoming),
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
                                    AlarmService.isAlarmSet(
                                        context.applicationContext, alarm.code
                                    )
                                )
                            }
                            var blocked by remember { mutableStateOf(false) }
                            val swipeToDismissBoxState = rememberSwipeToDismissBoxState()
                            val date = Date(alarm.timestamp)

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
                                        AlarmService.deleteAlarm(
                                            context.applicationContext, alarm.code
                                        )
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
                                                setAlarm(context, alarm, active)
                                                blocked = false
                                            }
                                        }), colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    ), headlineContent = {
                                        Text(timeFormat.format(date))
                                    }, supportingContent = {
                                        Text(dateFormat.format(date))
                                    }, leadingContent = {
                                        Icon(
                                            imageVector = if (TimeUtil.isAfterOrEqualTime(
                                                    date.toInstant(),
                                                    15
                                                )
                                            ) {
                                                Icons.Rounded.NightsStay
                                            } else {
                                                Icons.Rounded.WbSunny
                                            },
                                            contentDescription = null
                                        )

                                    }, trailingContent = {
                                        Switch(checked = active, thumbContent = {
                                            Icon(
                                                modifier = Modifier.size(SwitchDefaults.IconSize),
                                                imageVector = if (active) Icons.Rounded.AlarmOn else Icons.Rounded.AlarmOff,
                                                contentDescription = null
                                            )
                                        }, onCheckedChange = {
                                            if (blocked) {
                                                return@Switch
                                            }

                                            blocked = true
                                            active = !active

                                            scope.launch {
                                                setAlarm(context, alarm, active)
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
    })
}

private suspend fun setAlarm(context: Context, alarm: AlarmProto, enabled: Boolean) {
    if (enabled) {
        AlarmService.scheduleAlarm(context.applicationContext, alarm.timestamp, alarm.code)
    } else {
        AlarmService.cancelAlarm(context.applicationContext, alarm.code)
    }
}
