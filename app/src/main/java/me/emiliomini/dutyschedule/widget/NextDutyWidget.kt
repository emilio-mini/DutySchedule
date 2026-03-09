@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.shared.api.APPLICATION_CONTEXT
import me.emiliomini.dutyschedule.shared.datastores.DutyType
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.ui.main.activity.MainActivity
import kotlin.time.ExperimentalTime

class NextDutyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        APPLICATION_CONTEXT = context.applicationContext

        val nextDuty: MinimalDutyDefinition? = try {
            StorageService.initialize()
            StorageService.UPCOMING_DUTIES.getOrDefault().minimalDutyDefinitions.firstOrNull()
        } catch (e: Exception) {
            null
        }

        provideContent {
            GlanceTheme {
                NextDutyWidgetContent(context = context, nextDuty = nextDuty)
            }
        }
    }
}

@Composable
private fun NextDutyWidgetContent(context: Context, nextDuty: MinimalDutyDefinition?) {
    val openAppAction = actionStartActivity<MainActivity>()
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(openAppAction),
        contentAlignment = Alignment.TopStart
    ) {
        if (nextDuty == null) {
            Text(
                text = context.getString(R.string.widget_next_duty_empty),
                modifier = GlanceModifier.padding(16.dp),
                style = TextStyle(color = GlanceTheme.colors.onSurface)
            )
        } else {
            Row(modifier = GlanceModifier.fillMaxSize()) {
                // Left time column with vertical divider
                Column(
                    modifier = GlanceModifier
                        .fillMaxHeight()
                        .padding(start = 16.dp, top = 16.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                ) {
                    Text(
                        text = nextDuty.begin.format("HH:mm"),
                        style = TextStyle(
                            color = GlanceTheme.colors.outline,
                            fontSize = 12.sp,
                        )
                    )
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .width(1.dp)
                            .background(GlanceTheme.colors.outline)
                    )
                    Text(
                        text = nextDuty.end.format("HH:mm"),
                        style = TextStyle(
                            color = GlanceTheme.colors.outline,
                            fontSize = 12.sp,
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = nextDuty.begin.format("dd.MM."),
                        style = TextStyle(
                            color = GlanceTheme.colors.outline,
                            fontSize = 10.sp,
                        )
                    )
                }

                // Right info column: vehicle/type row + staff rows
                Column(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .padding(start = 12.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    val typeLabel = nextDuty.typeString.stripTypeBrackets()
                        .ifBlank { context.getString(nextDuty.type.toStringRes()) }

                    val vehicleName = nextDuty.vehicle
                        ?: context.getString(nextDuty.type.toStringRes())

                    DutyInfoRow(
                        iconRes = nextDuty.type.toWidgetIconRes(),
                        label = typeLabel,
                        name = vehicleName,
                        highlight = true,
                    )

                    for (name in nextDuty.staff) {
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        DutyInfoRow(
                            iconRes = R.drawable.ic_widget_person,
                            label = "",
                            name = name,
                            highlight = false,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DutyInfoRow(
    iconRes: Int,
    label: String,
    name: String,
    highlight: Boolean,
) {
    Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp),
            colorFilter = ColorFilter.tint(
                if (highlight) GlanceTheme.colors.primary else GlanceTheme.colors.onSurface
            )
        )
        Spacer(modifier = GlanceModifier.width(12.dp))
        Column {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = TextStyle(
                        color = GlanceTheme.colors.outline,
                        fontSize = 11.sp,
                    )
                )
            }
            Text(
                text = name,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                )
            )
        }
    }
}

private fun DutyType.toWidgetIconRes(): Int = when (this) {
    DutyType.EMS, DutyType.HAEND, DutyType.BLOOD_DONATION_SERVICE -> R.drawable.ic_widget_ambulance
    DutyType.TRAINING, DutyType.DRILL, DutyType.VEHICLE_TRAINING, DutyType.RECERTIFICATION -> R.drawable.ic_widget_school
    else -> R.drawable.ic_widget_person
}

private fun DutyType.toStringRes(): Int = when (this) {
    DutyType.EMS -> R.string.data_dutytype_ems
    DutyType.TRAINING -> R.string.data_dutytype_training
    DutyType.MEET -> R.string.data_dutytype_meet
    DutyType.DRILL -> R.string.data_dutytype_drill
    DutyType.VEHICLE_TRAINING -> R.string.data_dutytype_vehicle_training
    DutyType.RECERTIFICATION -> R.string.data_dutytype_recertification
    DutyType.HAEND -> R.string.data_dutytype_haend
    DutyType.ADMINISTRATIVE -> R.string.data_dutytype_administrative
    DutyType.EVENT -> R.string.data_dutytype_event
    DutyType.BLOOD_DONATION_SERVICE -> R.string.data_dutytype_blood_donation_service
    DutyType.UNKNOWN -> R.string.data_dutytype_unknown
}

private fun String.stripTypeBrackets(): String =
    this.replace(Regex("^\\[\\s*|\\s*]$"), "").trim()
