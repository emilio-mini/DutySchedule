package me.emiliomini.dutyschedule.shared.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.shared.datastores.Employee
import me.emiliomini.dutyschedule.shared.datastores.Timestamp
import me.emiliomini.dutyschedule.shared.mappings.Role
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.util.format

enum class PersonnelInfoState {
    DEFAULT, DISABLED, HIGHLIGHTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPersonnelInfo(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    employeeGuid: String,
    fallbackEmployee: Employee? = null,
    customBegin: Timestamp? = null,
    customEnd: Timestamp? = null,
    info: String? = null,
    state: PersonnelInfoState = PersonnelInfoState.DEFAULT,
    showInfoBadge: Boolean = false,
) {
    val employeeItems by StorageService.EMPLOYEES.collectAsState()

    val contentColor = when (state) {
        PersonnelInfoState.HIGHLIGHTED -> MaterialTheme.colorScheme.primary
        PersonnelInfoState.DISABLED -> MaterialTheme.colorScheme.outline
        PersonnelInfoState.DEFAULT -> MaterialTheme.colorScheme.onSurface
    }

    var employee by remember { mutableStateOf<Employee?>(null) }
    var infoText by remember { mutableStateOf("") }

    LaunchedEffect(employeeGuid, fallbackEmployee, employeeItems) {
        employee = employeeItems.employees[employeeGuid] ?: fallbackEmployee

        val infoContents = mutableListOf<String>()
        if (employeeGuid.isNotBlank() || info.isNullOrBlank()) {
            infoContents.add(employee?.identifier ?: "")
        }
        if (customBegin != null || customEnd != null) {
            infoContents.add(customBegin?.format("HH:mm") + " - " + customEnd?.format("HH:mm"))
        }
        infoContents.add(info ?: "")
        infoText = infoContents.filter { it.isNotBlank() }.joinToString(" | ")
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
                text = infoText.ifBlank { "?????" },
                color = MaterialTheme.colorScheme.outline,
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                fontWeight = FontWeight.Light
            )
            val role = Role.of(employeeGuid)
            if (role != Role.NONE) {
                AnimatedGradientText(
                    text = employee?.name ?: "",
                    fontWeight = FontWeight.SemiBold,
                    colors = role.colors()
                )
            } else {
                Text(
                    text = employee?.name ?: "",
                    color = contentColor,
                    fontWeight = if (state == PersonnelInfoState.HIGHLIGHTED) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
