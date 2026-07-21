package me.emiliomini.dutyschedule.shared.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

enum class ColorPreset(val id: Int) {
    DEFAULT(0),
    DYNAMIC(1),
    BLUE(2),
    GREEN(3),
    PURPLE(4);

    companion object {
        fun fromId(id: Int): ColorPreset = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/**
 * `lightColorScheme()`/`darkColorScheme()` only require primary/secondary/tertiary/background/
 * surface roles; every unspecified role (the surfaceDim/Bright/Container* ladder that Card,
 * NavigationBar, etc. actually render with) silently falls back to the M3 baseline scheme's
 * fixed neutral values. That made every static preset look identical in card and nav-bar
 * backgrounds. These helpers derive that ladder from each theme's own surface/onSurface tones
 * (matching the tonal steps of the real M3 baseline palette) so it stays hue-consistent per preset.
 */
private class SurfaceContainers(
    val dim: Color,
    val bright: Color,
    val containerLowest: Color,
    val containerLow: Color,
    val container: Color,
    val containerHigh: Color,
    val containerHighest: Color,
)

private fun lightSurfaceContainers(surface: Color, onSurface: Color) = SurfaceContainers(
    dim = lerp(surface, onSurface, 0.144f),
    bright = surface,
    containerLowest = lerp(surface, Color.White, 0.6f),
    containerLow = lerp(surface, onSurface, 0.031f),
    container = lerp(surface, onSurface, 0.049f),
    containerHigh = lerp(surface, onSurface, 0.080f),
    containerHighest = lerp(surface, onSurface, 0.108f),
)

private fun darkSurfaceContainers(surface: Color, onSurface: Color) = SurfaceContainers(
    dim = surface,
    bright = lerp(surface, onSurface, 0.186f),
    containerLowest = lerp(surface, Color.Black, 0.457f),
    containerLow = lerp(surface, onSurface, 0.010f),
    container = lerp(surface, onSurface, 0.027f),
    containerHigh = lerp(surface, onSurface, 0.077f),
    containerHighest = lerp(surface, onSurface, 0.132f),
)

private val Red = Color(0xFFD32F2F)

private val DutyScheduleLightSurfaces = lightSurfaceContainers(Color(0xFFFFFBFF), Color(0xFF201A19))
val DutyScheduleLightColorScheme = lightColorScheme(
    primary = Red,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDAD4),
    onPrimaryContainer = Color(0xFF410000),
    secondary = Color(0xFF775651),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDAD4),
    onSecondaryContainer = Color(0xFF2C1512),
    tertiary = Color(0xFF705C2E),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBDFA6),
    onTertiaryContainer = Color(0xFF251A00),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFF),
    onBackground = Color(0xFF201A19),
    surface = Color(0xFFFFFBFF),
    onSurface = Color(0xFF201A19),
    surfaceVariant = Color(0xFFE9E9E9),
    onSurfaceVariant = Color(0xFF534341),
    outline = Color(0xFF857370),
    surfaceDim = DutyScheduleLightSurfaces.dim,
    surfaceBright = DutyScheduleLightSurfaces.bright,
    surfaceContainerLowest = DutyScheduleLightSurfaces.containerLowest,
    surfaceContainerLow = DutyScheduleLightSurfaces.containerLow,
    surfaceContainer = DutyScheduleLightSurfaces.container,
    surfaceContainerHigh = DutyScheduleLightSurfaces.containerHigh,
    surfaceContainerHighest = DutyScheduleLightSurfaces.containerHighest
)

private val DutyScheduleDarkSurfaces = darkSurfaceContainers(Color(0xFF201A19), Color(0xFFEDE0DE))
val DutyScheduleDarkColorScheme = darkColorScheme(
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
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFD8C2BF),
    outline = Color(0xFFA08C8A),
    surfaceDim = DutyScheduleDarkSurfaces.dim,
    surfaceBright = DutyScheduleDarkSurfaces.bright,
    surfaceContainerLowest = DutyScheduleDarkSurfaces.containerLowest,
    surfaceContainerLow = DutyScheduleDarkSurfaces.containerLow,
    surfaceContainer = DutyScheduleDarkSurfaces.container,
    surfaceContainerHigh = DutyScheduleDarkSurfaces.containerHigh,
    surfaceContainerHighest = DutyScheduleDarkSurfaces.containerHighest
)

