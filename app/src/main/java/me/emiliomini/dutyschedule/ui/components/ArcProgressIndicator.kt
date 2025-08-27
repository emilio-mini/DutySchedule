package me.emiliomini.dutyschedule.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.floor

@Composable
fun ArcProgressIndicator(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 64.dp,
    progress: Float,
    totalSweep: Float = 270f,
    startAngle: Float = 135f,
    strokeWidth: Dp = 12.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    overflowColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
    progressBorderColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable BoxScope.() -> Unit = {}
) {
    var safeProgress = progress - floor(progress)
    if (progress > 0 && safeProgress == 0f) {
        safeProgress = 1f
    }

    val animatedProgress by animateFloatAsState(
        targetValue = safeProgress,
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "ArcProgressAnimation"
    )

    Box(modifier = modifier.size(sizeDp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = (size.minDimension - strokeWidth.toPx()) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Track
            drawArc(
                color = if (progress > 1) overflowColor else backgroundColor,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            val progressSweep = animatedProgress * totalSweep

            // Progress border
            if (progress > 0) {
                drawArc(
                    color = progressBorderColor,
                    startAngle = startAngle,
                    sweepAngle = progressSweep + 1,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = (strokeWidth + 8.dp).toPx(), cap = StrokeCap.Round)
                )
            }

            // Progress
            drawArc(
                color = progressColor,
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        content()
    }
}
