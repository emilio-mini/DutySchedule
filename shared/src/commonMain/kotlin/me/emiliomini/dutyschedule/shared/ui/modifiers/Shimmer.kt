package me.emiliomini.dutyschedule.shared.ui.modifiers

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

fun Modifier.shimmer(
    isLoading: Boolean = true
): Modifier = composed {
    val shimmerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
    val shimmerColorBg = MaterialTheme.colorScheme.onBackground
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    this.drawWithContent {
        if (!isLoading) {
            drawContent()
        } else {
            drawRect(color = shimmerColorBg)

            val brush = Brush.linearGradient(
                colors = listOf(
                    shimmerColor.copy(alpha = 0.3f),
                    shimmerColor.copy(alpha = 0.7f),
                    shimmerColor.copy(alpha = 0.3f),
                ),
                start = Offset(translateAnim, 0f),
                end = Offset(translateAnim + 500f, 500f)
            )
            drawRect(brush = brush)
        }
    }
}