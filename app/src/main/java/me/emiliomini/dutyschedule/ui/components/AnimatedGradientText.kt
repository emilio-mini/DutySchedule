package me.emiliomini.dutyschedule.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AnimatedGradientText(
    text: String,
    modifier: Modifier = Modifier,
    colors: List<Color>,
    textStyle: TextStyle = LocalTextStyle.current,
    fontWeight: FontWeight? = null,
    animationDurationMillis: Int = 4_000
) {
    require(colors.size > 1) { "Must provide at least 2 colors." }

    val infiniteTransition = rememberInfiniteTransition(label = "gradientAnimation")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientProgress"
    )

    var style = textStyle
    if (fontWeight != null) {
        style = style.copy(fontWeight = fontWeight)
    }

    BasicText(
        text = text,
        style = style,
        modifier = modifier
            .graphicsLayer(alpha = 0.99f)
            .drawWithCache {
                val brushWidth = size.width
                val translationX = brushWidth * progress

                val brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(x = translationX, y = 0f),
                    end = Offset(x = translationX + brushWidth, y = 0f),
                    tileMode = TileMode.Repeated
                )

                onDrawWithContent {
                    drawContent()
                    drawRect(
                        brush = brush,
                        blendMode = BlendMode.SrcIn
                    )
                }
            }
    )
}