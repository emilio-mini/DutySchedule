package me.emiliomini.dutyschedule.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
    pending: Boolean = false,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 1600,
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
                color = if (animatedProgress > 1) overflowColor else backgroundColor,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            val progressSweep = (animatedProgress - floor(animatedProgress)) * totalSweep

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
        if (pending) {
            LinearProgressIndicator(
                modifier = Modifier
                    .width(sizeDp / 4)
                    .offset(y = sizeDp / 6)
            )
        }
        if (animatedProgress >= 1f) {
            Column(modifier = Modifier.offset(y = sizeDp / 3), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Text("${floor(animatedProgress).toInt()}x")
            }
        }
    }
}
