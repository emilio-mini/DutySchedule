package me.emiliomini.dutyschedule.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.models.prep.MinimalDutyDefinition
import me.emiliomini.dutyschedule.services.network.PrepService
import me.emiliomini.dutyschedule.ui.components.ArcProgressIndicator
import me.emiliomini.dutyschedule.ui.components.UpcomingDutyCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(modifier: Modifier = Modifier, bottomBar: @Composable (() -> Unit) = {}) {
    val currentYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
    var upcomingDuties by remember { mutableStateOf<List<MinimalDutyDefinition>>(emptyList()) }
    var pastDuties by remember { mutableStateOf<List<MinimalDutyDefinition>>(emptyList()) }

    val requiredMinutes = 144 * 60
    var minutesSum by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        upcomingDuties = PrepService.loadUpcoming().getOrNull() ?: emptyList()
        pastDuties = PrepService.loadPast(currentYear).getOrNull() ?: emptyList()
    }

    LaunchedEffect(pastDuties) {
        minutesSum = pastDuties.sumOf { it.duration }.toFloat()
        progress = minutesSum / requiredMinutes
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.main_dashboard_title))
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
                    strokeWidth = 24.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${minutesSum / 60}", style = MaterialTheme.typography.titleLarge)
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
                Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        itemsIndexed(
                            items = upcomingDuties,
                            key = { _, duty -> duty.guid }) { index, duty ->
                            UpcomingDutyCard(
                                guid = duty.guid,
                                vehicle = duty.vehicle,
                                employees = duty.staff,
                                begin = duty.begin,
                                end = duty.end,
                                type = duty.type
                            )
                        }
                    }
                }
            }
        }
    )
}
