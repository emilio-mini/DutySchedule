package me.emiliomini.dutyschedule.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Import your new colors
import me.emiliomini.dutyschedule.ui.theme.LightTextFieldBackgroundColor
import me.emiliomini.dutyschedule.ui.theme.DarkTextFieldBackgroundColor
import me.emiliomini.dutyschedule.ui.theme.LightTextFieldPlaceholderColor
import me.emiliomini.dutyschedule.ui.theme.DarkTextFieldPlaceholderColor
import me.emiliomini.dutyschedule.ui.theme.LightTextFieldLeadingIconColor
import me.emiliomini.dutyschedule.ui.theme.DarkTextFieldLeadingIconColor

// Base Red color
private val Red = Color(0xFFD32F2F)

// Light Color Scheme derived from Red
private val LightColorScheme = lightColorScheme(
    primary = Red,
    onPrimary = Color.White, // 0xFFFFFFFF
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410000),
    secondary = Color(0xFF775651),
    onSecondary = Color.White, // 0xFFFFFFFF
    secondaryContainer = Color(0xFFFFDAD4),
    onSecondaryContainer = Color(0xFF2C1512),
    tertiary = Color(0xFF705C2E),
    onTertiary = Color.White, // 0xFFFFFFFF
    tertiaryContainer = Color(0xFFFBDFA6),
    onTertiaryContainer = Color(0xFF251A00),
    error = Color(0xFFBA1A1A),
    onError = Color.White, // 0xFFFFFFFF
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A19),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF201A19),
    surfaceVariant = LightTextFieldBackgroundColor, // Updated
    onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857370)
)

// Dark Color Scheme derived from Red
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4A8),
    onPrimary = Color(0xFF690000),
    primaryContainer = Color(0xFF930000),
    onPrimaryContainer = Color(0xFFFFDAD4),
    secondary = Color(0xFFE7BDB6),
    onSecondary = Color(0xFF442925),
    secondaryContainer = Color(0xFF5D3F3B),
    onSecondaryContainer = Color(0xFFFFDAD4),
    tertiary = Color(0xFFDEC48C),
    onTertiary = Color(0xFF3F2E04),
    tertiaryContainer = Color(0xFF574419),
    onTertiaryContainer = Color(0xFFFBDFA6),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF201A19),
    onBackground = Color(0xFFEDE0DE),
    surface = Color(0xFF201A19),
    onSurface = Color(0xFFEDE0DE),
    surfaceVariant = DarkTextFieldBackgroundColor, // Updated
    onSurfaceVariant = Color(0xFFD8C2BF),
    outline = Color(0xFFA08C8A)
)

// Composable vals to provide theme-aware TextField colors
val currentTextFieldPlaceholderColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkTextFieldPlaceholderColor else LightTextFieldPlaceholderColor

val currentTextFieldLeadingIconColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkTextFieldLeadingIconColor else LightTextFieldLeadingIconColor

@Composable
fun DutyScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Dynamic color remains disabled to use the custom Red theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography.kt exists and is correctly defined
        content = content
    )
}
