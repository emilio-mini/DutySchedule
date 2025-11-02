@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.main.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_dashboard_hours
import dutyschedule.shared.generated.resources.main_dashboard_section_upcoming_title
import dutyschedule.shared.generated.resources.main_dashboard_title
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.debug.DebugFlags
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.prep.live.PrepService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.components.ArcProgressIndicator
import me.emiliomini.dutyschedule.shared.ui.components.CardListItemType
import me.emiliomini.dutyschedule.shared.ui.components.EmployeeAvatar
import me.emiliomini.dutyschedule.shared.ui.components.LazyCardColumn
import me.emiliomini.dutyschedule.shared.ui.components.MinimalDutyCard
import me.emiliomini.dutyschedule.shared.ui.icons.DeleteSweep
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.withinLast
import org.jetbrains.compose.resources.stringResource
import kotlin.math.floor
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {},
    onLogout: () -> Unit,
    onTriggerRestart: () -> Unit
) {
    val currentYear = Clock.System.now().format("yyyy")
    val upcomingDuties by StorageService.UPCOMING_DUTIES.collectAsState()
    val statistics by StorageService.STATISTICS.collectAsState()

    val requiredMinutes = 144 * 60f
    var progress by remember { mutableFloatStateOf(0f) }

    var hoursLoaded by remember { mutableStateOf(true) }
    var upcomingLoaded by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(DutyScheduleService.isLoggedIn) {
        if (!DutyScheduleService.isLoggedIn) {
            return@LaunchedEffect
        }

        if (!StorageService.UPCOMING_DUTIES.lastUpdated.withinLast(30.minutes)) {
            upcomingLoaded = false
            DutyScheduleService.loadUpcoming()
            upcomingLoaded = true
        }

        if (!StorageService.STATISTICS.lastUpdated.withinLast(30.minutes)) {
            hoursLoaded = false
            DutyScheduleService.loadHoursOfService(currentYear)
            hoursLoaded = true
        }
    }

    LaunchedEffect(statistics) {
        progress = statistics.minutesServed / requiredMinutes
    }

    val animatedMinutes by animateIntAsState(
        targetValue = statistics.minutesServed, animationSpec = tween(
            durationMillis = 500, easing = FastOutSlowInEasing
        ), label = "QuotaAnimation"
    )

    Screen(
        modifier = modifier,
        title = stringResource(Res.string.main_dashboard_title),
        actions = {
            if (DutyScheduleService.self != null) {
                if (DebugFlags.SHOW_DEBUG_INFO.active()) {
                    IconButton(onClick = {
                        scope.launch {
                            val userPreferences = StorageService.USER_PREFERENCES.getOrDefault()
                            StorageService.clear()
                            PrepService.logout()
                            PrepService.login(
                                userPreferences.username, userPreferences.password
                            )
                            onTriggerRestart()
                        }
                    }) {
                        Icon(DeleteSweep, contentDescription = null)
                    }
                }
                EmployeeAvatar(employee = DutyScheduleService.self!!, onLogout = onLogout)
                Spacer(Modifier.width(16.dp))
            }
        },
        bottomBar = bottomBar,
        pullToRefresh = PullToRefreshOptions(
            isRefreshing = !hoursLoaded || !upcomingLoaded,
            onRefresh = {
                scope.launch {
                    hoursLoaded = false
                    upcomingLoaded = false

                    DutyScheduleService.loadUpcoming()
                    DutyScheduleService.loadHoursOfService(currentYear)

                    hoursLoaded = true
                    upcomingLoaded = true
                }
            }
        )
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding)
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ArcProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                sizeDp = 232.dp,
                progress = progress,
                strokeWidth = 24.dp,
                pending = !hoursLoaded
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "${floor(((animatedMinutes / 60.0) * 100) / 100)}",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            " / ${floor(requiredMinutes / 60).toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(stringResource(Res.string.main_dashboard_hours))
                }
            }
            Spacer(Modifier.height(24.dp))
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
                        type = if (index == 0 && upcomingDuties.minimalDutyDefinitions.size == 1) CardListItemType.SINGLE else if (index == 0) CardListItemType.TOP else if (index == upcomingDuties.minimalDutyDefinitions.size - 1) CardListItemType.BOTTOM else CardListItemType.DEFAULT
                    )
                    if (index == upcomingDuties.minimalDutyDefinitions.size - 1) {
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
