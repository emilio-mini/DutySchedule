package me.emiliomini.dutyschedule.shared.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.objectivity_black
import dutyschedule.shared.generated.resources.objectivity_black_slanted
import dutyschedule.shared.generated.resources.objectivity_bold
import dutyschedule.shared.generated.resources.objectivity_bold_slanted
import dutyschedule.shared.generated.resources.objectivity_extrabold
import dutyschedule.shared.generated.resources.objectivity_extrabold_slanted
import dutyschedule.shared.generated.resources.objectivity_light
import dutyschedule.shared.generated.resources.objectivity_light_slanted
import dutyschedule.shared.generated.resources.objectivity_medium
import dutyschedule.shared.generated.resources.objectivity_medium_slanted
import dutyschedule.shared.generated.resources.objectivity_regular
import dutyschedule.shared.generated.resources.objectivity_regular_slanted
import dutyschedule.shared.generated.resources.objectivity_thin
import dutyschedule.shared.generated.resources.objectivity_thin_slanted
import org.jetbrains.compose.resources.Font

@Composable
fun objectivityTypography(): Typography {
    val objectivityFont = FontFamily(
        Font(Res.font.objectivity_thin, FontWeight.Thin),
        Font(Res.font.objectivity_thin_slanted, FontWeight.Thin, FontStyle.Italic),
        Font(Res.font.objectivity_light, FontWeight.Light),
        Font(Res.font.objectivity_light_slanted, FontWeight.Light, FontStyle.Italic),
        Font(Res.font.objectivity_regular, FontWeight.Normal),
        Font(Res.font.objectivity_regular_slanted, FontWeight.Normal, FontStyle.Italic),
        Font(Res.font.objectivity_medium, FontWeight.Medium),
        Font(Res.font.objectivity_medium_slanted, FontWeight.Medium, FontStyle.Italic),
        Font(Res.font.objectivity_bold, FontWeight.Bold),
        Font(Res.font.objectivity_bold_slanted, FontWeight.Bold, FontStyle.Italic),
        Font(Res.font.objectivity_extrabold, FontWeight.ExtraBold),
        Font(Res.font.objectivity_extrabold_slanted, FontWeight.ExtraBold, FontStyle.Italic),
        Font(Res.font.objectivity_black, FontWeight.Black),
        Font(Res.font.objectivity_black_slanted, FontWeight.Black, FontStyle.Italic),
    )

    return Typography(
        displayLarge = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ), displayMedium = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Normal,
            fontSize = 42.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ), displaySmall = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ), headlineLarge = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ), headlineMedium = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ), headlineSmall = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ), titleLarge = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ), titleMedium = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ), titleSmall = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        ), bodyLarge = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ), bodyMedium = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        ), bodySmall = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        ), labelLarge = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        ), labelMedium = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        ), labelSmall = TextStyle(
            fontFamily = objectivityFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp
        )
    )
    
}
