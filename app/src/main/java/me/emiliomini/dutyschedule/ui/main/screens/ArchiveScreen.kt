package me.emiliomini.dutyschedule.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.ui.components.LazyCardColumn
import me.emiliomini.dutyschedule.ui.components.MinimalDutyCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit) = {}
) {
    val currentYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy"))
    var duties by remember { mutableStateOf<List<MinimalDutyDefinitionProto>?>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf(currentYear) }

    LaunchedEffect(DutyScheduleService.isLoggedIn, selectedYear) {
        loaded = false
        if (!DutyScheduleService.isLoggedIn) {
            return@LaunchedEffect
        }

        duties = DutyScheduleService.loadPast(selectedYear).getOrNull()
        loaded = true
    }

    Scaffold(modifier = modifier, topBar = {
        TopAppBar(
            title = {
                Text(stringResource(R.string.main_archive_title))
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
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = null)
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
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null)
                    }
                }
            }
        )
    }, bottomBar = bottomBar, content = { innerPadding ->
        Column(modifier = modifier.padding(innerPadding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.main_archive_section_duties_title, duties?.size ?: 0, (duties?.sumOf { it.duration } ?: 0) / 60),
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