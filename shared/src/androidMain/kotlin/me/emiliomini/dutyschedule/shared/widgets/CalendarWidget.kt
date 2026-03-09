package me.emiliomini.dutyschedule.shared.widgets

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text

class CalendarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {

        provideContent {
            // create your AppWidget here
            Text("Hello World")
        }
    }
}