private val BlueLightSurfaces = lightSurfaceContainers(Color(0xFFFDFCFF), Color(0xFF1A1C1E))
val BlueLightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF535F70),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD7E3F7),
    onSecondaryContainer = Color(0xFF101C2B),
    tertiary = Color(0xFF6B5778),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF2DAFF),
    onTertiaryContainer = Color(0xFF251431),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF73777F),
    surfaceDim = BlueLightSurfaces.dim,
    surfaceBright = BlueLightSurfaces.bright,
    surfaceContainerLowest = BlueLightSurfaces.containerLowest,
    surfaceContainerLow = BlueLightSurfaces.containerLow,
    surfaceContainer = BlueLightSurfaces.container,
    surfaceContainerHigh = BlueLightSurfaces.containerHigh,
    surfaceContainerHighest = BlueLightSurfaces.containerHighest
)

private val BlueDarkSurfaces = darkSurfaceContainers(Color(0xFF1A1C1E), Color(0xFFE2E2E5))
val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = Color(0xFFD7E3F7),
    tertiary = Color(0xFFD6BEE4),
    onTertiary = Color(0xFF3B2948),
    tertiaryContainer = Color(0xFF523F5F),
    onTertiaryContainer = Color(0xFFF2DAFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C1E),
    onBackground = Color(0xFFE2E2E5),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE2E2E5),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC3C7CF),
    outline = Color(0xFF8D9199),
    surfaceDim = BlueDarkSurfaces.dim,
    surfaceBright = BlueDarkSurfaces.bright,
    surfaceContainerLowest = BlueDarkSurfaces.containerLowest,
    surfaceContainerLow = BlueDarkSurfaces.containerLow,
    surfaceContainer = BlueDarkSurfaces.container,
    surfaceContainerHigh = BlueDarkSurfaces.containerHigh,
    surfaceContainerHighest = BlueDarkSurfaces.containerHighest
)

private val GreenLightSurfaces = lightSurfaceContainers(Color(0xFFFCFDF6), Color(0xFF1A1C18))
val GreenLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E6E32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFAFF5AC),
    onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFF52634F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5E8CF),
    onSecondaryContainer = Color(0xFF101F0F),
    tertiary = Color(0xFF39656B),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFBCEBF2),
    onTertiaryContainer = Color(0xFF001F23),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFCFDF6),
    onBackground = Color(0xFF1A1C18),
    surface = Color(0xFFFCFDF6),
    onSurface = Color(0xFF1A1C18),
    surfaceVariant = Color(0xFFDFE4D8),
    onSurfaceVariant = Color(0xFF43483F),
    outline = Color(0xFF73796E),
    surfaceDim = GreenLightSurfaces.dim,
    surfaceBright = GreenLightSurfaces.bright,
    surfaceContainerLowest = GreenLightSurfaces.containerLowest,
    surfaceContainerLow = GreenLightSurfaces.containerLow,
    surfaceContainer = GreenLightSurfaces.container,
    surfaceContainerHigh = GreenLightSurfaces.containerHigh,
    surfaceContainerHighest = GreenLightSurfaces.containerHighest
)

