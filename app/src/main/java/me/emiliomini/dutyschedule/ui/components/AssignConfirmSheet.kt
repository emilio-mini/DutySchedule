package me.emiliomini.dutyschedule.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text(stringResource(R.string.create_duty_title)) }, // „Dienst eintragen?“
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.create_duty_subtitle), // “Willst du dich für diesen Slot eintragen?”
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!error.isNullOrBlank()) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !loading,
                onClick = onConfirm
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.create_duty_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !loading,
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.create_duty_cancel))
            }
        }
    )
}

@Preview(showBackground = false)
@Composable
fun AssignConfirmSheetPreview() {
    AssignConfirmSheet(
        planGuid = "",
        loading = false,
        error = "",
        onDismiss = {},
        onConfirm = {},
    )
}
