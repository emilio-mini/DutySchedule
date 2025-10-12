@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.components

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.shared.datastores.DutyType
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.debug.DebugFlags
import me.emiliomini.dutyschedule.shared.ui.icons.Ambulance
import me.emiliomini.dutyschedule.shared.ui.icons.Coffee
import me.emiliomini.dutyschedule.shared.ui.icons.EcgHeart
import me.emiliomini.dutyschedule.shared.ui.icons.EmojiPeople
import me.emiliomini.dutyschedule.shared.ui.icons.Exercise
import me.emiliomini.dutyschedule.shared.ui.icons.Festival
import me.emiliomini.dutyschedule.shared.ui.icons.MedicalServices
import me.emiliomini.dutyschedule.shared.ui.icons.QuestionMark
import me.emiliomini.dutyschedule.shared.ui.icons.School
import me.emiliomini.dutyschedule.shared.ui.icons.SteeringWheel
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.resourceString
import me.emiliomini.dutyschedule.shared.util.toInstant
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime

@Composable
fun MinimalDutyCard(
    modifier: Modifier = Modifier,
    duty: MinimalDutyDefinition,
    demo: Boolean = false
) {
    val timeFormatter = "HH:mm"
    val dateFormatter = "dd.MM.yyyy"

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
                        when (duty.type) {
                            DutyType.EMS -> Ambulance
                            DutyType.TRAINING -> School
                            DutyType.MEET -> EmojiPeople
                            DutyType.DRILL -> Exercise
                            DutyType.VEHICLE_TRAINING -> SteeringWheel
                            DutyType.RECERTIFICATION -> EcgHeart
                            DutyType.HAEND -> MedicalServices
                            DutyType.ADMINISTRATIVE -> Coffee
                            DutyType.EVENT -> Festival
                            else -> QuestionMark
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
        )
        if (!demo) {
            AlarmToggle(dutyBegin = duty.begin.toInstant(), guid = duty.guid)
        }
    }
}
