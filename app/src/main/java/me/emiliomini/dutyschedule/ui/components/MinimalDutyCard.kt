package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.models.prep.DutyType
import me.emiliomini.dutyschedule.models.prep.MinimalDutyDefinition
import me.emiliomini.dutyschedule.ui.components.icons.Ambulance
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MinimalDutyCard(
    modifier: Modifier = Modifier,
    duty: MinimalDutyDefinition
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val localZoneId = ZoneId.systemDefault()

    Box(modifier = modifier, contentAlignment = Alignment.TopEnd) {
        CardListItem(
            modifier = Modifier.fillMaxSize(),
            headlineContent = {
                Text(stringResource(duty.type.getResourceString()))
            },
            supportingContent = {
                Column {
                    if (duty.vehicle != null) {
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
                                duty.begin.atZoneSameInstant(localZoneId).format(timeFormatter)
                            }-${
                                duty.end.atZoneSameInstant(localZoneId).format(timeFormatter)
                            }"
                        )
                        Text(duty.begin.atZoneSameInstant(localZoneId).format(dateFormatter))
                    }
                }
            },
            leadingContent = {
                Column(verticalArrangement = Arrangement.Top) {
                    Icon(
                        when (duty.type) {
                            DutyType.EMS -> Ambulance
                            DutyType.TRAINING -> Icons.Rounded.School
                            else -> Icons.Rounded.QuestionMark
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
        )
        AlarmToggle(dutyBegin = duty.begin, guid = duty.guid)
    }
}