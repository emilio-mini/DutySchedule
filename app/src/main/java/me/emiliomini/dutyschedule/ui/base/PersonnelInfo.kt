package me.emiliomini.dutyschedule.ui.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.data.models.Employee

enum class PersonnelInfoState {
    DEFAULT, DISABLED, HIGHLIGHTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPersonnelInfo(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    employee: Employee,
    info: String? = null,
    state: PersonnelInfoState = PersonnelInfoState.DEFAULT,
    showInfoBadge: Boolean = false
) {
    val contentColor = when (state) {
        PersonnelInfoState.HIGHLIGHTED -> MaterialTheme.colorScheme.primary
        PersonnelInfoState.DISABLED -> MaterialTheme.colorScheme.outline
        PersonnelInfoState.DEFAULT -> MaterialTheme.colorScheme.onSurface
    }

    var infoText = ""
    if (info != null) {
        infoText = info
    }
    if (employee.identifier != null) {
        infoText =
            if (infoText.isBlank()) employee.identifier!! else "$infoText | ${employee.identifier}"
    }

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            BadgedBox(badge = { if (showInfoBadge) Badge() }) {
                Icon(
                    imageVector = icon, contentDescription = null, tint = contentColor
                )
            }
        } else {
            BadgedBox(
                badge = {
                    if (showInfoBadge) Badge()
                }
            ) {
                Box(modifier = Modifier.size(width = 24.dp, height = 24.dp))
            }
        }
        Column {
            Text(
                text = infoText,
                color = MaterialTheme.colorScheme.outline,
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                fontWeight = FontWeight.Light
            )
            Text(
                text = employee.name, color = contentColor
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE9E9E9, name = "Default State")
@Composable
fun AppPersonnelInfoPreviewDefault() {
    AppPersonnelInfo(
        icon = Icons.Outlined.Badge,
        employee = Employee("", "Someone Else", "0012345"),
        state = PersonnelInfoState.DEFAULT
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFE9E9E9, name = "Highlighted State")
@Composable
fun AppPersonnelInfoPreviewHighlighted() {
    AppPersonnelInfo(
        icon = Icons.Outlined.Badge,
        employee = Employee("", "Your Name", "0012345"),
        state = PersonnelInfoState.HIGHLIGHTED
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFE9E9E9, name = "Disabled State")
@Composable
fun AppPersonnelInfoPreviewDisabled() {
    AppPersonnelInfo(
        employee = Employee("", "Empty Seat"), state = PersonnelInfoState.DISABLED
    )
}
