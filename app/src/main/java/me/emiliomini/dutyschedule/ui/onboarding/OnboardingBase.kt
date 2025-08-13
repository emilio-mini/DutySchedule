package me.emiliomini.dutyschedule.ui.onboarding

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun AppOnboardingBase(
    modifier: Modifier = Modifier,
    headerIcon: ImageVector = Icons.Rounded.BugReport,
    headerText: String = "Header",
    subheaderText: String = "Subheader",
    actionLeft: @Composable (() -> Unit)? = null,
    actionRight: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit) = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotationTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 36000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .size(512.dp)
                    .offset(x = (-128).dp, y = (-64).dp)
                    .graphicsLayer {
                        rotationZ = rotation
                    }
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialShapes.SoftBurst.toShape()
                    ))
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .size(512.dp)
                    .offset(x = 128.dp, y = 512.dp)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialShapes.Pill.toShape()
                    )
            )

            Column(modifier = modifier.padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        headerIcon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        headerText,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Text(
                        subheaderText, style = MaterialTheme.typography.bodyLarge
                    )
                }

                content()

                Spacer(modifier = Modifier.weight(2f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (actionLeft != null) {
                        actionLeft()
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }
                    if (actionRight != null) {
                        actionRight()
                    } else {
                        Spacer(modifier = Modifier.size(1.dp))
                    }
                }
            }
        }
    }
}