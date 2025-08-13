package me.emiliomini.dutyschedule.ui.onboarding

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
@Preview()
fun AppPermissionScreen(
    skipAction: () -> Unit = {}, continueAction: () -> Unit = {}
) {
    val context = LocalContext.current;
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager?;

    var notificationPermissionCheck by remember { mutableStateOf(false) }
    var alarmPermissionCheck by remember {
        mutableStateOf(
            false
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted: Boolean ->
            notificationPermissionCheck = isGranted
        })

    val lifecycleOwner = LocalLifecycleOwner.current;
    LaunchedEffect(lifecycleOwner.lifecycle.currentStateAsState().value) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            if (alarmManager != null) {
                alarmPermissionCheck = alarmPermissionEnabled(alarmManager);
            } else {
                alarmPermissionCheck = true;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionCheck = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;
            } else {
                notificationPermissionCheck = true;
            }
        }
    }

    AppOnboardingBase(
        headerIcon = Icons.Rounded.Security,
        headerText = "Permissions",
        subheaderText = "Before we get started, we'll need some permissions for this app to function properly",
        actionLeft = {
            TextButton(onClick = skipAction) {
                Text("Skip")
            }
        },
        actionRight = {
            Button(
                onClick = continueAction,
                enabled = notificationPermissionCheck && alarmPermissionCheck
            ) {
                Text("Continue")
            }
        }) {
        ListItem(
            colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ), headlineContent = {
            Text("Send Notifications")
        }, supportingContent = {
            Text("To inform you of upcoming duties and dismiss alarms")
        }, trailingContent = {
            Switch(checked = notificationPermissionCheck, onCheckedChange = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val isPermissionGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!isPermissionGranted) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    notificationPermissionCheck = isPermissionGranted
                }
            }, thumbContent = {
                if (notificationPermissionCheck) {
                    Icon(
                        Icons.Rounded.Check, contentDescription = null,
                        modifier = Modifier.size(
                            SwitchDefaults.IconSize
                        ),
                    );
                } else {
                    null
                }
            })
        })
        ListItem(
            colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ), headlineContent = {
            Text("Create Alarms")
        }, supportingContent = {
            Text("To automatically schedule alarms based on your preferences")
        }, trailingContent = {
            Switch(checked = alarmPermissionCheck, onCheckedChange = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.fromParts("package", context.packageName, null);
                    }
                    context.startActivity(intent);
                }
            }, thumbContent = {
                if (alarmPermissionCheck) {
                    Icon(
                        Icons.Rounded.Check, contentDescription = null,
                        modifier = Modifier.size(
                            SwitchDefaults.IconSize
                        ),
                    );
                } else {
                    null
                }
            })
        })
    }
}

private fun alarmPermissionEnabled(alarmManager: AlarmManager): Boolean {
    return !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms());
}