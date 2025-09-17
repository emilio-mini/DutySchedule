package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R
import kotlin.math.round

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> PieChart(
    data: Map<T, Float>,
    modifier: Modifier = Modifier,
    sliceColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0f)
    ),
    chartSize: Dp = 160.dp,
    drawLabels: Boolean = true,
    labelText: (key: T, value: Float, total: Float) -> String = { _, value, total -> "${round(value / total * 100)}%" },
    labelTextResource: ((key: T, value: Float, total: Float) -> Int)? = null
) {
    val labelShapes = listOf(
        MaterialShapes.Circle,
        MaterialShapes.Square,
        MaterialShapes.Diamond,
        MaterialShapes.Gem,
        MaterialShapes.Cookie6Sided,
    )

    if (data.isEmpty()) {
        Column(
            modifier = modifier
                .height(chartSize)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Rounded.PieChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.components_piechart_empty))
        }
        return
    }
    val chartData = data.toSortedMap { k1, k2 -> if (data[k1]!! < data[k2]!!) 1 else -1 }

    val total = chartData.values.sum()
    val legendScrollState = rememberScrollState()

    Row(
        modifier = modifier
            .height(chartSize)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Canvas(modifier = modifier.size(chartSize)) {
            val radius = size.minDimension / 2f

            var startAngle = -90f
            var index = -1
            chartData.forEach { key, value ->
                index++
                val sweepAngle = (value / total) * 360f
                val paint = Paint().apply {
                    color = sliceColors[index % sliceColors.size]
                    style = PaintingStyle.Fill
                }

                drawArc(
                    color = paint.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset.Zero,
                    size = Size(radius * 2, radius * 2)
                )

                startAngle += sweepAngle
            }
        }
        if (drawLabels) {
            Column(modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(legendScrollState)) {
                var index = -1
                for ((key, value) in chartData) {
                    index++

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .offset(y = (-2).dp)
                                .background(
                                    color = sliceColors[index % sliceColors.size],
                                    shape = labelShapes[index % labelShapes.size].toShape()
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = labelShapes[index % labelShapes.size].toShape()
                                )
                        )
                        Text(
                            if (labelTextResource != null) "${value.toInt()}x ${stringResource(
                                labelTextResource(
                                    key,
                                    value,
                                    total
                                )
                            )}" else labelText(key, value, total)
                        )
                    }
                }
            }
        }
    }
}