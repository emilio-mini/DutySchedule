@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.base_dutycard_clipboard_duty
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.emiliomini.dutyschedule.shared.api.getPlatformClipboardApi
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.debug.DebugFlags
import me.emiliomini.dutyschedule.shared.supportsAlarms
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.getIcon
import me.emiliomini.dutyschedule.shared.util.resourceString
import me.emiliomini.dutyschedule.shared.util.toInstant
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@Composable
fun MinimalDutyCard(
    modifier: Modifier = Modifier,
    duty: MinimalDutyDefinition,
    demo: Boolean = false,
    type: CardListItemType = CardListItemType.DEFAULT,
    onClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState?
) {
    val scope = rememberCoroutineScope()
    val timeFormatter = "HH:mm"
    val dateFormatter = "dd.MM.yyyy"
    val clipboardLabel = stringResource(Res.string.base_dutycard_clipboard_duty)

    Box(modifier = modifier.wrapContentSize().combinedClickable(onClick = onClick, onLongClick = {
        scope.launch {
            getPlatformClipboardApi().copyToClipboard(Json.encodeToString(duty), clipboardLabel)
        }
    }), contentAlignment = Alignment.TopEnd) {
        CardListItem(
            headlineContent = {
                Text(stringResource(duty.type.resourceString()))
            },
            supportingContent = {
                Column {
                    if (DebugFlags.SHOW_DEBUG_INFO.active()) {
                        Text(duty.typeString, fontWeight = FontWeight.ExtraBold)
                    }
                    if (duty.vehicle != null && duty.vehicle.isNotBlank()) {
                        Text(duty.vehicle, fontWeight = FontWeight.SemiBold)
                    }
                    for (employee in duty.staff) {
                        Text(employee)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${
                                duty.begin.format(timeFormatter)
                            } - ${
                                duty.end.format(timeFormatter)
                            }"
                        )
                        Text(
                            duty.begin.format(dateFormatter)
                        )
                    }
                }
            },
            leadingContent = {
                Column(verticalArrangement = Arrangement.Top) {
                    Icon(
                        duty.type.getIcon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            type = type
        )
        if (!demo && supportsAlarms) {
            AlarmToggle(dutyBegin = duty.begin.toInstant(), guid = duty.guid, snackbarHostState = snackbarHostState)
        }
    }
}
