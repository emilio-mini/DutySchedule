package me.emiliomini.dutyschedule.ui.main.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MedicalInformation
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Textsms
import androidx.compose.material.icons.rounded.Whatsapp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.org.OrgProto
import me.emiliomini.dutyschedule.models.prep.AssignedEmployee
import me.emiliomini.dutyschedule.models.prep.Requirement
import me.emiliomini.dutyschedule.models.prep.TimelineItem
import me.emiliomini.dutyschedule.services.network.PrepService
import me.emiliomini.dutyschedule.services.storage.DataKeys
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.services.storage.StorageService
import me.emiliomini.dutyschedule.services.storage.viewmodels.OrgListViewModel
import me.emiliomini.dutyschedule.services.storage.viewmodels.OrgListViewModelFactory
import me.emiliomini.dutyschedule.ui.components.AppDateInfo
import me.emiliomini.dutyschedule.ui.components.AppDutyCard
import me.emiliomini.dutyschedule.ui.components.icons.Ambulance
import me.emiliomini.dutyschedule.ui.components.icons.SteeringWheel
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview()
fun HomeScreen(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {},
    viewModel: OrgListViewModel = viewModel(
        factory = OrgListViewModelFactory(DataStores.ORG_ITEMS)
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

    val context = LocalContext.current
    var timeline by remember { mutableStateOf<List<TimelineItem>?>(null) }

    var allowedOrgs by remember { mutableStateOf<List<String>?>(null) }
    var selectedOrg by remember { mutableStateOf<String?>(null) }
    val orgs by viewModel.orgsFlow.collectAsStateWithLifecycle(
        initialValue = emptyList<OrgProto>()
    )

    LaunchedEffect(Unit) {
        val orgs = StorageService.load(DataKeys.ALLOWED_ORGS)
        if (orgs == null) {
            return@LaunchedEffect
        }

        allowedOrgs = orgs.split(StorageService.DEFAULT_SEPARATOR)
        selectedOrg = allowedOrgs?.first()
    }

    LaunchedEffect(selectedStartDate, selectedEndDate, selectedOrg) {
        if (selectedOrg == null) {
            return@LaunchedEffect
        }

        if (selectedStartDate == null || selectedEndDate == null) {
            selectedStartDate = currentMillis
            selectedEndDate = currentMillis + defaultDateSpacing
        }

        timeline = null
        timeline = PrepService.loadTimeline(
            selectedOrg!!,
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(selectedStartDate!!), ZoneId.systemDefault()
            ),
            OffsetDateTime.ofInstant(
                Instant.ofEpochMilli(selectedEndDate!!), ZoneId.systemDefault()
            ),
        ).getOrNull()

        PrepService.loadMessages(
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

    val detailActionsScrollState = rememberScrollState()
    val detailViewState = rememberModalBottomSheetState()
    var detailViewEmployee by remember { mutableStateOf<AssignedEmployee?>(null) }

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
                    orgs.filter { allowedOrgs?.contains(it.guid) ?: false }
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
                        when (item) {
                            is TimelineItem.Date -> {
                                AppDateInfo(date = item.date)
                            }

                            is TimelineItem.Duty -> {
                                AppDutyCard(
                                    duty = item.duty,
                                    selfId = PrepService.getSelf()?.identifier,
                                    onEmployeeClick = {
                                        detailViewEmployee = it
                                    }
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
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val localZoneId = ZoneId.systemDefault()

        ModalBottomSheet(
            onDismissRequest = {
                detailViewEmployee = null
            },
            sheetState = detailViewState
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    detailViewEmployee!!.employee.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    detailViewEmployee!!.employee.identifier ?: "",
                    modifier = Modifier.padding(bottom = 4.dp),
                    style = MaterialTheme.typography.bodyMediumEmphasized
                )
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (detailViewEmployee!!.info.isNotBlank()) {
                            ListItem(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                leadingContent = {
                                    Icon(
                                        Icons.Rounded.Info,
                                        contentDescription = null
                                    )
                                },
                                headlineContent = {
                                    Text(detailViewEmployee!!.info)
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.main_schedule_infobox_info))
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                        ListItem(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            leadingContent = {
                                when (detailViewEmployee!!.requirement) {
                                    Requirement.SEW -> Icon(
                                        Ambulance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Requirement.EL -> Icon(
                                        SteeringWheel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    Requirement.TF -> Icon(
                                        Icons.Rounded.MedicalInformation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )

                                    else -> Icon(
                                        Icons.Rounded.Badge,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            headlineContent = {
                                Text(
                                    stringResource(detailViewEmployee!!.requirement.getResourceString())
                                )
                            },
                            supportingContent = {
                                Text(stringResource(R.string.main_schedule_infobox_assigned_role))
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                        ListItem(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            leadingContent = {
                                Icon(
                                    Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            headlineContent = {
                                Text(
                                    "${
                                        detailViewEmployee!!.begin.atZoneSameInstant(localZoneId)
                                            .format(timeFormatter)
                                    } - ${
                                        detailViewEmployee!!.end.atZoneSameInstant(localZoneId)
                                            .format(timeFormatter)
                                    }"
                                )
                            },
                            supportingContent = {
                                Text(
                                    detailViewEmployee!!.begin.format(
                                        DateTimeFormatter.ofPattern("d MMMM yyyy")
                                    )
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                    }
                }

                val nowMillis = OffsetDateTime.now().toInstant().toEpochMilli()
                val messages =
                    PrepService.getMessages()[detailViewEmployee!!.employee.resourceTypeGuid]
                        ?: emptyList()
                val filteredMessages = messages.filter {
                    it.displayFrom.toInstant()
                        .toEpochMilli() <= nowMillis && it.displayTo.toInstant()
                        .toEpochMilli() >= nowMillis
                }
                if (filteredMessages.isNotEmpty()) {
                    Spacer(Modifier)
                    Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (message in messages) {

                                ListItem(
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            shape = RoundedCornerShape(4.dp)
                                        ),
                                    leadingContent = {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.Notes,
                                            contentDescription = null
                                        )
                                    },
                                    headlineContent = {
                                        Text(message.message)
                                    },
                                    supportingContent = {
                                        Text(message.title)
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = Color.Transparent
                                    )
                                )

                            }
                        }
                    }
                }
                Spacer(Modifier)
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (detailViewEmployee!!.employee.defaultOrg.isNotBlank()) {
                            ListItem(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                leadingContent = {
                                    Icon(Icons.Rounded.Business, contentDescription = null)
                                },
                                headlineContent = {
                                    var primaryOrg =
                                        orgs.firstOrNull { it.abbreviation == detailViewEmployee!!.employee.defaultOrg }
                                    if (primaryOrg == null) {
                                        primaryOrg =
                                            orgs.firstOrNull { it.identifier == detailViewEmployee!!.employee.defaultOrg }
                                    }

                                    Text(if (primaryOrg != null) primaryOrg.title else detailViewEmployee!!.employee.defaultOrg)
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.main_schedule_infobox_primary))
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                        if (detailViewEmployee!!.employee.birthdate != null) {
                            ListItem(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                leadingContent = {
                                    Icon(Icons.Rounded.Cake, contentDescription = null)
                                },
                                headlineContent = {
                                    Text(
                                        detailViewEmployee!!.employee.birthdate!!.format(
                                            DateTimeFormatter.ofPattern("d MMMM yyyy")
                                        )
                                    )
                                },
                                supportingContent = {
                                    Text(stringResource(R.string.main_schedule_infobox_birthday))
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
                if (detailViewEmployee!!.requirement != Requirement.SEW) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(detailActionsScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                val uri = "tel:${detailViewEmployee!!.employee.phone}".toUri()
                                val intent = Intent(Intent.ACTION_DIAL, uri)
                                context.startActivity(intent)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        AssistChipDefaults.IconSize
                                    )
                                )
                            },
                            label = {
                                Text(stringResource(R.string.main_schedule_infobox_actions_call))
                            },
                            enabled = detailViewEmployee!!.employee.phone.isNotBlank()
                        )
                        AssistChip(
                            onClick = {
                                val uri = "smsto:${detailViewEmployee!!.employee.phone}".toUri()
                                val intent = Intent(Intent.ACTION_SENDTO, uri)
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Textsms,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        AssistChipDefaults.IconSize
                                    )
                                )
                            },
                            label = {
                                Text(stringResource(R.string.main_schedule_infobox_actions_sms))
                            },
                            enabled = detailViewEmployee!!.employee.phone.isNotBlank()
                        )
                        AssistChip(
                            onClick = {
                                val fullNumber =
                                    detailViewEmployee!!.employee.phone.filter { it.isDigit() }
                                val uri =
                                    "https://wa.me/$fullNumber/".toUri()

                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    data = uri
                                    setPackage("com.whatsapp")
                                }

                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Whatsapp,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        AssistChipDefaults.IconSize
                                    )
                                )
                            },
                            label = {
                                Text(stringResource(R.string.main_schedule_infobox_actions_whatsapp))
                            },
                            enabled = detailViewEmployee!!.employee.phone.isNotBlank()
                        )
                        AssistChip(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = "mailto:".toUri()
                                    putExtra(
                                        Intent.EXTRA_EMAIL,
                                        arrayOf(detailViewEmployee!!.employee.email)
                                    )
                                }
                                if (intent.resolveActivity(context.packageManager) != null) {
                                    context.startActivity(intent)
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.AlternateEmail,
                                    contentDescription = null,
                                    modifier = Modifier.size(
                                        AssistChipDefaults.IconSize
                                    )
                                )
                            },
                            label = {
                                Text(stringResource(R.string.main_schedule_infobox_actions_mail))
                            },
                            enabled = detailViewEmployee!!.employee.email.isNotBlank()
                        )
                    }
                }
            }
        }
    }
}