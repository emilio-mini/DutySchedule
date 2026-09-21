@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.base_dutycard_no_staff
import dutyschedule.shared.generated.resources.main_dashboard_nextduty_countdown_days
import dutyschedule.shared.generated.resources.main_dashboard_nextduty_ends_in
import dutyschedule.shared.generated.resources.main_dashboard_nextduty_from_garage
import dutyschedule.shared.generated.resources.main_dashboard_nextduty_handoff
import dutyschedule.shared.generated.resources.main_dashboard_nextduty_starts_in
import dutyschedule.shared.generated.resources.main_dashboard_nextduty_takeover
import dutyschedule.shared.generated.resources.main_dashboard_nextduty_to_garage
import me.emiliomini.dutyschedule.shared.api.getPlatformConnectivityApi
import me.emiliomini.dutyschedule.shared.datastores.DutyContext
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.MinimalDutyDefinition
import me.emiliomini.dutyschedule.shared.datastores.Slot
import me.emiliomini.dutyschedule.shared.mappings.RequirementMapping
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.icons.Garage
import me.emiliomini.dutyschedule.shared.ui.icons.Moon
import me.emiliomini.dutyschedule.shared.ui.icons.Sunny
import me.emiliomini.dutyschedule.shared.ui.icons.TruckSpeed
import me.emiliomini.dutyschedule.shared.ui.modifiers.shimmer
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.formatCountdown
import me.emiliomini.dutyschedule.shared.util.getIcon
import me.emiliomini.dutyschedule.shared.util.isNightShift
import me.emiliomini.dutyschedule.shared.util.isNotNullOrBlank
import me.emiliomini.dutyschedule.shared.util.resourceString
import me.emiliomini.dutyschedule.shared.util.toInstant
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private const val TIME_FORMAT = "H:mm"

/** Keeps the ticking digits from shifting about as they change width */
private const val TABULAR_NUMBERS = "tnum"

private val HANDOVER_CARD_HEIGHT = 32.dp
private val CARD_SHAPE = RoundedCornerShape(8.dp)
private val TRUCK_SIZE = 20.dp
private val SHIFT_ICON_SIZE = 16.dp
private val SWIPE_THRESHOLD = 48.dp

/** How long either end of a duty counts as arriving or handing over rather than being on it */
private val HANDOVER_WINDOW = 1.hours

/** Break in the track either side of the truck, so the line does not run through it */
private val TRACK_GAP = 4.dp

/** Enough of the neighbouring cards is left showing to say they are there and can be reached */
private const val COLLAPSED_WEIGHT = 0.04f

/** In the order the duty runs through them, which is also the order they sit in on screen */
private enum class DutyStage {
    BEFORE, DURING, AFTER
}

/**
 * The duty coming up: a countdown, who is on it, and where the vehicle comes from and goes to. The
 * crew and the vehicle come from the plan entry behind the duty, which has to be looked up, so the
 * countdown shows straight away and the rest fills in when it arrives
 */
