package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.EmojiPeople
import androidx.compose.material.icons.rounded.MedicalServices
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
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyTypeProto
import me.emiliomini.dutyschedule.datastore.prep.duty.MinimalDutyDefinitionProto
import me.emiliomini.dutyschedule.debug.DebugFlags
import me.emiliomini.dutyschedule.ui.components.icons.Ambulance
import me.emiliomini.dutyschedule.ui.components.icons.EcgHeart
import me.emiliomini.dutyschedule.ui.components.icons.Exercise
import me.emiliomini.dutyschedule.ui.components.icons.SteeringWheel
import me.emiliomini.dutyschedule.util.resourceString
import me.emiliomini.dutyschedule.util.toOffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MinimalDutyCard(
    modifier: Modifier = Modifier,
    duty: MinimalDutyDefinitionProto,
    demo: Boolean = false
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val localZoneId = ZoneId.systemDefault()

    Box(modifier = modifier.wrapContentSize(), contentAlignment = Alignment.TopEnd) {
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
                    for (employee in duty.staffList) {
                        Text(employee)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${
                                duty.begin.toOffsetDateTime().atZoneSameInstant(localZoneId)
                                    .format(timeFormatter)
                            }-${
                                duty.end.toOffsetDateTime().atZoneSameInstant(localZoneId)
                                    .format(timeFormatter)
                            }"
                        )
                        Text(
                            duty.begin.toOffsetDateTime().atZoneSameInstant(localZoneId)
                                .format(dateFormatter)
                        )
                    }
                }
            },
            leadingContent = {
                Column(verticalArrangement = Arrangement.Top) {
                    Icon(
                        when (duty.type) {
                            DutyTypeProto.EMS -> Ambulance
                            DutyTypeProto.TRAINING -> Icons.Rounded.School
                            DutyTypeProto.MEET -> Icons.Rounded.EmojiPeople
                            DutyTypeProto.DRILL -> Exercise
                            DutyTypeProto.VEHICLE_TRAINING -> SteeringWheel
                            DutyTypeProto.RECERTIFICATION -> EcgHeart
                            DutyTypeProto.HAEND -> Icons.Rounded.MedicalServices
                            DutyTypeProto.ADMINISTRATIVE -> Icons.Rounded.Coffee
                            else -> Icons.Rounded.QuestionMark
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
        )
        if (!demo) {
            AlarmToggle(dutyBegin = duty.begin.toOffsetDateTime(), guid = duty.guid)
        }
    }
}
