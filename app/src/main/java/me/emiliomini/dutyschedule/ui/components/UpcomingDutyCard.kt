package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmAdd
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.models.prep.DutyType
import me.emiliomini.dutyschedule.services.alarm.AlarmService
import me.emiliomini.dutyschedule.ui.components.icons.Ambulance
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun UpcomingDutyCard(
    modifier: Modifier = Modifier,
    guid: String,
    vehicle: String? = null,
    employees: List<String> = emptyList(),
    begin: OffsetDateTime,
    end: OffsetDateTime,
    type: DutyType
) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val localZoneId = ZoneId.systemDefault()

    ListItem(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(4.dp)
            ),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (vehicle != null) {
                        Text(vehicle, fontWeight = FontWeight.SemiBold)
                    }
                    for (employee in employees) {
                        Text(employee)
                    }
                }
                IconButton(
                    modifier = Modifier.offset(x = 12.dp, y = (-10).dp),
                    onClick = {
                        // TODO: Make the alarm button a separate component to avoid code duplication
                    }
                ) {
                    Icon(
                        if (AlarmService.isAlarmSet(
                                context,
                                guid.hashCode()
                            )
                        ) Icons.Rounded.AlarmOn else Icons.Rounded.AlarmAdd,
                        contentDescription = null
                    )
                }
            }
        },
        supportingContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${
                        begin.atZoneSameInstant(localZoneId).format(timeFormatter)
                    }-${
                        end.atZoneSameInstant(localZoneId).format(timeFormatter)
                    }"
                )
                Text(begin.atZoneSameInstant(localZoneId).format(dateFormatter))
            }
        },
        leadingContent = {
            Column(verticalArrangement = Arrangement.Top) {
                Icon(
                    when (type) {
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
}