package me.emiliomini.dutyschedule.ui.main.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import me.emiliomini.dutyschedule.models.prep.MinimalDutyDefinition
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
    var upcomingDuties by remember { mutableStateOf<List<MinimalDutyDefinition>>(emptyList()) }
    var pastDuties by remember { mutableStateOf<List<MinimalDutyDefinition>>(emptyList()) }

    val requiredMinutes = 144 * 60
    var minutesSum by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }

    var pastLoaded by remember { mutableStateOf(false) }
    var upcomingLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        upcomingDuties = PrepService.loadUpcoming().getOrNull() ?: emptyList()
        upcomingLoaded = true
        pastDuties = PrepService.loadPast(currentYear).getOrNull() ?: emptyList()
        minutesSum = pastDuties.sumOf { it.duration }.toFloat()
        progress = minutesSum / requiredMinutes
        pastLoaded = true
    }

    val animatedMinutes by animateFloatAsState(
        targetValue = minutesSum,
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
                    val employee = PrepService.getSelf()
                    if (employee != null) {
                        EmployeeAvatar(employee = employee, onLogout = onLogout)
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
                    .padding(horizontal = 20.dp)
                    .offset(y = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ArcProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    sizeDp = 232.dp,
                    progress = progress,
                    strokeWidth = 24.dp,
                    pending = !pastLoaded
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                String.format(Locale.getDefault(), "%.2f", animatedMinutes / 60f),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                " / ${requiredMinutes / 60}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(stringResource(R.string.main_dashboard_hours))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.main_dashboard_section_upcoming_title),
                    color = MaterialTheme.colorScheme.primary
                )
                if (!upcomingLoaded) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator()
                    }
                } else {
                    LazyCardColumn {
                        itemsIndexed(
                            items = upcomingDuties,
                            key = { _, duty -> duty.guid }) { index, duty ->
                            MinimalDutyCard(duty = duty)
                        }
                    }
                }
            }
        }
    )
}
