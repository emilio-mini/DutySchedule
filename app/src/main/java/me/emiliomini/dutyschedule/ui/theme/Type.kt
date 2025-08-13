package me.emiliomini.dutyschedule.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.emiliomini.dutyschedule.R // Import your project's R file

// Define the Objectivity font family by referencing the XML
val ObjectivityFontFamily = FontFamily(
    Font(R.font.objectivity)
)

// Set of Material typography styles using Objectivity
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Normal, // Adjust weight as needed for this style
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp // Kept as is, often intentional for large display
    ),
    displayMedium = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Bold, // Example: Headlines might be bold
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Bold, // Often titles are bolder
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Medium, // Medium weight for subtitles
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp // Reduced from 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp // Reduced from 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp // Reduced from 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp // Reduced from 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp // Reduced from 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp // Reduced from 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp // Reduced from 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = ObjectivityFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp // Reduced from 0.5.sp
    )
)
