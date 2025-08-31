package me.emiliomini.dutyschedule.ui.components

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.protobuf.Timestamp
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeItemsProto
import me.emiliomini.dutyschedule.datastore.prep.employee.EmployeeProto
import me.emiliomini.dutyschedule.datastore.prep.employee.RequirementProto
import me.emiliomini.dutyschedule.models.prep.Employee
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.services.storage.ProtoMapViewModel
import me.emiliomini.dutyschedule.services.storage.ProtoMapViewModelFactory
import me.emiliomini.dutyschedule.services.storage.ViewModelKeys
import me.emiliomini.dutyschedule.util.format

enum class PersonnelInfoState {
    DEFAULT, DISABLED, HIGHLIGHTED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPersonnelInfo(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    employeeGuid: String,
    fallbackEmployee: EmployeeProto? = null,
    customBegin: Timestamp? = null,
    customEnd: Timestamp? = null,
    info: String? = null,
    state: PersonnelInfoState = PersonnelInfoState.DEFAULT,
    showInfoBadge: Boolean = false,
    viewModel: ProtoMapViewModel<EmployeeItemsProto, EmployeeProto> = viewModel(
        key = ViewModelKeys.EMPLOYEES,
        factory = ProtoMapViewModelFactory<EmployeeItemsProto, EmployeeProto>(DataStores.EMPLOYEES) { it.employeesMap }
    )
) {
    val employees by viewModel.flow.collectAsStateWithLifecycle(
        initialValue = emptyMap<String, EmployeeProto>()
    )

    val contentColor = when (state) {
        PersonnelInfoState.HIGHLIGHTED -> MaterialTheme.colorScheme.primary
        PersonnelInfoState.DISABLED -> MaterialTheme.colorScheme.outline
        PersonnelInfoState.DEFAULT -> MaterialTheme.colorScheme.onSurface
    }

    var employee by remember { mutableStateOf<EmployeeProto?>(null) }
    var infoText by remember { mutableStateOf("") }

    LaunchedEffect(employeeGuid, fallbackEmployee, employees) {
        employee = employees[employeeGuid] ?: fallbackEmployee

        var infoContents = mutableListOf<String>()
        infoContents.add(employee?.identifier ?: "")
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
                text = infoText,
                color = MaterialTheme.colorScheme.outline,
                fontSize = MaterialTheme.typography.labelSmall.fontSize,
                fontWeight = FontWeight.Light
            )
            Text(
                text = employee?.name ?: "", color = contentColor,
                fontWeight = if (state == PersonnelInfoState.HIGHLIGHTED) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
