package me.emiliomini.dutyschedule.shared.api

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent

/**
 * Fully-qualified names of the app's [androidx.glance.appwidget.GlanceAppWidgetReceiver]
 * subclasses. Referenced by name (rather than by class) because they live in the `app` module,
 * which depends on `shared` — not the other way around.
 */
private val WIDGET_RECEIVER_CLASS_NAMES = listOf(
    "me.emiliomini.dutyschedule.widget.NextDutyWidgetReceiver",
    "me.emiliomini.dutyschedule.widget.DutyCalendarWidgetReceiver"
)

class AndroidWidgetApi : PlatformWidgetApi {
    override suspend fun refreshWidgets() {
        val manager = AppWidgetManager.getInstance(APPLICATION_CONTEXT)
        for (className in WIDGET_RECEIVER_CLASS_NAMES) {
            val component = ComponentName(APPLICATION_CONTEXT.packageName, className)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) continue

            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                setComponent(component)
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            APPLICATION_CONTEXT.sendBroadcast(intent)
        }
    }
}

actual fun initializePlatformWidgetApi(): PlatformWidgetApi = AndroidWidgetApi()
