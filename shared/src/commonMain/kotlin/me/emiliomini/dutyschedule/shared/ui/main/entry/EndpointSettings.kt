package me.emiliomini.dutyschedule.shared.ui.main.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.onboarding_settings_accessibility_close
import dutyschedule.shared.generated.resources.onboarding_settings_action_save
import dutyschedule.shared.generated.resources.onboarding_settings_body
import dutyschedule.shared.generated.resources.onboarding_settings_docsced_url
import dutyschedule.shared.generated.resources.onboarding_settings_prep_url
import dutyschedule.shared.generated.resources.onboarding_settings_title
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.services.network.EndpointService
import me.emiliomini.dutyschedule.shared.ui.icons.Close
import org.jetbrains.compose.resources.stringResource

/**
 * Lets the user point the app at their own PREP and Docsced instances. Neither URL can ship
 * hardcoded, so this is reachable from onboarding before anyone signs in.
 */
@Composable
fun EndpointSettings(onDismiss: () -> Unit) {
    var prepUrl by remember { mutableStateOf(EndpointService.prepUrl) }
    var docscedUrl by remember { mutableStateOf(EndpointService.docscedUrl) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val save = {
        if (!saving) {
            saving = true
            scope.launch {
                try {
                    EndpointService.update(prepUrl, docscedUrl)
                } finally {
                    saving = false
                    onDismiss()
                }
            }
        }
        Unit
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onDismiss
            ) {
                Icon(
                    Close,
                    contentDescription = stringResource(Res.string.onboarding_settings_accessibility_close)
                )
            }

            Text(
                stringResource(Res.string.onboarding_settings_title),
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                stringResource(Res.string.onboarding_settings_body),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = prepUrl,
                onValueChange = { prepUrl = it },
                enabled = !saving,
                label = { Text(stringResource(Res.string.onboarding_settings_prep_url)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next
                ),
                maxLines = 1
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = docscedUrl,
                onValueChange = { docscedUrl = it },
                enabled = !saving,
                label = { Text(stringResource(Res.string.onboarding_settings_docsced_url)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done
                ),
                maxLines = 1
            )

            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = save,
                enabled = !saving
            ) {
                Text(stringResource(Res.string.onboarding_settings_action_save))
            }
        }
    }
}
