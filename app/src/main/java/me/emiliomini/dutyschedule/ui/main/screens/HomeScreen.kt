package me.emiliomini.dutyschedule.ui.main.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.employee.AssignedEmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgDayProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgItemsProto
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.prep.ShiftType
import me.emiliomini.dutyschedule.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.services.storage.DataKeys
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.services.storage.ProtoMapViewModel
import me.emiliomini.dutyschedule.services.storage.ProtoMapViewModelFactory
import me.emiliomini.dutyschedule.services.storage.StorageService
import me.emiliomini.dutyschedule.services.storage.ViewModelKeys
import me.emiliomini.dutyschedule.ui.components.AppDateInfo
import me.emiliomini.dutyschedule.ui.components.AssignConfirmSheet
import me.emiliomini.dutyschedule.ui.components.DutyCardCarousel
import me.emiliomini.dutyschedule.ui.components.EmployeeDetailSheet
import me.emiliomini.dutyschedule.util.toOffsetDateTime
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview()
fun HomeScreen(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {},
    viewModel: ProtoMapViewModel<OrgItemsProto, OrgProto> = viewModel(
        key = ViewModelKeys.ORG_ITEMS,
        factory = ProtoMapViewModelFactory<OrgItemsProto, OrgProto>(
            DataStores.ORG_ITEMS
        ) { it.orgsMap }
    )
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

    var timeline by remember { mutableStateOf<List<OrgDayProto>?>(null) }

    var allowedOrgs by remember { mutableStateOf<List<String>?>(null) }
    var selectedOrg by remember { mutableStateOf<String?>(null) }
    val orgs by viewModel.flow.collectAsStateWithLifecycle(
        initialValue = emptyMap<String, OrgProto>()
    )

    var showThanks by remember { mutableStateOf(false) }

    var pendingPlanGuid by remember { mutableStateOf<String?>(null) }
    var reqirement by remember { mutableStateOf<RequirementProto?>(null) }
    var creating by remember { mutableStateOf(false) }
    var createError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        val orgs = StorageService.load(DataKeys.ALLOWED_ORGS)
        if (orgs == null) {
            return@LaunchedEffect
        }

        allowedOrgs = orgs.split(StorageService.DEFAULT_SEPARATOR)
    }

    LaunchedEffect(orgs, allowedOrgs) {
        val default = DutyScheduleService.self?.defaultOrg
        val primaryOrg = if (default != null) DutyScheduleService.getOrg(default) else null

        selectedOrg = if (primaryOrg != null) {
            primaryOrg.guid
        } else {
            allowedOrgs?.first()
        }
    }

    LaunchedEffect(
        DutyScheduleService.isLoggedIn,
        selectedStartDate,
        selectedEndDate,
        selectedOrg
    ) {
        if (selectedOrg == null || !DutyScheduleService.isLoggedIn) {
            return@LaunchedEffect
        }

        if (selectedStartDate == null || selectedEndDate == null) {
            selectedStartDate = currentMillis
            selectedEndDate = currentMillis + defaultDateSpacing
        }

        timeline = null
        timeline = DutyScheduleService.loadTimeline(
            selectedOrg!!,
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(selectedStartDate!!), ZoneId.systemDefault()
            ),
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(selectedEndDate!!), ZoneId.systemDefault()
            ),
        ).getOrNull()

        DutyScheduleService.loadMessages(
            selectedOrg!!,
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(selectedStartDate!!), ZoneId.systemDefault()
            ),
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(selectedEndDate!!), ZoneId.systemDefault()
            )
        )
    }

    val stationScrollState = rememberScrollState()
    var detailViewEmployee by remember { mutableStateOf<AssignedEmployeeProto?>(null) }

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
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier
                        .weight(2f)
                        .horizontalScroll(stationScrollState),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    orgs.values.filter { allowedOrgs?.contains(it.guid) ?: false }
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
                    modifier = Modifier.width(48.dp),
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
                        AppDateInfo(date = item.date.toOffsetDateTime())
                        DutyCardCarousel(
                            duties = item.dayShiftList,
                            shiftType = ShiftType.DAY_SHIFT,
                            onEmployeeClick = {
                                detailViewEmployee = it
                            },
                            onDutyClick = { planGuid, req ->
                                pendingPlanGuid = planGuid
                                reqirement = req
                            })
                        DutyCardCarousel(
                            duties = item.nightShiftList,
                            shiftType = ShiftType.NIGHT_SHIFT,
                            onEmployeeClick = {
                                detailViewEmployee = it
                            },
                            onDutyClick = { planGuid, req ->
                                pendingPlanGuid = planGuid
                                reqirement = req
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
    })

    if (showDatePicker) {
        DatePickerDialog(onDismissRequest = {
            showDatePicker = false
        }, confirmButton = {
            TextButton(onClick = {
                selectedStartDate = dateRangePickerState.selectedStartDateMillis ?: currentMillis
                selectedEndDate =
                    dateRangePickerState.selectedEndDateMillis?.plus(24 * 60 * 60 * 1000L)
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
                            .padding(horizontal = 16.dp)
                            .padding(top = 36.dp)
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
            assignedEmployee = detailViewEmployee,
            orgs = orgs.values.toList(),
            onDismiss = { detailViewEmployee = null }
        )
    }

    AssignConfirmSheet(
        planGuid = pendingPlanGuid,
        req = reqirement,
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
                /*val resp = Result.success(
                    CreateDutyResponse(
                        success = true,
                        errorMessages = emptyList(),
                        alertMessage = null,
                        successMessage = "",
                        changedDataId = "",
                        duty = CreatedDuty(
                            guid = "",
                            dataGuid = "",
                            orgUnitDataGuid = "",
                            begin = OffsetDateTime.now(),
                            end = OffsetDateTime.now(),
                            requirementGroupChildDataGuid = "",
                            resourceTypeDataGuid = "",
                            skillDataGuid = "",
                            skillCharacterisationDataGuid = "",
                            shiftDataGuid = "",
                            planBaseDataGuid = "",
                            planBaseEntryDataGuid = "",
                            allocationDataGuid = "",
                            allocationRessourceDataGuid = "",
                            released = 0,
                            bookable = 0,
                            resourceName = ""
                        )
                    )
                )*/
                val ok = resp.getOrNull()?.success == true

                creating = false
                if (ok) {
                    pendingPlanGuid = null
                    // Timeline refreshen:
                    timeline = DutyScheduleService.loadTimeline(
                        selectedOrg!!,
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochMilli(selectedStartDate!!),
                            ZoneId.systemDefault()
                        ),
                        OffsetDateTime.ofInstant(
                            Instant.ofEpochMilli(selectedEndDate!!),
                            ZoneId.systemDefault()
                        ),
                    ).getOrNull()

                    showThanks = true
                } else {
                    createError = resp.fold(
                        onSuccess = { r ->
                            listOfNotNull(
                                r.alertMessage,
                                r.errorMessages.joinToString().ifBlank { null })
                                .joinToString(" – ")
                                .ifBlank { "Unbekannter Fehler" }
                        },
                        onFailure = { it.message ?: "Netzwerkfehler" }
                    )
                }
            }
        }
    )

    if (showThanks) {
        AlertDialog(
            onDismissRequest = { showThanks = false },
            title = { Text("Danke") },
            text = { Text("Dienst wurde eingetragen.") },
            confirmButton = {
                TextButton(onClick = { showThanks = false }) { Text("OK") }
            }
        )
    }
}