package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.base_dutytypefilter_accessibility_open
import dutyschedule.shared.generated.resources.base_dutytypefilter_empty
import dutyschedule.shared.generated.resources.base_dutytypefilter_hours_format
import dutyschedule.shared.generated.resources.base_dutytypefilter_title
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.datastores.DutyType
import me.emiliomini.dutyschedule.shared.datastores.countedDutyTypes
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.icons.Close
import me.emiliomini.dutyschedule.shared.ui.icons.Filter
import me.emiliomini.dutyschedule.shared.util.resourceString
import org.jetbrains.compose.resources.stringResource
import kotlin.math.floor

/**
 * Picks which duty types the dashboard's quota ring adds up. Only types the user actually served
 * this year are offered, since ticking one they have no hours in would not change the total
 */
@Composable
fun DutyTypeFilter(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val statistics by StorageService.STATISTICS.collectAsState()
    val userPreferences by StorageService.USER_PREFERENCES.collectAsState()

    var expanded by remember { mutableStateOf(false) }

    val counted = userPreferences.countedDutyTypes()
    val servedTypes = remember(statistics) {
        statistics.minutesByDutyType.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.value }
    }

    val toggle: (DutyType, Boolean) -> Unit = { type, checked ->
        scope.launch {
            StorageService.USER_PREFERENCES.update { preferences ->
                val selection = preferences.countedDutyTypes().let {
                    if (checked) it + type else it - type
                }

                preferences.copy(
                    countedDutyTypesConfigured = true,
                    countedDutyTypes = selection.toList()
                )
            }
        }
    }

    IconButton(modifier = modifier, onClick = { expanded = !expanded }) {
        Icon(
            Filter,
            contentDescription = stringResource(Res.string.base_dutytypefilter_accessibility_open)
        )
    }

    if (!expanded) {
        return
    }

    Dialog(onDismissRequest = { expanded = false }) {
        // A year can touch every duty type, so the panel is capped against the window and the list
        // inside it scrolls. The weight keeps the title and the close button in place while it does,
        // and not filling means a short list still wraps rather than stretching to the cap
        BoxWithConstraints {
            Card(
                modifier = Modifier.heightIn(max = this.maxHeight * PANEL_MAX_HEIGHT_FRACTION),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
            ) {
                Box {
                    IconButton(onClick = { expanded = false }) {
                        Icon(Close, contentDescription = null)
                    }
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(Res.string.base_dutytypefilter_title))

                        if (servedTypes.isEmpty()) {
                            Text(
                                stringResource(Res.string.base_dutytypefilter_empty),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            return@Column
                        }

                        CardColumn(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                        ) {
                            servedTypes.forEachIndexed { index, (type, minutes) ->
                                val checked = counted.contains(type)

                                CardListItem(
                                    modifier = Modifier.clickable(true) { toggle(type, !checked) },
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    headlineContent = {
                                        Text(stringResource(type.resourceString()))
                                    },
                                    supportingContent = {
                                        Text(
                                            stringResource(
                                                Res.string.base_dutytypefilter_hours_format,
                                                floor((minutes / 60.0) * 100) / 100
                                            )
                                        )
                                    },
                                    trailingContent = {
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = { toggle(type, it) })
                                    },
                                    type = when {
                                        servedTypes.size == 1 -> CardListItemType.SINGLE
                                        index == 0 -> CardListItemType.TOP
                                        index == servedTypes.lastIndex -> CardListItemType.BOTTOM
                                        else -> CardListItemType.DEFAULT
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Leaves the dialog clear of the window edges no matter how many types the year holds */
private const val PANEL_MAX_HEIGHT_FRACTION = 0.9f
