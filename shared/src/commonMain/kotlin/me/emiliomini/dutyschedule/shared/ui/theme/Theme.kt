package me.emiliomini.dutyschedule.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** themeMode: 0 = system, 1 = light, 2 = dark */
@Composable
fun DutyScheduleTheme(
    themeMode: Int = 0,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }
    val colorScheme = platformColorScheme(darkTheme, dynamicColor)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = objectivityTypography(),
        content = content
    )
}
