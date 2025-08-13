package me.emiliomini.dutyschedule.ui.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.util.toOrdinalSuffix
import java.time.OffsetDateTime
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDateInfo(modifier: Modifier = Modifier, date: OffsetDateTime) {
    Row(modifier = modifier.padding(top = 12.dp), verticalAlignment = Alignment.Bottom) {
        Text(
            date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()),
            modifier = Modifier
                .weight(2f)
                .alignByBaseline(),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            date.dayOfMonth.toString(),
            modifier = Modifier.alignByBaseline(),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                drawStyle = Stroke(
                    miter = 10f,
                    width = 5f,
                    join = StrokeJoin.Round
                ),
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(
            modifier = Modifier.paddingFromBaseline(bottom = 10.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                date.dayOfMonth.toOrdinalSuffix().uppercase(),
                modifier = Modifier.padding(start = 2.dp),
                style = MaterialTheme.typography.labelSmallEmphasized.copy(
                    fontWeight = FontWeight.Black,
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppDateInfoPreview() {
    AppDateInfo(date = OffsetDateTime.now())
}