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
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.UserPreferences
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.CalendarDay
import me.emiliomini.dutyschedule.shared.util.CalendarMonth
import me.emiliomini.dutyschedule.shared.util.buildCalendarMonth
import me.emiliomini.dutyschedule.ui.main.activity.MainActivity
import kotlin.time.ExperimentalTime

private val WidgetCornerRadius = 20.dp

class DutyCalendarWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        APPLICATION_CONTEXT = context.applicationContext

        var duties: List<MinimalDutyDefinition> = emptyList()
        var self: Employee? = null
        var prefs = UserPreferences()
        try {
            StorageService.initialize()
            val upcoming = StorageService.UPCOMING_DUTIES.getOrDefault().minimalDutyDefinitions
            val past = StorageService.PAST_DUTIES.getOrDefault().years.values
                .flatMap { it.minimalDutyDefinitions }
            duties = upcoming + past
            self = StorageService.SELF.getOrDefault().takeIf { it.name.isNotBlank() }
            prefs = StorageService.USER_PREFERENCES.getOrDefault()
        } catch (e: Exception) {
        }

        val month = buildCalendarMonth(duties = duties, selfName = self?.name)

        provideContent {
            GlanceTheme(colors = widgetColorProviders(context, prefs)) {
                DutyCalendarWidgetContent(month = month)
            }
        }
    }
}

@Composable
private fun DutyCalendarWidgetContent(month: CalendarMonth) {
    val openAppAction = actionStartActivity<MainActivity>()
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(WidgetCornerRadius)
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(openAppAction)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_calendar),
                contentDescription = null,
                modifier = GlanceModifier.size(18.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = month.monthLabel,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(modifier = GlanceModifier.fillMaxWidth()) {
            for (label in month.weekdayLabels) {
                Text(
                    text = label.take(2).uppercase(),
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = GlanceTheme.colors.outline,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                    )
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(4.dp))

        for (week in month.weeks) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                for (day in week) {
                    CalendarDayCell(day = day, modifier = GlanceModifier.defaultWeight())
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(day: CalendarDay, modifier: GlanceModifier = GlanceModifier) {
    val textColor = when {
        day.isToday -> GlanceTheme.colors.onPrimary
        !day.isCurrentMonth -> GlanceTheme.colors.outline
        day.isSelfDuty -> GlanceTheme.colors.primary
        else -> GlanceTheme.colors.onSurface
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Box(
            modifier = GlanceModifier
                .size(26.dp)
                .cornerRadius(13.dp)
                .background(if (day.isToday) GlanceTheme.colors.primary else GlanceTheme.colors.widgetBackground),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = TextStyle(
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = if (day.isToday || day.isSelfDuty) FontWeight.Bold else FontWeight.Normal,
                )
            )
        }
        Spacer(modifier = GlanceModifier.height(3.dp))
        val shiftIconTint = if (day.isSelfDuty) GlanceTheme.colors.primary else GlanceTheme.colors.outline
        if (day.isCurrentMonth && (day.hasDayShift || day.hasNightShift)) {
            Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
                if (day.hasDayShift) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_sun),
                        contentDescription = null,
                        modifier = GlanceModifier.size(9.dp),
                        colorFilter = ColorFilter.tint(shiftIconTint)
                    )
                }
                if (day.hasDayShift && day.hasNightShift) {
                    Spacer(modifier = GlanceModifier.width(2.dp))
                }
                if (day.hasNightShift) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_widget_moon),
                        contentDescription = null,
                        modifier = GlanceModifier.size(9.dp),
                        colorFilter = ColorFilter.tint(shiftIconTint)
                    )
                }
            }
        } else {
            Spacer(modifier = GlanceModifier.size(9.dp))
        }
    }
}