@Composable
fun NextDutyPage(
    modifier: Modifier = Modifier,
    duty: MinimalDutyDefinition,
    now: Instant,
    onEmployeeClick: (Slot) -> Unit
) {
    // Taken from the store rather than read once into local state: the store fills in from disk
    // while the app is starting, and a single read on the way past would miss it and never look
    // again. Observing it also means the last known roster is on screen offline, and that a
    // refresh lands by itself once one completes
    val storedContexts by StorageService.DUTY_CONTEXTS.collectAsState()
    val context = storedContexts.contexts[duty.guid]
    val orgItems by StorageService.ORG_ITEMS.collectAsState()

    var unresolved by remember(duty.guid) { mutableStateOf(false) }

    // Keyed on the session too: a launch composes this before the login is restored, and a lookup
    // made then can only fail. Without a session there is nothing to ask, so the placeholder stays
    // until either one arrives or the store hands over something already known
    LaunchedEffect(duty.guid, DutyScheduleService.isLoggedIn) {
        if (!DutyScheduleService.isLoggedIn) {
            return@LaunchedEffect
        }

        unresolved = DutyScheduleService.loadDutyContext(duty) == null
    }

    // A placeholder is a promise that something is coming, so it only holds while something can.
    // Off the network there is nothing to wait for: whatever was last stored is all there is, and
    // an empty card is the honest answer when there is not even that
    val connected by getPlatformConnectivityApi().isConnected.collectAsState()
    val loading = context == null && connected && !unresolved

    val begin = duty.begin.toInstant()
    val end = duty.end.toInstant()
    val ongoing = now >= begin

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    stringResource(
                        if (ongoing) {
                            Res.string.main_dashboard_nextduty_ends_in
                        } else {
                            Res.string.main_dashboard_nextduty_starts_in
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    (if (ongoing) end - now else begin - now).formatCountdown(
                        stringResource(Res.string.main_dashboard_nextduty_countdown_days)
                    ),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFeatureSettings = TABULAR_NUMBERS
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(duty.type.resourceString()),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    if (duty.isNightShift()) Moon else Sunny,
                    contentDescription = null,
                    modifier = Modifier.size(SHIFT_ICON_SIZE)
                )
            }
        }

        Crew(
            modifier = Modifier.weight(1f),
            context = context,
            loading = loading,
            onEmployeeClick = onEmployeeClick
        )

        val vehicle = context?.vehicle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                orgItems.orgs.values.firstOrNull { it.guid == context?.orgGuid }?.title.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (vehicle != null) {
                Text(
                    vehicle.inlineEmployee?.name.orEmpty(),
                    modifier = Modifier.clickable { onEmployeeClick(vehicle) },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Where the vehicle comes from and goes to is only a thing when there is a vehicle. Until
        // the lookup lands nobody knows either way, so the strip shimmers rather than guessing
        if (loading || vehicle != null) {
            HandoverStrip(duty = duty, context = context, now = now, loading = loading)
        }
    }
}

@Composable
private fun Crew(
    modifier: Modifier = Modifier,
    context: DutyContext?,
    loading: Boolean,
    onEmployeeClick: (Slot) -> Unit
) {
    val crew = context?.duty?.slots.orEmpty()
        .filter { !RequirementMapping.VEHICLES.contains(it.requirement.guid) }

    // Unfilled slots are worth showing -- they are how you see the duty is short -- but they have
    // no employee to name, so they borrow the duty card's placeholder
    val emptySeat = Employee(name = stringResource(Res.string.base_dutycard_no_staff))

    Card(
        modifier = modifier.fillMaxWidth().clip(CARD_SHAPE).shimmer(isLoading = loading),
        shape = CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (slot in crew) {
                val filled = slot.employeeGuid.isNotNullOrBlank()

                AppPersonnelInfo(
                    modifier = Modifier.clickable { onEmployeeClick(slot) },
                    icon = slot.requirement.getIcon(),
                    employeeGuid = if (filled) slot.employeeGuid.orEmpty() else emptySeat.guid,
                    fallbackEmployee = if (filled) {
                        slot.inlineEmployee
                    } else {
                        emptySeat.copy(
                            identifier = stringResource(slot.requirement.resourceString())
                        )
                    },
                    info = slot.info,
                    showInfoBadge = slot.info.isNotNullOrBlank(),
                    state = if (slot.employeeGuid == DutyScheduleService.self?.guid) {
                        PersonnelInfoState.HIGHLIGHTED
                    } else if (filled) {
                        PersonnelInfoState.DEFAULT
                    } else {
                        PersonnelInfoState.DISABLED
                    }
                )
            }
        }
    }
}

/**
 * The three stages of the vehicle's day laid side by side: where it comes from, how far through the
 * shift it is, and where it goes afterwards. One is open and the other two are collapsed to a sliver
 * that can be swiped or tapped back open. Which one opens follows the duty until the user picks
 */
