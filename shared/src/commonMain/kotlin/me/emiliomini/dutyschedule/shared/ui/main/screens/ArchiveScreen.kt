@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import dutyschedule.shared.generated.resources.main_archive_section_duties_title
import dutyschedule.shared.generated.resources.main_archive_title
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.YearlyDutyItems
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.components.CardListItemType
import me.emiliomini.dutyschedule.shared.ui.components.LazyCardColumn
import me.emiliomini.dutyschedule.shared.ui.components.MinimalDutyCard
import me.emiliomini.dutyschedule.shared.ui.components.PieChart
import me.emiliomini.dutyschedule.shared.ui.icons.ChevronLeft
import me.emiliomini.dutyschedule.shared.ui.icons.ChevronRight
import me.emiliomini.dutyschedule.shared.util.countByProperty
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.resourceString
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier, bottomBar: @Composable (() -> Unit) = {}
) {
    val past by StorageService.PAST_DUTIES.collectAsState()

    val currentYear = Clock.System.now().format("yyyy")
    var duties by remember { mutableStateOf<List<MinimalDutyDefinition>>(emptyList()) }
    var loaded by remember { mutableStateOf(true) }
    var selectedYear by remember { mutableStateOf(currentYear) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(DutyScheduleService.isLoggedIn, selectedYear) {
        if (past.years.containsKey(selectedYear.toInt())) {
            return@LaunchedEffect
        }

        loaded = false
        if (!DutyScheduleService.isLoggedIn) {
            return@LaunchedEffect
        }

        DutyScheduleService.loadPast(selectedYear)
        loaded = true
    }

    LaunchedEffect(past, selectedYear) {
        duties = past.years.getOrElse(
            selectedYear.toInt()
        ) { YearlyDutyItems() }.minimalDutyDefinitions
    }

    Screen(
        title = stringResource(Res.string.main_archive_title),
        actions = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        selectedYear = (selectedYear.toInt() - 1).toString()
                    }) {
                    Icon(ChevronLeft, contentDescription = null)
                }
                Text(selectedYear, style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = {
                        if (selectedYear == currentYear) {
                            return@IconButton
                        }
                        selectedYear = (selectedYear.toInt() + 1).toString()
                    }, enabled = selectedYear != currentYear
                ) {
                    Icon(ChevronRight, contentDescription = null)
                }
            }
        },
        bottomBar = bottomBar,
        pullToRefresh = PullToRefreshOptions(
            isRefreshing = !loaded,
            onRefresh = {
                loaded = false
                scope.launch {
                    DutyScheduleService.loadPast(selectedYear)
                    loaded = true
                }
            }
        )
    ) { innerPadding ->
        Column(
            modifier = modifier.padding(innerPadding)
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PieChart(
                data = duties.countByProperty { it.type },
                labelTextResource = { type, _, _ -> type.resourceString() })
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(
                        Res.string.main_archive_section_duties_title,
                        duties.size,
                        duties.sumOf { it.duration } / 60),
                    color = MaterialTheme.colorScheme.primary)
                if (!loaded) LoadingIndicator(Modifier.size(24.dp))
            }
            LazyCardColumn {
                itemsIndexed(
                    items = duties, key = { _, duty -> duty.guid }) { index, duty ->
                    MinimalDutyCard(
                        duty = duty,
                        type = if (index == 0 && duties.size == 1) CardListItemType.SINGLE else if (index == 0) CardListItemType.TOP else if (index == duties.size - 1) CardListItemType.BOTTOM else CardListItemType.DEFAULT
                    )

                    if (index == duties.size - 1) {
                        Spacer(Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}