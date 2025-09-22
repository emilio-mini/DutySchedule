@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.ui.components

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
import androidx.compose.ui.unit.dp
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.base_date_ordinal
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.emiliomini.dutyschedule.shared.util.format
import me.emiliomini.dutyschedule.shared.util.toOrdinalSuffix
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppDateInfo(modifier: Modifier = Modifier, date: Instant) {
    Row(modifier = modifier.padding(top = 12.dp), verticalAlignment = Alignment.Bottom) {
        Text(
            date.format("EEEE"),
            modifier = Modifier
                .weight(2f)
                .alignByBaseline(),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary
            )
        )
        Text(
            date.format("d"),
            modifier = Modifier.alignByBaseline(),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                drawStyle = Stroke(
                    miter = 10f, width = 5f, join = StrokeJoin.Round
                ),
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(
            modifier = Modifier.paddingFromBaseline(bottom = 10.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            val ordinal =
                date.toLocalDateTime(TimeZone.currentSystemDefault()).day.toOrdinalSuffix()
            Text(
                stringResource(Res.string.base_date_ordinal, ordinal),
                modifier = Modifier.padding(start = 2.dp),
                style = MaterialTheme.typography.labelSmallEmphasized.copy(
                    fontWeight = FontWeight.Black,
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                date.format("MMMM"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.outline
                )
            )
        }
    }
}
