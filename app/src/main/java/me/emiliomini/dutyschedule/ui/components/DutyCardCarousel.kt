package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyDefinitionProto
import me.emiliomini.dutyschedule.datastore.prep.duty.DutyGroupProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.datastore.prep.employee.SlotProto
import me.emiliomini.dutyschedule.models.prep.ShiftType
import me.emiliomini.dutyschedule.ui.components.icons.Moon
import me.emiliomini.dutyschedule.ui.components.icons.Sunny
import me.emiliomini.dutyschedule.util.getVehicle

@Composable
fun DutyCardCarousel(
    modifier: Modifier = Modifier,
    duties: List<DutyDefinitionProto>,
    groups: Map<String, DutyGroupProto>,
    shiftType: ShiftType,
    onEmployeeClick: (SlotProto) -> Unit = {},
    onDutyClick: (String?, RequirementProto) -> Unit = { _, _ -> },
) {
    if (duties.isEmpty()) {
        return
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { duties.size })
    val listState = rememberLazyListState()

    LaunchedEffect(pagerState.currentPage) {
        listState.animateScrollToItem(pagerState.currentPage)
    }

    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (shiftType) {
                ShiftType.DAY_SHIFT -> Icon(Sunny, contentDescription = null)
                ShiftType.NIGHT_SHIFT -> Icon(Moon, contentDescription = null)
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), state = listState) {
                itemsIndexed(duties, key = { _, duty -> duty.guid }) { index, duty ->
                    var label = duty.getVehicle()?.inlineEmployee?.name?.ifBlank { null } ?: context.getString(R.string.base_carousel_no_vehicle)
                    if (duty.hasInfo()) {
                        label += " | ${duty.info}"
                    }

                    if (duty.hasGroupGuid() && groups.containsKey(duty.groupGuid)) {
                        label += " | ${groups[duty.groupGuid]!!.title}"
                    }

                    AssistChip(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        label = {
                            Text(label)
                        },
                        colors = if (pagerState.currentPage == index) AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary,
                        ) else AssistChipDefaults.assistChipColors(),
                        border = AssistChipDefaults.assistChipBorder(enabled = pagerState.currentPage != index)
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            verticalAlignment = Alignment.Top
        ) { index ->
            AppDutyCard(
                duty = duties[index],
                onEmployeeClick = onEmployeeClick,
                onDutyClick = onDutyClick
            )
        }
    }
}