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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_archive_section_duties_title
import dutyschedule.shared.generated.resources.main_archive_title
import me.emiliomini.dutyschedule.shared.datastores.DutyType
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
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
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {}
) {
    val currentYear = Clock.System.now().format("yyyy")
    var duties by remember { mutableStateOf<List<MinimalDutyDefinition>?>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(currentYear) }

    LaunchedEffect(DutyScheduleService.isLoggedIn, selectedYear) {
        loaded = false
        if (!DutyScheduleService.isLoggedIn) {
            return@LaunchedEffect
        }

        duties = DutyScheduleService.loadPast(selectedYear)
        loaded = true
    }

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = {
                Text(stringResource(Res.string.main_archive_title))
            },
            actions = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selectedYear = (selectedYear.toInt() - 1).toString()
                        }
                    ) {
                        Icon(ChevronLeft, contentDescription = null)
                    }
                    Text(selectedYear, style = MaterialTheme.typography.titleMedium)
                    IconButton(
                        onClick = {
                            if (selectedYear == currentYear) {
                                return@IconButton
                            }
                            selectedYear = (selectedYear.toInt() + 1).toString()
                        },
                        enabled = selectedYear != currentYear
                    ) {
                        Icon(ChevronRight, contentDescription = null)
                    }
                }
            }
        )
    }, bottomBar = bottomBar, content = { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PieChart<DutyType>(
                data = (duties ?: emptyList()).countByProperty { it.type },
                labelTextResource = { type, _, _ -> type.resourceString() }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(
                        Res.string.main_archive_section_duties_title,
                        duties?.size ?: 0,
                        (duties?.sumOf { it.duration } ?: 0) / 60),
                    color = MaterialTheme.colorScheme.primary
                )
                if (!loaded) LoadingIndicator(Modifier.size(24.dp))
            }
            LazyCardColumn {
                itemsIndexed(
                    items = duties ?: emptyList(),
                    key = { _, duty -> duty.guid }) { index, duty ->
                    MinimalDutyCard(duty = duty)
                }
            }
        }
    })
}