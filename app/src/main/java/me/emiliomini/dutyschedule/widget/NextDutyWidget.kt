@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
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
            .padding(16.dp)
            .clickable(openAppAction),
        contentAlignment = Alignment.TopStart
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                text = context.getString(R.string.widget_next_duty_title),
                style = TextStyle(
                    color = GlanceTheme.colors.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            if (nextDuty == null) {
                Text(
                    text = context.getString(R.string.widget_next_duty_empty),
                    style = TextStyle(color = GlanceTheme.colors.onSurface)
                )
            } else {
                Text(
                    text = context.getString(nextDuty.type.toStringRes()),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )
                val vehicle = nextDuty.vehicle
                if (!vehicle.isNullOrBlank()) {
                    Text(
                        text = vehicle,
                        style = TextStyle(color = GlanceTheme.colors.secondary)
                    )
                }
                for (name in nextDuty.staff) {
                    Text(
                        text = name,
                        style = TextStyle(color = GlanceTheme.colors.onSurface)
                    )
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(
                        text = "${nextDuty.begin.format("HH:mm")} \u2013 ${nextDuty.end.format("HH:mm")}",
                        style = TextStyle(color = GlanceTheme.colors.onSurface)
                    )
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = nextDuty.begin.format("dd.MM.yyyy"),
                        style = TextStyle(color = GlanceTheme.colors.onSurface)
                    )
                }
            }
        }
    }
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
