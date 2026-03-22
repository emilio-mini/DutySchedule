package me.emiliomini.dutyschedule.shared.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import me.emiliomini.dutyschedule.shared.api.APPLICATION_CONTEXT

actual fun isDynamicColorSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

actual fun platformColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme {
    if (dynamicColor && isDynamicColorSupported()) {
        return if (darkTheme) dynamicDarkColorScheme(APPLICATION_CONTEXT)
        else dynamicLightColorScheme(APPLICATION_CONTEXT)
    }
    return if (darkTheme) DarkColorScheme else LightColorScheme
}
