package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.emiliomini.dutyschedule.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignConfirmSheet(
    planGuid: String?,
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (planGuid == null) return
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = { if (!loading) onDismiss() },
        sheetState = sheetState
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.create_duty_title)) // „Dienst eintragen?“
            Text(
                text = stringResource(R.string.create_duty_subtitle),     // “Willst du dich für diesen Slot eintragen?”
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(enabled = !loading, onClick = onDismiss) {
                    Text(stringResource(R.string.create_duty_cancel))
                }
                Button(enabled = !loading, onClick = onConfirm) {
                    if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text(stringResource(R.string.create_duty_confirm))
                }
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun AssignConfirmSheetPreview() {
    AssignConfirmSheet(
        planGuid = "",
        loading = false,
        error = "",
        onDismiss = { Unit },
        onConfirm = { Unit },
    )
}
