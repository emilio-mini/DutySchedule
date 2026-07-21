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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.shared.api.APPLICATION_CONTEXT
import me.emiliomini.dutyschedule.shared.datastores.DutyType
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Timestamp
import me.emiliomini.dutyschedule.shared.datastores.UserPreferences
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.ui.main.activity.MainActivity
import kotlin.time.ExperimentalTime

private val WidgetCornerRadius = 20.dp

class NextDutyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        APPLICATION_CONTEXT = context.applicationContext

        var nextDuty: MinimalDutyDefinition? = null
        var self: Employee? = null
        var prefs = UserPreferences()
        try {
            StorageService.initialize()
            nextDuty =
                StorageService.UPCOMING_DUTIES.getOrDefault().minimalDutyDefinitions.firstOrNull()
            self = StorageService.SELF.getOrDefault().takeIf { it.name.isNotBlank() }
            prefs = StorageService.USER_PREFERENCES.getOrDefault()
        } catch (e: Exception) {
        }

        provideContent {
            GlanceTheme(colors = widgetColorProviders(context, prefs)) {
                NextDutyWidgetContent(
                    context = context,
                    nextDuty = nextDuty,
                    selfName = self?.name,
                )
            }
        }
    }
}

@Composable
private fun NextDutyWidgetContent(
    context: Context,
    nextDuty: MinimalDutyDefinition?,
    selfName: String?,
) {
    val openAppAction = actionStartActivity<MainActivity>()
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetCornerRadius)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(openAppAction),
        contentAlignment = Alignment.Center
    ) {
        if (nextDuty == null) {
            EmptyState(context)
        } else {
            // fillMaxSize (not fillMaxWidth): home-screen widgets resize in coarse grid-cell
            // increments, so leftover vertical space is unavoidable at some sizes. Rather than
            // fighting it, the divider spans the full available height and the info column below
            // centers its content within that height, turning any extra space into even
            // breathing room above/below instead of a dead gap under the staff list.
            Row(modifier = GlanceModifier.fillMaxSize().padding(16.dp)) {
                // Left time column with vertical divider
                Column(
                    modifier = GlanceModifier.fillMaxHeight(),
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                ) {
                    Text(
                        text = nextDuty.begin.format("HH:mm"),
                        style = TextStyle(
                            color = GlanceTheme.colors.outline,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .width(1.dp)
                            .background(GlanceTheme.colors.outline)
                    ) { }
                    Spacer(modifier = GlanceModifier.height(6.dp))
                    Text(
                        text = nextDuty.end.format("HH:mm"),
                        style = TextStyle(
                            color = GlanceTheme.colors.outline,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.width(14.dp))

                // Right info column: header row (type/vehicle + date) + staff rows, centered
                // vertically so it settles in the middle of the card rather than clinging to top.
                Column(
                    modifier = GlanceModifier.defaultWeight().fillMaxHeight(),
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    val typeLabel = nextDuty.typeString.stripTypeBrackets()
                        .ifBlank { context.getString(nextDuty.type.toStringRes()) }

                    val vehicleName = nextDuty.vehicle
                        ?: context.getString(nextDuty.type.toStringRes())

                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                    ) {
                        DutyInfoRow(
                            modifier = GlanceModifier.defaultWeight(),
                            iconRes = nextDuty.type.toWidgetIconRes(),
                            label = typeLabel,
                            name = vehicleName,
                            state = StaffState.TYPE_HEADER,
                        )
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        DateBadge(nextDuty.begin)
                    }

                    for (name in nextDuty.staff) {
                        Spacer(modifier = GlanceModifier.height(10.dp))
                        val isDriver = name == nextDuty.driverName
                        val isSelf = name == selfName
                        DutyInfoRow(
                            iconRes = if (isDriver) R.drawable.ic_widget_steering_wheel else R.drawable.ic_widget_person,
                            label = "",
                            name = name,
                            state = when {
                                isSelf -> StaffState.SELF
                                else -> StaffState.DEFAULT
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(context: Context) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_calendar),
            contentDescription = null,
            modifier = GlanceModifier.size(28.dp),
            colorFilter = ColorFilter.tint(GlanceTheme.colors.outline)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = context.getString(R.string.widget_next_duty_empty),
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        )
    }
}

@Composable
private fun DateBadge(begin: Timestamp) {
    Row(
        modifier = GlanceModifier
            .background(GlanceTheme.colors.primaryContainer)
            .cornerRadius(10.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Text(
            text = begin.format("d"),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onPrimaryContainer,
            )
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Text(
            text = begin.format("MMM"),
            style = TextStyle(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onPrimaryContainer,
            )
        )
    }
}

private enum class StaffState { TYPE_HEADER, SELF, DEFAULT }

@Composable
private fun DutyInfoRow(
    modifier: GlanceModifier = GlanceModifier,
    iconRes: Int,
    label: String,
    name: String,
    state: StaffState,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Vertical.CenterVertically) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = null,
            modifier = GlanceModifier.size(22.dp),
            colorFilter = ColorFilter.tint(
                when (state) {
                    StaffState.TYPE_HEADER -> GlanceTheme.colors.primary
                    StaffState.SELF -> GlanceTheme.colors.primary
                    StaffState.DEFAULT -> GlanceTheme.colors.onSurfaceVariant
                }
            )
        )
        Spacer(modifier = GlanceModifier.width(10.dp))
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
                    color = when (state) {
                        StaffState.SELF -> GlanceTheme.colors.primary
                        StaffState.TYPE_HEADER, StaffState.DEFAULT -> GlanceTheme.colors.onSurface
                    },
                    fontWeight = when (state) {
                        StaffState.SELF -> FontWeight.Bold
                        StaffState.TYPE_HEADER -> FontWeight.Medium
                        StaffState.DEFAULT -> FontWeight.Normal
                    },
                    fontSize = 14.sp,
                )
            )
        }
    }
}

private fun DutyType.toWidgetIconRes(): Int = when (this) {
    DutyType.EMS, DutyType.HAEND, DutyType.BLOOD_DONATION_SERVICE -> R.drawable.ic_widget_ambulance
    DutyType.TRAINING, DutyType.DRILL, DutyType.RECERTIFICATION -> R.drawable.ic_widget_school
    DutyType.VEHICLE_TRAINING -> R.drawable.ic_widget_steering_wheel
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