private val GreenDarkSurfaces = darkSurfaceContainers(Color(0xFF1A1C18), Color(0xFFE2E3DC))
val GreenDarkColorScheme = darkColorScheme(
    primary = Color(0xFF94D690),
    onPrimary = Color(0xFF00390D),
    primaryContainer = Color(0xFF00531A),
    onPrimaryContainer = Color(0xFFAFF5AC),
    secondary = Color(0xFFB9CCB4),
    onSecondary = Color(0xFF243424),
    secondaryContainer = Color(0xFF3A4B38),
    onSecondaryContainer = Color(0xFFD5E8CF),
    tertiary = Color(0xFFA1CED5),
    onTertiary = Color(0xFF00363C),
    tertiaryContainer = Color(0xFF1F4D53),
    onTertiaryContainer = Color(0xFFBCEBF2),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1C18),
    onBackground = Color(0xFFE2E3DC),
    surface = Color(0xFF1A1C18),
    onSurface = Color(0xFFE2E3DC),
    surfaceVariant = Color(0xFF43483F),
    onSurfaceVariant = Color(0xFFC3C8BC),
    outline = Color(0xFF8D9387),
    surfaceDim = GreenDarkSurfaces.dim,
    surfaceBright = GreenDarkSurfaces.bright,
    surfaceContainerLowest = GreenDarkSurfaces.containerLowest,
    surfaceContainerLow = GreenDarkSurfaces.containerLow,
    surfaceContainer = GreenDarkSurfaces.container,
    surfaceContainerHigh = GreenDarkSurfaces.containerHigh,
    surfaceContainerHighest = GreenDarkSurfaces.containerHighest
)

private val PurpleLightSurfaces = lightSurfaceContainers(Color(0xFFFFFBFE), Color(0xFF1C1B1F))
val PurpleLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    surfaceDim = PurpleLightSurfaces.dim,
    surfaceBright = PurpleLightSurfaces.bright,
    surfaceContainerLowest = PurpleLightSurfaces.containerLowest,
    surfaceContainerLow = PurpleLightSurfaces.containerLow,
    surfaceContainer = PurpleLightSurfaces.container,
    surfaceContainerHigh = PurpleLightSurfaces.containerHigh,
    surfaceContainerHighest = PurpleLightSurfaces.containerHighest
)

private val PurpleDarkSurfaces = darkSurfaceContainers(Color(0xFF1C1B1F), Color(0xFFE6E1E5))
val PurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    surfaceDim = PurpleDarkSurfaces.dim,
    surfaceBright = PurpleDarkSurfaces.bright,
    surfaceContainerLowest = PurpleDarkSurfaces.containerLowest,
    surfaceContainerLow = PurpleDarkSurfaces.containerLow,
    surfaceContainer = PurpleDarkSurfaces.container,
    surfaceContainerHigh = PurpleDarkSurfaces.containerHigh,
    surfaceContainerHighest = PurpleDarkSurfaces.containerHighest
)

/** The static (non-dynamic) color scheme for [colorPreset], falling back to [ColorPreset.DEFAULT] for [ColorPreset.DYNAMIC]. */
fun staticColorScheme(darkTheme: Boolean, colorPreset: ColorPreset): ColorScheme = when (colorPreset) {
    ColorPreset.BLUE -> if (darkTheme) BlueDarkColorScheme else BlueLightColorScheme
    ColorPreset.GREEN -> if (darkTheme) GreenDarkColorScheme else GreenLightColorScheme
    ColorPreset.PURPLE -> if (darkTheme) PurpleDarkColorScheme else PurpleLightColorScheme
    ColorPreset.DEFAULT, ColorPreset.DYNAMIC -> if (darkTheme) DutyScheduleDarkColorScheme else DutyScheduleLightColorScheme
}

/** Returns true if the platform supports dynamic (wallpaper-derived) color schemes. */
expect fun isDynamicColorSupported(): Boolean

/**
 * Returns the appropriate [ColorScheme] for the given [darkTheme] flag and [colorPreset].
 * On Android 12+, [ColorPreset.DYNAMIC] uses wallpaper-derived colors; on other platforms
 * or when dynamic color is unsupported, it falls back to [ColorPreset.DEFAULT].
 */
expect fun platformColorScheme(darkTheme: Boolean, colorPreset: ColorPreset): ColorScheme
