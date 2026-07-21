package me.emiliomini.dutyschedule.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DutyCalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DutyCalendarWidget()
}
