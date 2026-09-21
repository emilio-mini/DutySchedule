@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_dashboard_section_upcoming_title
import dutyschedule.shared.generated.resources.main_dashboard_upcoming_link_failed
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.countedDutyTypes
import me.emiliomini.dutyschedule.shared.datastores.minutesOf
import me.emiliomini.dutyschedule.shared.debug.DebugFlags
import me.emiliomini.dutyschedule.shared.services.CredentialService
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.prep.live.PrepService
import me.emiliomini.dutyschedule.shared.services.scaffold.Action
import me.emiliomini.dutyschedule.shared.services.scaffold.ScaffoldService
import me.emiliomini.dutyschedule.shared.services.scaffold.ScreenActions
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.components.CardListItemType
import me.emiliomini.dutyschedule.shared.ui.components.DutyTypeFilter
import me.emiliomini.dutyschedule.shared.ui.components.EmployeeAvatar
import me.emiliomini.dutyschedule.shared.ui.components.HoursSummary
import me.emiliomini.dutyschedule.shared.ui.components.LazyCardColumn
import me.emiliomini.dutyschedule.shared.ui.components.MinimalDutyCard
import me.emiliomini.dutyschedule.shared.ui.icons.DeleteSweep
import me.emiliomini.dutyschedule.shared.ui.main.entry.NavItemId
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.toScheduleFocus
import me.emiliomini.dutyschedule.shared.util.withinLast
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    onRestart: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    ScreenActions(NavItemId.DASHBOARD) {
        listOf(
            Action(element = { run ->
                IconButton(onClick = { run() }) {
                    Icon(
                        DeleteSweep, contentDescription = null
                    )
                }
            }, callback = {
                scope.launch {
                    val username = StorageService.USER_PREFERENCES.getOrDefault().username
                    val password = CredentialService.getPassword()
                    PrepService.logout()
                    if (password != null) {
                        PrepService.login(username, password)
                    }
                    onRestart()
                }
            }, visible = DebugFlags.SHOW_DEBUG_ACTIONS.active()),
            Action({ DutyTypeFilter() }),
            Action({ EmployeeAvatar(employee = DutyScheduleService.self, onLogout = onLogout) }),
            Action({ Spacer(Modifier.width(16.dp)) })
        )
    }

    val currentYear = Clock.System.now().format("yyyy")
    val upcomingDuties by StorageService.UPCOMING_DUTIES.collectAsState()
    val statistics by StorageService.STATISTICS.collectAsState()
    val userPreferences by StorageService.USER_PREFERENCES.collectAsState()

    val requiredMinutes = 144 * 60f
    val countedMinutes = statistics.minutesOf(userPreferences.countedDutyTypes())

    var hoursLoaded by remember { mutableStateOf(true) }
    var upcomingLoaded by remember { mutableStateOf(true) }


    val snackbarHostState = ScaffoldService.snackbarHostState

    LaunchedEffect(DutyScheduleService.isLoggedIn) {
        if (!DutyScheduleService.isLoggedIn) {
            return@LaunchedEffect
        }

        if (!StorageService.UPCOMING_DUTIES.lastUpdated.withinLast(30.minutes)) {
            upcomingLoaded = false
            try {
                DutyScheduleService.loadUpcoming()
            } finally {
                upcomingLoaded = true
            }
        }

        // The year check catches both the rollover into January and a store written before the
        // hours were split by type, without mistaking a year that genuinely holds no duties yet
        // for one that was never counted
        if (statistics.year != currentYear ||
            !StorageService.STATISTICS.lastUpdated.withinLast(30.minutes)
        ) {
            hoursLoaded = false
            try {
                DutyScheduleService.loadHoursOfService(currentYear)
            } finally {
                hoursLoaded = true
            }
        }

        DutyScheduleService.preloadTimeline()
        DutyScheduleService.resolveUpcomingDutyLinks()
    }

    // Tapping a duty jumps to its place in the schedule. The pairing is worked out in the
    // background after the list loads, so the tap only has to resolve it when it got there first
    var linking by remember { mutableStateOf(false) }
    val linkFailedMessage = stringResource(Res.string.main_dashboard_upcoming_link_failed)
    val openInSchedule: (MinimalDutyDefinition) -> Unit = { duty ->
        val known = DutyScheduleService.peekDutyLink(duty.guid)
        if (known != null) {
            ScaffoldService.focusSchedule(known.toScheduleFocus())
        } else if (!linking) {
            linking = true
            scope.launch {
                try {
                    val resolved = DutyScheduleService.resolveDutyLink(duty, retryUnresolved = true)
                    if (resolved != null) {
                        ScaffoldService.focusSchedule(resolved.toScheduleFocus())
                    } else {
                        snackbarHostState?.showSnackbar(linkFailedMessage)
                    }
                } finally {
                    linking = false
                }
            }
        }
    }

    Screen(
        modifier = modifier,
        paddingValues = paddingValues,
        pullToRefresh = PullToRefreshOptions(
            isRefreshing = !hoursLoaded || !upcomingLoaded, onRefresh = {
                scope.launch {
                    hoursLoaded = false
                    upcomingLoaded = false

                    try {
                        DutyScheduleService.loadUpcoming()
                        DutyScheduleService.loadHoursOfService(currentYear)
                    } finally {
                        hoursLoaded = true
                        upcomingLoaded = true
                    }
                }
            })
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding)
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HoursSummary(
                modifier = Modifier.fillMaxWidth(),
                countedMinutes = countedMinutes,
                requiredMinutes = requiredMinutes,
                statistics = statistics,
                upcomingDuties = upcomingDuties.minimalDutyDefinitions,
                pending = !hoursLoaded
            )
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(Res.string.main_dashboard_section_upcoming_title),
                    color = MaterialTheme.colorScheme.primary
                )
                if (!upcomingLoaded) LoadingIndicator(Modifier.size(24.dp))
            }
            LazyCardColumn {
                itemsIndexed(
                    items = upcomingDuties.minimalDutyDefinitions,
                    key = { _, duty -> duty.guid }) { index, duty ->
                    MinimalDutyCard(
                        duty = duty,
                        type = if (index == 0 && upcomingDuties.minimalDutyDefinitions.size == 1) CardListItemType.SINGLE else if (index == 0) CardListItemType.TOP else if (index == upcomingDuties.minimalDutyDefinitions.size - 1) CardListItemType.BOTTOM else CardListItemType.DEFAULT,
                        onClick = { openInSchedule(duty) },
                        snackbarHostState = snackbarHostState
                    )
                    if (index == upcomingDuties.minimalDutyDefinitions.size - 1) {
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
