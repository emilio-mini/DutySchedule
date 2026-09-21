@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.main_dashboard_chart_slice_format
import dutyschedule.shared.generated.resources.main_dashboard_chart_total_format
import dutyschedule.shared.generated.resources.main_dashboard_hours
import kotlinx.coroutines.delay
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.datastores.Statistics
import me.emiliomini.dutyschedule.shared.datastores.totalMinutes
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.resourceString
import me.emiliomini.dutyschedule.shared.util.toEpochMilliseconds
import me.emiliomini.dutyschedule.shared.util.toInstant
import org.jetbrains.compose.resources.stringResource
import kotlin.math.floor
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

/** In swipe order. [NEXT_DUTY] drops out entirely when there is nothing coming up */
private enum class SummaryPage {
    NEXT_DUTY, QUOTA, BREAKDOWN
}

/**
 * Swiping wraps around, which a pager does by running over a virtual range far longer than anyone
 * will swipe and taking the page modulo the real count. Starting halfway along, on a multiple of
 * that count, leaves as much room to swipe back as forward
 */
private const val VIRTUAL_PAGE_COUNT = Int.MAX_VALUE

/**
 * Every page sits in the same box so swiping between them does not resize the dashboard. The box
 * is taller than the ring it grew out of, taking the space that used to sit between the carousel
 * and the upcoming list, so what is below stays where it was and only the duty page gets the room
 */
private val PAGE_HEIGHT = 264.dp

/** The charts keep their own sizes rather than filling the taller page; they are centred in it */
private val QUOTA_SIZE = 232.dp
private val PIE_SIZE = 160.dp

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
    upcomingDuties: List<MinimalDutyDefinition> = emptyList(),
    pending: Boolean = false
) {
    // Ticks only while there is a countdown to show; the other pages hold nothing live. The clock
    // also decides which duty is next, so one running out rolls onto the following one by itself
    // instead of sitting at zero until the upcoming list happens to be fetched again
    var now by remember { mutableStateOf(Clock.System.now()) }
    LaunchedEffect(upcomingDuties) {
        while (true) {
            now = Clock.System.now()
            if (upcomingDuties.none { it.end.toInstant() > now }) {
                break
            }
            delay(1.seconds)
        }
    }

    val nextDuty = upcomingDuties
        .filter { it.end.toInstant() > now }
        .minByOrNull { it.begin.toEpochMilliseconds() }

    val pages = remember(nextDuty == null) {
        SummaryPage.entries.filter { it != SummaryPage.NEXT_DUTY || nextDuty != null }
    }

    // A duty already under way is the thing the user opened the app for, so start there
    val ongoing = nextDuty != null && now >= nextDuty.begin.toInstant()
    val pagerState = key(pages.size) {
        val start = (VIRTUAL_PAGE_COUNT / 2).let { it - it % pages.size }
        val landing = if (ongoing) SummaryPage.NEXT_DUTY else SummaryPage.QUOTA

        rememberPagerState(
            initialPage = start + pages.indexOf(landing).coerceAtLeast(0),
            pageCount = { VIRTUAL_PAGE_COUNT })
    }

    var detailViewEmployee by remember { mutableStateOf<Slot?>(null) }
    val orgItems by StorageService.ORG_ITEMS.collectAsState()

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
        // The height belongs to the pager, not to the pages: left to the pages it would follow
        // whichever one is showing and the carousel would resize as you swipe through them
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.height(PAGE_HEIGHT),
            verticalAlignment = Alignment.CenterVertically
        ) { page ->
            when (pages[page % pages.size]) {
                SummaryPage.NEXT_DUTY -> if (nextDuty != null) {
                    NextDutyPage(
                        modifier = Modifier.fillMaxHeight(),
                        duty = nextDuty,
                        now = now,
                        onEmployeeClick = { detailViewEmployee = it })
                }

                SummaryPage.QUOTA -> ArcProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    sizeDp = QUOTA_SIZE,
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

                SummaryPage.BREAKDOWN -> DutyTypeBreakdown(statistics)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(pages.size) { page ->
                Box(
                    Modifier.size(8.dp).background(
                        color = if (pagerState.currentPage % pages.size == page) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        }, shape = CircleShape
                    )
                )
            }
        }
    }

    if (detailViewEmployee != null) {
        EmployeeDetailSheet(
            slot = detailViewEmployee,
            orgs = orgItems.orgs.values.toList(),
            onDismiss = { detailViewEmployee = null })
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
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        PieChart(data = slices, chartSize = PIE_SIZE, labelText = { key, _, _ -> key })
        Text(
            stringResource(
                Res.string.main_dashboard_chart_total_format,
                floor((statistics.totalMinutes() / 60.0) * 100) / 100
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}
