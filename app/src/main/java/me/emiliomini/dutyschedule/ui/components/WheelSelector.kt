package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun WheelSelector(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedColor: Color,
    selectedIndex: Int = 0,
    onValueChange: (Int) -> Unit = {},
    textStyle: TextStyle = LocalTextStyle.current,
    alignment: Alignment.Horizontal = Alignment.Start,
    textAlign: TextAlign = TextAlign.Start,
    monospaced: Boolean = false,
    monospacedLetterWidth: Dp = 12.dp
) {
    val pagerState =
        rememberPagerState(initialPage = items.size + selectedIndex, pageCount = { items.size * 3 })

    LaunchedEffect(pagerState.currentPage) {
        onValueChange(pagerState.currentPage % items.size)
    }

    LaunchedEffect(pagerState.settledPage) {
        pagerState.scrollToPage(items.size + (pagerState.currentPage % items.size))
    }

    VerticalPager(
        pagerState,
        modifier = modifier.height(textStyle.fontSize.value.dp * 3),
        pageSize = PageSize.Fixed(textStyle.fontSize.value.dp),
        beyondViewportPageCount = 2,
        horizontalAlignment = alignment,
        snapPosition = SnapPosition.Center
    ) {
        if (monospaced) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (it != pagerState.currentPage) 0.25f else 1f)
            ) {
                for (char in items[it % items.size]) {
                    Text(
                        "$char",
                        modifier = Modifier.width(monospacedLetterWidth),
                        textAlign = TextAlign.Center,
                        style = textStyle,
                        color = if (it == pagerState.currentPage) selectedColor else Color.Unspecified
                    )
                }
            }
        } else {
            Text(
                items[it % items.size],
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (it != pagerState.currentPage) 0.25f else 1f),
                textAlign = textAlign,
                style = textStyle,
                color = if (it == pagerState.currentPage) selectedColor else Color.Unspecified
            )
        }
    }
}
