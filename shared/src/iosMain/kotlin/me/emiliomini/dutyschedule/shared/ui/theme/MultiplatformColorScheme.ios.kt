package me.emiliomini.dutyschedule.shared.ui.theme

import androidx.compose.material3.ColorScheme

actual fun isDynamicColorSupported(): Boolean = false

actual fun platformColorScheme(darkTheme: Boolean, colorPreset: ColorPreset): ColorScheme {
    return staticColorScheme(darkTheme, colorPreset)
}
