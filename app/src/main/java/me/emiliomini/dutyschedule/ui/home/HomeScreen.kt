package me.emiliomini.dutyschedule.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.data.models.TimelineItem
import me.emiliomini.dutyschedule.data.models.mapping.OrgUnitDataGuid
import me.emiliomini.dutyschedule.services.api.PrepService
import me.emiliomini.dutyschedule.ui.base.AppDateInfo
import me.emiliomini.dutyschedule.ui.base.AppDutyCard
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview()
fun HomeScreen(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {},
) {
    val defaultDateSpacing = 5 * 24 * 60 * 60 * 1000L
    val currentMillis = OffsetDateTime.now()
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)
        .toInstant()
        .toEpochMilli()

    val dateRangePickerState = rememberDateRangePickerState()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedStartDate by remember { mutableStateOf<Long?>(currentMillis) }
    var selectedEndDate by remember { mutableStateOf<Long?>(currentMillis + defaultDateSpacing) }

    val context = LocalContext.current
    var timeline by remember { mutableStateOf<List<TimelineItem>?>(null) }

    LaunchedEffect(selectedStartDate, selectedEndDate) {
        if (selectedStartDate == null || selectedEndDate == null) {
            selectedStartDate = currentMillis
            selectedEndDate = currentMillis + defaultDateSpacing
        }

        timeline = null
        timeline = PrepService.loadTimeline(
            context,
            OrgUnitDataGuid.EMS_SATTLEDT,
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(selectedStartDate!!), ZoneId.systemDefault()
            ),
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(selectedEndDate!!), ZoneId.systemDefault()
            ),
        ).getOrNull()
    }

    val stationOptions = listOf("Sattledt", "Wels")
    var selectedStationIndex by remember { mutableIntStateOf(0) }

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = {
                Text(stringResource(R.string.main_schedule_title))
            },
        )
    }, bottomBar = bottomBar, content = { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    stationOptions.forEachIndexed { index, label ->
                        ToggleButton(
                            checked = selectedStationIndex == index,
                            onCheckedChange = { selectedStationIndex = index },
                            modifier = Modifier.semantics { role = Role.RadioButton },
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                stationOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            enabled = selectedStationIndex == index
                        ) {
                            Text(label)
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(2f))
                IconButton(
                    onClick = {
                        dateRangePickerState.setSelection(
                            selectedStartDate, selectedEndDate
                        )
                        showDatePicker = true
                    }) {
                    Icon(
                        Icons.Rounded.CalendarMonth,
                        contentDescription = stringResource(R.string.main_schedule_accessibility_datepicker)
                    )
                }
            }
            if (timeline != null) {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(timeline ?: emptyList()) { item ->
                        when (item) {
                            is TimelineItem.Date -> {
                                AppDateInfo(date = item.date)
                            }

                            is TimelineItem.Duty -> {
                                AppDutyCard(
                                    duty = item.duty, selfId = PrepService.getSelf()?.identifier
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoadingIndicator()
                }
            }
        }
    })

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = {
            showDatePicker = false
        }, confirmButton = {
            TextButton(onClick = {
                selectedStartDate = dateRangePickerState.selectedStartDateMillis ?: currentMillis
                selectedEndDate = dateRangePickerState.selectedEndDateMillis
                    ?: (currentMillis + defaultDateSpacing)

                showDatePicker = false
            }) {
                Text(stringResource(R.string.main_schedule_datepicker_confirm))
            }
        }, dismissButton = {
            TextButton(onClick = {
                showDatePicker = false
            }) {
                Text(stringResource(R.string.main_schedule_datepicker_dismiss))
            }
        }) {
            DateRangePicker(
                modifier = Modifier.fillMaxWidth(),
                state = dateRangePickerState,
                showModeToggle = false,
                title = {
                    DateRangePickerDefaults.DateRangePickerTitle(
                        dateRangePickerState.displayMode,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp)
                    )
                },
                headline = {
                    DateRangePickerDefaults.DateRangePickerHeadline(
                        selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis,
                        selectedEndDateMillis = dateRangePickerState.selectedEndDateMillis,
                        displayMode = dateRangePickerState.displayMode,
                        DatePickerDefaults.dateFormatter(),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                })
        }
    }
}