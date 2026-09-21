package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_dashboard_chart_slice_format
import dutyschedule.shared.generated.resources.main_dashboard_chart_total_format
import dutyschedule.shared.generated.resources.main_dashboard_hours
import me.emiliomini.dutyschedule.shared.datastores.Statistics
import me.emiliomini.dutyschedule.shared.datastores.totalMinutes
import me.emiliomini.dutyschedule.shared.util.resourceString
import org.jetbrains.compose.resources.stringResource
import kotlin.math.floor

private const val QUOTA_PAGE = 0
private const val PAGE_COUNT = 2

/**
 * Swiping wraps around, which a pager does by running over a virtual range far longer than anyone
 * will swipe and taking the page modulo the real count. Starting halfway along, on a multiple of
 * [PAGE_COUNT], leaves the quota page first and as much room to swipe back as forward
 */
private const val VIRTUAL_PAGE_COUNT = Int.MAX_VALUE
private val VIRTUAL_START = (VIRTUAL_PAGE_COUNT / 2).let { it - it % PAGE_COUNT }

/** Both pages sit in the same box so swiping between them does not resize the dashboard */
private val PAGE_HEIGHT = 232.dp

/**
 * The year's hours, as the quota ring against the types the user counts and, a swipe away, the
 * full breakdown by type. The breakdown deliberately ignores the quota filter: it is there to show
 * where the time actually went, including the types that do not count towards the target
 */
@Composable
fun HoursSummary(
    modifier: Modifier = Modifier,
    countedMinutes: Int,
    requiredMinutes: Float,
    statistics: Statistics,
    pending: Boolean = false
) {
    val pagerState =
        rememberPagerState(initialPage = VIRTUAL_START, pageCount = { VIRTUAL_PAGE_COUNT })

    val animatedMinutes by animateIntAsState(
        targetValue = countedMinutes, animationSpec = tween(
            durationMillis = 500, easing = FastOutSlowInEasing
        ), label = "QuotaAnimation"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(state = pagerState, verticalAlignment = Alignment.CenterVertically) { page ->
            when (page % PAGE_COUNT) {
                QUOTA_PAGE -> ArcProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    sizeDp = PAGE_HEIGHT,
                    progress = countedMinutes / requiredMinutes,
                    strokeWidth = 24.dp,
                    pending = pending
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${floor((animatedMinutes / 60.0) * 100) / 100}",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                " / ${floor(requiredMinutes / 60).toInt()}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(stringResource(Res.string.main_dashboard_hours))
                    }
                }

                else -> DutyTypeBreakdown(statistics)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(PAGE_COUNT) { page ->
                Box(
                    Modifier.size(8.dp).background(
                        color = if (pagerState.currentPage % PAGE_COUNT == page) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }, shape = CircleShape
                    )
                )
            }
        }
    }
}

@Composable
private fun DutyTypeBreakdown(statistics: Statistics) {
    // The slice labels carry their own hours, so they are resolved up front and used as the keys
    // the chart draws; its own label formatting counts occurrences, which these are not
    val slices = mutableMapOf<String, Float>()
    for ((type, minutes) in statistics.minutesByDutyType) {
        if (minutes <= 0) {
            continue
        }

        val label = stringResource(
            Res.string.main_dashboard_chart_slice_format,
            stringResource(type.resourceString()),
            floor((minutes / 60.0) * 100) / 100
        )
        slices[label] = minutes / 60f
    }

    Column(
        modifier = Modifier.height(PAGE_HEIGHT).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        PieChart(data = slices, chartSize = 160.dp, labelText = { key, _, _ -> key })
        Text(
            stringResource(
                Res.string.main_dashboard_chart_total_format,
                floor((statistics.totalMinutes() / 60.0) * 100) / 100
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
