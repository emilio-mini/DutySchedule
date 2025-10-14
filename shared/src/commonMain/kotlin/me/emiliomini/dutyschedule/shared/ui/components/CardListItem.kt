package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CardListItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit = {},
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    type: CardListItemType = CardListItemType.DEFAULT
) {
    ListItem(
        modifier = modifier.background(
            color = containerColor, shape = RoundedCornerShape(
                topStart = if (type == CardListItemType.TOP || type == CardListItemType.SINGLE) 16.dp else 4.dp,
                topEnd = if (type == CardListItemType.TOP || type == CardListItemType.SINGLE) 16.dp else 4.dp,
                bottomStart = if (type == CardListItemType.BOTTOM || type == CardListItemType.SINGLE) 16.dp else 4.dp,
                bottomEnd = if (type == CardListItemType.BOTTOM || type == CardListItemType.SINGLE) 16.dp else 4.dp
            )
        ),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        headlineContent = headlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent
    )
}

enum class CardListItemType {
    TOP, DEFAULT, BOTTOM, SINGLE
}
