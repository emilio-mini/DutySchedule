@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.main.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_schedule_accessibility_datepicker
import dutyschedule.shared.generated.resources.main_schedule_datepicker_confirm
import dutyschedule.shared.generated.resources.main_schedule_datepicker_dismiss
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.emiliomini.dutyschedule.shared.datastores.OrgDay
import me.emiliomini.dutyschedule.shared.datastores.Requirement
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.mappings.ShiftType
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.scaffold.Action
import me.emiliomini.dutyschedule.shared.services.scaffold.ScaffoldService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.components.AppDateInfo
import me.emiliomini.dutyschedule.shared.ui.components.AssignConfirmSheet
import me.emiliomini.dutyschedule.shared.ui.components.DutyCardCarousel
import me.emiliomini.dutyschedule.shared.ui.components.EmployeeDetailSheet
import me.emiliomini.dutyschedule.shared.ui.icons.CalendarMonth
import me.emiliomini.dutyschedule.shared.ui.icons.ChevronLeft
import me.emiliomini.dutyschedule.shared.ui.icons.ChevronRight
import me.emiliomini.dutyschedule.shared.ui.main.entry.NavItemId
import me.emiliomini.dutyschedule.shared.util.WEEK_MILLIS
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.startOfWeek
import me.emiliomini.dutyschedule.shared.util.toInstant
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ScheduleScreen(
    modifier: Modifier = Modifier, paddingValues: PaddingValues
) {
    val timeNow = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentMillis = LocalDateTime(
        year = timeNow.year,
        month = timeNow.month,
        day = timeNow.day,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0
    ).toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()

    val userPreferences by StorageService.USER_PREFERENCES.collectAsState()

    val dateRangePickerState = rememberDateRangePickerState()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedStartDate by remember { mutableStateOf<Long?>(currentMillis) }
    var selectedEndDate by remember { mutableStateOf<Long?>(currentMillis + WEEK_MILLIS) }

    var timeline by remember { mutableStateOf<List<OrgDay>?>(null) }

    var allowedOrgs by remember { mutableStateOf<List<String>?>(null) }
    var selectedOrg by remember { mutableStateOf<String?>(null) }
    val orgItems by StorageService.ORG_ITEMS.collectAsState()

    var showThanks by remember { mutableStateOf(false) }

    var pendingPlanGuid by remember { mutableStateOf<String?>(null) }
    var requirement by remember { mutableStateOf<Requirement?>(null) }
    var creating by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userPreferences) {
        allowedOrgs = userPreferences.allowedOrgs
        selectedOrg = userPreferences.lastSelectedOrg
    }

    LaunchedEffect(orgItems, allowedOrgs) {
        val default = DutyScheduleService.self?.defaultOrg
        val primaryOrg = if (default != null) DutyScheduleService.getOrg(default) else null

        if (selectedOrg.isNullOrBlank()) {
            selectedOrg = primaryOrg?.guid ?: allowedOrgs?.firstOrNull()
        }
    }

    LaunchedEffect(
        DutyScheduleService.isLoggedIn, selectedStartDate, selectedEndDate, selectedOrg
    ) {
        if (selectedOrg == null || !DutyScheduleService.isLoggedIn) {
            return@LaunchedEffect
        }

        if (selectedStartDate == null || selectedEndDate == null) {
            selectedStartDate = currentMillis
            selectedEndDate = currentMillis + WEEK_MILLIS
        }

        StorageService.USER_PREFERENCES.update {
            it.copy(
                lastSelectedOrg = selectedOrg!!
            )
        }

        timeline = null
        timeline = DutyScheduleService.loadTimeline(
            selectedOrg!!,
            Instant.fromEpochMilliseconds(selectedStartDate!!),
            Instant.fromEpochMilliseconds(selectedEndDate!!)
        )

        DutyScheduleService.loadMessages(
            selectedOrg!!,
            Instant.fromEpochMilliseconds(selectedStartDate!!),
            Instant.fromEpochMilliseconds(selectedEndDate!!)
        )
    }

    val stationScrollState = rememberScrollState()
    var detailViewEmployee by remember { mutableStateOf<Slot?>(null) }

    ScaffoldService.setActionsForScreen(
        NavItemId.SCHEDULE, listOf(
            Action({ run ->
                IconButton(onClick = { run() }) {
                    Icon(
                        ChevronLeft, contentDescription = null
                    )
                }
            }, {
                val startOfWeek = selectedStartDate.toInstant().startOfWeek()
                selectedStartDate = startOfWeek.toEpochMilliseconds() - WEEK_MILLIS
                selectedEndDate = selectedStartDate!! + WEEK_MILLIS
            }), Action({
                Text(
                    "KW${selectedStartDate.toInstant().format("ww")}",
                    style = MaterialTheme.typography.titleMedium
                )
            }), Action({ run ->
                IconButton(onClick = { run() }) {
                    Icon(
                        ChevronRight, contentDescription = null
                    )
                }
            }, {
                val startOfWeek = selectedStartDate.toInstant().startOfWeek()
                selectedStartDate = startOfWeek.toEpochMilliseconds() + WEEK_MILLIS
                selectedEndDate = selectedStartDate!! + WEEK_MILLIS
            })
        )
    )

    Screen(
        modifier = modifier, paddingValues = paddingValues
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).padding(horizontal = 20.dp)
                .padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.weight(2f).horizontalScroll(stationScrollState),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    orgItems.orgs.values.filter { allowedOrgs?.contains(it.guid) ?: false }
                        .forEachIndexed { index, proto ->
                            ToggleButton(
                                checked = selectedOrg == proto.guid,
                                onCheckedChange = { selectedOrg = proto.guid },
                                modifier = Modifier.semantics { role = Role.RadioButton },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    allowedOrgs?.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                                colors = ToggleButtonDefaults.toggleButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                )
                            ) {
                                Text(proto.title)
                            }
                        }
                }
                IconButton(
                    modifier = Modifier.width(48.dp), onClick = {
                        dateRangePickerState.setSelection(
                            selectedStartDate, selectedEndDate
                        )
                        showDatePicker = true
                    }) {
                    Icon(
                        CalendarMonth,
                        contentDescription = stringResource(Res.string.main_schedule_accessibility_datepicker)
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
                        AppDateInfo(date = item.date.toInstant())
                        DutyCardCarousel(
                            duties = item.dayShifts,
                            groups = item.groups.associateBy { it.guid },
                            shiftType = ShiftType.DAY_SHIFT,
                            onEmployeeClick = {
                                detailViewEmployee = it
                            },
                            onDutyClick = { planGuid, req ->
                                pendingPlanGuid = planGuid
                                requirement = req
                            })
                        DutyCardCarousel(
                            duties = item.nightShifts,
                            groups = item.groups.associateBy { it.guid },
                            shiftType = ShiftType.NIGHT_SHIFT,
                            onEmployeeClick = {
                                detailViewEmployee = it
                            },
                            onDutyClick = { planGuid, req ->
                                pendingPlanGuid = planGuid
                                requirement = req
                            })
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
    }

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = {
            showDatePicker = false
        }, confirmButton = {
            TextButton(onClick = {
                val startMillis = dateRangePickerState.selectedStartDateMillis ?: currentMillis
                var endMillis =
                    dateRangePickerState.selectedEndDateMillis ?: (startMillis + WEEK_MILLIS)

                if (endMillis < startMillis) {
                    endMillis = startMillis + WEEK_MILLIS
                }

                selectedStartDate = startMillis
                selectedEndDate = endMillis

                showDatePicker = false
            }) {
                Text(stringResource(Res.string.main_schedule_datepicker_confirm))
            }
        }, dismissButton = {
            TextButton(onClick = {
                showDatePicker = false
            }) {
                Text(stringResource(Res.string.main_schedule_datepicker_dismiss))
            }
        }) {
            DateRangePicker(
                modifier = Modifier.fillMaxWidth(),
                state = dateRangePickerState,
                showModeToggle = false,
                title = {
                    DateRangePickerDefaults.DateRangePickerTitle(
                        dateRangePickerState.displayMode,
                        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 36.dp)
                    )
                },
                headline = {
                    DateRangePickerDefaults.DateRangePickerHeadline(
                        selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis,
                        selectedEndDateMillis = dateRangePickerState.selectedEndDateMillis,
                        displayMode = dateRangePickerState.displayMode,
                        DatePickerDefaults.dateFormatter(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                })
        }
    }

    if (detailViewEmployee != null) {
        EmployeeDetailSheet(
            slot = detailViewEmployee,
            orgs = orgItems.orgs.values.toList(),
            onDismiss = { detailViewEmployee = null })
    }

    AssignConfirmSheet(
        planGuid = pendingPlanGuid,
        req = requirement,
        loading = creating,
        error = createError,
        onDismiss = {
            if (!creating) {
                pendingPlanGuid = null; createError = null
            }
        },
        onConfirm = {
            val guid = pendingPlanGuid ?: return@AssignConfirmSheet
            creating = true
            createError = null
            scope.launch {

                val resp = DutyScheduleService.createAndAllocateDuty(guid)
                val ok = resp?.success == true

                creating = false
                if (ok) {
                    pendingPlanGuid = null
                    // Refresh timeline
                    timeline = DutyScheduleService.loadTimeline(
                        selectedOrg!!,
                        Instant.fromEpochMilliseconds(selectedStartDate!!),
                        Instant.fromEpochMilliseconds(selectedEndDate!!)
                    )

                    showThanks = true
                } else {
                    createError = if (resp == null) {
                        "Network error"
                    } else {
                        listOfNotNull(
                            resp.alertMessage,
                            resp.errorMessages.joinToString().ifBlank { null }).joinToString(" – ")
                            .ifBlank { "Unbekannter Fehler" }
                    }
                }
            }
        })

    if (showThanks) {
        AlertDialog(
            onDismissRequest = { showThanks = false },
            title = { Text("Danke") },
            text = { Text("Dienst wurde eingetragen.") },
            confirmButton = {
                TextButton(onClick = { showThanks = false }) { Text("OK") }
            })
    }
}