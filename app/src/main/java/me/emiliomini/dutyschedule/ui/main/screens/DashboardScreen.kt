package me.emiliomini.dutyschedule.ui.main.screens

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.services.network.PrepService
import me.emiliomini.dutyschedule.ui.components.ArcProgressIndicator
import me.emiliomini.dutyschedule.ui.components.EmployeeAvatar
import me.emiliomini.dutyschedule.ui.components.LazyCardColumn
import me.emiliomini.dutyschedule.ui.components.MinimalDutyCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {},
    onLogout: () -> Unit
) {
    val currentYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
    var upcomingDuties by remember { mutableStateOf<List<MinimalDutyDefinitionProto>>(emptyList()) }

    val requiredHours = 144
    var hoursServed by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }

    var hoursLoaded by remember { mutableStateOf(false) }
    var upcomingLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(PrepService.isLoggedIn) {
        upcomingDuties = PrepService.loadUpcoming()
        hoursServed = PrepService.loadHoursOfService(currentYear)
        progress = hoursServed / requiredHours

        if (PrepService.isLoggedIn) {
            hoursLoaded = true
            upcomingLoaded = true
        }
    }

    val animatedHours by animateFloatAsState(
        targetValue = hoursServed,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "QuotaAnimation"
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.main_dashboard_title))
                },
                actions = {
                    if (PrepService.self != null) {
                        EmployeeAvatar(employee = PrepService.self!!, onLogout = onLogout)
                        Spacer(Modifier.width(16.dp))
                    }
                }
            )
        },
        bottomBar = bottomBar,
        content = { innerPadding ->
            Column(
                modifier = modifier
                    .padding(innerPadding)
                    .padding(20.dp),
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
                                String.format(Locale.getDefault(), "%.2f", animatedHours),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                " / $requiredHours",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(stringResource(R.string.main_dashboard_hours))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.main_dashboard_section_upcoming_title),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!upcomingLoaded) LoadingIndicator(Modifier.size(24.dp))
                }
                LazyCardColumn {
                    itemsIndexed(
                        items = upcomingDuties,
                        key = { _, duty -> duty.guid }) { index, duty ->
                        MinimalDutyCard(duty = duty)
                    }
                }
            }
        }
    )
}
