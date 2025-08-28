package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CardColumn(
    modifier: Modifier = Modifier,
    spacing: Dp = 2.dp,
    content: @Composable (ColumnScope.() -> Unit)
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing), content = content)
    }
}