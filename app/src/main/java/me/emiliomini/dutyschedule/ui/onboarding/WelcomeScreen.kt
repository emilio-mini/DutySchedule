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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview()
fun AppWelcomeScreen(
    modifier: Modifier = Modifier, aboutAction: () -> Unit = {}, continueAction: () -> Unit = {}
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
                    .offset(x = 128.dp, y = (-64).dp)
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
                    .offset(x = (-128).dp, y = 512.dp)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialShapes.Pill.toShape()
                    )
            )

            Column(
                modifier = modifier.padding(
                    start = 32.dp, end = 32.dp, top = 184.dp, bottom = 32.dp
                )
            ) {
                Text(
                    stringResource(R.string.onboarding_welcome_title),
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    stringResource(R.string.onboarding_welcome_subtitle),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.weight(2f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = aboutAction
                    ) {
                        Text(stringResource(R.string.onboarding_welcome_about))
                    }
                    Button(
                        onClick = continueAction
                    ) {
                        Text(stringResource(R.string.onboarding_welcome_continue))
                    }
                }
            }
        }
    }
}