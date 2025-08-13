package me.emiliomini.dutyschedule.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContactEmergency
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.services.api.PrepService

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview()
fun AppLoginScreen(modifier: Modifier = Modifier, successAction: () -> Unit = {}) {
    val scope = rememberCoroutineScope();
    val context = LocalContext.current;
    val (usernameFocusRequester, passwordFocusRequester) = FocusRequester.createRefs();
    var email by remember { mutableStateOf(if (PrepService.DEBUG_MODE) "Developer" else "") }
    var password by remember { mutableStateOf(if (PrepService.DEBUG_MODE) "password" else "") }
    var blockContinue by remember { mutableStateOf(false) }

    AppOnboardingBase(
        headerIcon = Icons.Rounded.ContactEmergency,
        headerText = "Sign in",
        subheaderText = "with your Red Cross Account",
        actionRight = {
            Button(
                enabled = email.isNotBlank() && password.isNotBlank(), onClick = {
                    if (blockContinue) {
                        return@Button;
                    }

                    blockContinue = true;
                    scope.launch {
                        val result = PrepService.login(
                            context, email, password
                        );
                        if (result) {
                            successAction();
                        }

                        blockContinue = false;
                    }
                }) {
                Text("Continue")
            }
        }) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(usernameFocusRequester),
                value = email,
                onValueChange = { email = it },
                label = {
                    Text("Email")
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Person, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        passwordFocusRequester.requestFocus();
                    }
                ),
                maxLines = 1
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Rounded.Fingerprint, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (blockContinue) {
                        return@KeyboardActions;
                    }

                    blockContinue = true;
                    scope.launch {
                        val result = PrepService.login(
                            context, email, password
                        );
                        if (result) {
                            successAction();
                        }

                        blockContinue = false;
                    }
                }),
                maxLines = 1
            )
        }
    }
}