@Composable
private fun HandoverStrip(
    duty: MinimalDutyDefinition,
    context: DutyContext?,
    now: Instant,
    loading: Boolean
) {
    val begin = duty.begin.toInstant()
    val end = duty.end.toInstant()

    // Getting there and handing over are the ends of the shift worth showing, so the taking over
    // card stays up an hour past the start and the handing over one comes up an hour before the
    // end. A duty too short for that gets thirds instead, so the middle never vanishes
    val handover = minOf(HANDOVER_WINDOW, (end - begin) / 3)
    val stage = when {
        now < begin + handover -> DutyStage.BEFORE
        now < end - handover -> DutyStage.DURING
        else -> DutyStage.AFTER
    }
    var selected by remember(duty.guid) { mutableIntStateOf(stage.ordinal) }
    LaunchedEffect(stage) {
        selected = stage.ordinal
    }

    var drag by remember { mutableStateOf(0f) }

    Row(
        modifier = Modifier.fillMaxWidth().pointerInput(duty.guid) {
            val threshold = SWIPE_THRESHOLD.toPx()

            detectHorizontalDragGestures(onDragEnd = {
                if (drag <= -threshold) {
                    selected = (selected + 1).coerceAtMost(DutyStage.entries.lastIndex)
                } else if (drag >= threshold) {
                    selected = (selected - 1).coerceAtLeast(0)
                }
                drag = 0f
            }, onDragCancel = { drag = 0f }) { _, amount -> drag += amount }
        },
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DutyStage.entries.forEachIndexed { index, entry ->
            val expanded = index == selected
            val weight by animateFloatAsState(
                targetValue = if (expanded) 1f else COLLAPSED_WEIGHT, label = "HandoverWeight"
            )

            HandoverCard(
                weight = weight,
                expanded = expanded && !loading,
                loading = loading,
                onClick = { selected = index }) {
                when (entry) {
                    DutyStage.BEFORE -> HandoverLabel(
                        icon = if (context?.handoverFrom != null) TruckSpeed else Garage,
                        text = stringResource(
                            if (context?.handoverFrom != null) {
                                Res.string.main_dashboard_nextduty_takeover
                            } else {
                                Res.string.main_dashboard_nextduty_from_garage
                            }, duty.begin.format(TIME_FORMAT)
                        )
                    )

                    DutyStage.DURING -> DutyProgress(
                        beginLabel = duty.begin.format(TIME_FORMAT),
                        endLabel = duty.end.format(TIME_FORMAT),
                        fraction = progressBetween(begin, end, now)
                    )

                    DutyStage.AFTER -> HandoverLabel(
                        icon = if (context?.handoverTo != null) TruckSpeed else Garage,
                        text = stringResource(
                            if (context?.handoverTo != null) {
                                Res.string.main_dashboard_nextduty_handoff
                            } else {
                                Res.string.main_dashboard_nextduty_to_garage
                            }, duty.end.format(TIME_FORMAT)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.HandoverCard(
    weight: Float,
    expanded: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.weight(weight).height(HANDOVER_CARD_HEIGHT).clip(CARD_SHAPE)
            .shimmer(isLoading = loading).clickable { onClick() },
        shape = CARD_SHAPE,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        if (expanded) {
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                content()
            }
        }
    }
}

@Composable
private fun HandoverLabel(icon: ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(TRUCK_SIZE))
        Text(text, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun DutyProgress(beginLabel: String, endLabel: String, fraction: Float) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(beginLabel, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        BoxWithConstraints(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            // Drawn as the two stretches either side of the truck rather than one line behind it,
            // so the gaps in the icon stay empty instead of showing the track through them
            val truckStart = (this.maxWidth - TRUCK_SIZE) * fraction
            val truckEnd = truckStart + TRUCK_SIZE

            Track(offset = 0.dp, width = truckStart - TRACK_GAP)
            Track(offset = truckEnd + TRACK_GAP, width = this.maxWidth - truckEnd - TRACK_GAP)

            Icon(
                TruckSpeed,
                contentDescription = null,
                modifier = Modifier.offset(x = truckStart).size(TRUCK_SIZE),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(endLabel, style = MaterialTheme.typography.labelMedium, maxLines = 1)
    }
}

@Composable
private fun Track(offset: Dp, width: Dp) {
    Box(
        modifier = Modifier.offset(x = offset).width(width.coerceAtLeast(0.dp)).height(2.dp)
            .background(
                color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(1.dp)
            )
    )
}

private fun progressBetween(begin: Instant, end: Instant, now: Instant): Float {
    val total = (end - begin).inWholeSeconds
    if (total <= 0) {
        return 0f
    }

    return ((now - begin).inWholeSeconds.toFloat() / total).coerceIn(0f, 1f)
}
