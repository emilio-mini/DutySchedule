package me.emiliomini.dutyschedule.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * The tones both schemes are built from, named the way Material names the steps of a tonal palette.
 * Every role below is assigned from these: a role left out falls back to Material's own baseline
 * palette, whose neutrals are tinted violet and read cold against the warm ones here.
 *
 * The neutral steps continue the ramp the scheme already stood on -- tone 10 is the dark surface and
 * tone 90 the dark text -- at the same hue and chroma, so the containers are the surface colour at
 * other lightnesses rather than a second grey.
 */
private val Primary10 = Color(0xFF410000)
private val Primary20 = Color(0xFF690000)
private val Primary30 = Color(0xFF930000)
private val Primary40 = Color(0xFFD32F2F)
private val Primary80 = Color(0xFFFFB4A8)
private val Primary90 = Color(0xFFFFDAD4)

private val Secondary10 = Color(0xFF2C1512)
private val Secondary20 = Color(0xFF442925)
private val Secondary30 = Color(0xFF5D3F3B)
private val Secondary40 = Color(0xFF775651)
private val Secondary80 = Color(0xFFE7BDB6)
private val Secondary90 = Color(0xFFFFDAD4)

private val Tertiary10 = Color(0xFF251A00)
private val Tertiary20 = Color(0xFF3F2E04)
private val Tertiary30 = Color(0xFF574419)
private val Tertiary40 = Color(0xFF705C2E)
private val Tertiary80 = Color(0xFFDEC48C)
private val Tertiary90 = Color(0xFFFBDFA6)

private val Error10 = Color(0xFF410002)
private val Error20 = Color(0xFF690005)
private val Error30 = Color(0xFF93000A)
private val Error40 = Color(0xFFBA1A1A)
private val Error80 = Color(0xFFFFB4AB)
private val Error90 = Color(0xFFFFDAD6)

private val Neutral0 = Color(0xFF000000)
private val Neutral4 = Color(0xFF140C0A)
private val Neutral6 = Color(0xFF181210)
private val Neutral10 = Color(0xFF201A19)
private val Neutral12 = Color(0xFF241E1D)
private val Neutral17 = Color(0xFF2F2827)
private val Neutral20 = Color(0xFF362F2E)
private val Neutral22 = Color(0xFF3A3332)
private val Neutral24 = Color(0xFF3F3736)
private val Neutral87 = Color(0xFFE4D7D5)
private val Neutral90 = Color(0xFFEDE0DE)
private val Neutral92 = Color(0xFFF2E5E3)
private val Neutral94 = Color(0xFFF8EBE9)
private val Neutral95 = Color(0xFFFBEEEC)
private val Neutral96 = Color(0xFFFEF1EF)
private val Neutral98 = Color(0xFFFFF6F4)
private val Neutral100 = Color(0xFFFFFFFF)

private val NeutralVariant30 = Color(0xFF534341)
private val NeutralVariant80 = Color(0xFFD8C2BF)

/** Off the neutral ramp on purpose, and what the app has always used for its page background */
private val Background = Color(0xFFFFFBFF)

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    onPrimary = Color.White,
    primaryContainer = Primary90,
    onPrimaryContainer = Primary10,
    inversePrimary = Primary80,
    secondary = Secondary40,
    onSecondary = Color.White,
    secondaryContainer = Secondary90,
    onSecondaryContainer = Secondary10,
    tertiary = Tertiary40,
    onTertiary = Color.White,
    tertiaryContainer = Tertiary90,
    onTertiaryContainer = Tertiary10,
    error = Error40,
    onError = Color.White,
    errorContainer = Error90,
    onErrorContainer = Error10,
    background = Background,
    onBackground = Neutral10,
    surface = Background,
    onSurface = Neutral10,
    surfaceVariant = Color(0xFFE9E9E9),
    onSurfaceVariant = NeutralVariant30,
    surfaceTint = Primary40,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    outline = Color(0xFF857370),
    outlineVariant = NeutralVariant80,
    scrim = Neutral0,
    surfaceBright = Neutral98,
    surfaceDim = Neutral87,
    surfaceContainerLowest = Neutral100,
    surfaceContainerLow = Neutral96,
    surfaceContainer = Neutral94,
    surfaceContainerHigh = Neutral92,
    surfaceContainerHighest = Neutral90,
    primaryFixed = Primary90,
    primaryFixedDim = Primary80,
    onPrimaryFixed = Primary10,
    onPrimaryFixedVariant = Primary30,
    secondaryFixed = Secondary90,
    secondaryFixedDim = Secondary80,
    onSecondaryFixed = Secondary10,
    onSecondaryFixedVariant = Secondary30,
    tertiaryFixed = Tertiary90,
    tertiaryFixedDim = Tertiary80,
    onTertiaryFixed = Tertiary10,
    onTertiaryFixedVariant = Tertiary30
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary80,
    onPrimary = Primary20,
    primaryContainer = Primary30,
    onPrimaryContainer = Primary90,
    inversePrimary = Primary40,
    secondary = Secondary80,
    onSecondary = Secondary20,
    secondaryContainer = Secondary30,
    onSecondaryContainer = Secondary90,
    tertiary = Tertiary80,
    onTertiary = Tertiary20,
    tertiaryContainer = Tertiary30,
    onTertiaryContainer = Tertiary90,
    error = Error80,
    onError = Error20,
    errorContainer = Error30,
    onErrorContainer = Error90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = NeutralVariant80,
    surfaceTint = Primary80,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    outline = Color(0xFFA08C8A),
    outlineVariant = NeutralVariant30,
    scrim = Neutral0,
    surfaceBright = Neutral24,
    surfaceDim = Neutral6,
    surfaceContainerLowest = Neutral4,
    surfaceContainerLow = Neutral10,
    surfaceContainer = Neutral12,
    surfaceContainerHigh = Neutral17,
    surfaceContainerHighest = Neutral22,
    // Fixed roles hold the same value in both schemes; that is what makes them fixed
    primaryFixed = Primary90,
    primaryFixedDim = Primary80,
    onPrimaryFixed = Primary10,
    onPrimaryFixedVariant = Primary30,
    secondaryFixed = Secondary90,
    secondaryFixedDim = Secondary80,
    onSecondaryFixed = Secondary10,
    onSecondaryFixedVariant = Secondary30,
    tertiaryFixed = Tertiary90,
    tertiaryFixedDim = Tertiary80,
    onTertiaryFixed = Tertiary10,
    onTertiaryFixedVariant = Tertiary30
)

@Composable
fun DutyScheduleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = objectivityTypography(),
        content = content
    )
}
