package me.emiliomini.dutyschedule.widget

import android.content.Context
import android.content.res.Configuration
import androidx.glance.material3.ColorProviders
import me.emiliomini.dutyschedule.shared.datastores.UserPreferences
import me.emiliomini.dutyschedule.shared.ui.theme.ColorPreset
import me.emiliomini.dutyschedule.shared.ui.theme.platformColorScheme

/**
 * Builds the widget's color providers from the user's in-app theme preferences (theme mode +
 * color preset), so home-screen widgets always match what's shown inside the app.
 */
internal fun widgetColorProviders(context: Context, prefs: UserPreferences) = run {
    val systemDark =
        context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    val darkTheme = when (prefs.themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }
    val scheme = platformColorScheme(darkTheme, ColorPreset.fromId(prefs.colorPreset))
    ColorProviders(light = scheme, dark = scheme)
}
