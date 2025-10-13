package me.emiliomini.dutyschedule.shared.ui.main.entry

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component1
import androidx.compose.ui.focus.FocusRequester.Companion.FocusRequesterFactory.component2
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.mohamedrejeb.calf.permissions.ExperimentalPermissionsApi
import com.mohamedrejeb.calf.permissions.Permission
import com.mohamedrejeb.calf.permissions.isGranted
import com.mohamedrejeb.calf.permissions.rememberPermissionState
import dutyschedule.shared.generated.resources.Res
import dutyschedule.shared.generated.resources.mock_notification_1
import dutyschedule.shared.generated.resources.mock_notification_2
import dutyschedule.shared.generated.resources.mockup
import dutyschedule.shared.generated.resources.onboarding_action_demo
import dutyschedule.shared.generated.resources.onboarding_action_skip
import dutyschedule.shared.generated.resources.onboarding_alarms_body
import dutyschedule.shared.generated.resources.onboarding_alarms_title
import dutyschedule.shared.generated.resources.onboarding_intro_body
import dutyschedule.shared.generated.resources.onboarding_intro_title
import dutyschedule.shared.generated.resources.onboarding_login_body
import dutyschedule.shared.generated.resources.onboarding_login_email
import dutyschedule.shared.generated.resources.onboarding_login_password
import dutyschedule.shared.generated.resources.onboarding_login_title
import dutyschedule.shared.generated.resources.onboarding_notifications_body
import dutyschedule.shared.generated.resources.onboarding_notifications_title
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.api.getPlatformAlarmApi
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.ui.icons.Check
import me.emiliomini.dutyschedule.shared.ui.icons.ChevronRight
import me.emiliomini.dutyschedule.shared.ui.icons.Fingerprint
import me.emiliomini.dutyschedule.shared.ui.icons.Person
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


private data class Page(val content: @Composable () -> Unit)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalPermissionsApi::class)
@Composable
fun Onboarding() {
    val scope = rememberCoroutineScope()
    var blockContinue by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val notificationPermissionCheck = rememberPermissionState(Permission.Notification)
    var alarmPermissionCheck by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle.currentStateAsState().value) {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
            alarmPermissionCheck = getPlatformAlarmApi().isPermissionGranted()
        }
    }

    val pages: List<Page> = listOf(
        Page({
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp)
                    .padding(top = 150.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    stringResource(Res.string.onboarding_intro_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(stringResource(Res.string.onboarding_intro_body))
            }
        }), Page({
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp)
                    .padding(top = 150.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        stringResource(Res.string.onboarding_alarms_title),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        modifier = Modifier.width(64.dp),
                        checked = alarmPermissionCheck,
                        onCheckedChange = {
                            getPlatformAlarmApi().requestPermission()
                        },
                        thumbContent = {
                            if (alarmPermissionCheck) {
                                Icon(
                                    Check, contentDescription = null,
                                    modifier = Modifier.size(
                                        SwitchDefaults.IconSize
                                    ),
                                )
                            } else {
                                null
                            }
                        })
                }
                Text(stringResource(Res.string.onboarding_alarms_body))
            }
        }), Page({
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp)
                    .padding(top = 150.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        stringResource(Res.string.onboarding_notifications_title),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        modifier = Modifier.width(64.dp),
                        checked = notificationPermissionCheck.status.isGranted,
                        onCheckedChange = {
                            notificationPermissionCheck.launchPermissionRequest()
                        },
                        thumbContent = {
                            if (notificationPermissionCheck.status.isGranted) {
                                Icon(
                                    Check, contentDescription = null,
                                    modifier = Modifier.size(
                                        SwitchDefaults.IconSize
                                    ),
                                )
                            } else {
                                null
                            }
                        })
                }
                Text(stringResource(Res.string.onboarding_notifications_body))
            }
        }), Page({
            val (usernameFocusRequester, passwordFocusRequester) = FocusRequester.createRefs()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp)
                    .padding(top = 40.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    stringResource(Res.string.onboarding_login_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(stringResource(Res.string.onboarding_login_body))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(usernameFocusRequester),
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        Text(stringResource(Res.string.onboarding_login_email))
                    },
                    leadingIcon = {
                        Icon(Person, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            passwordFocusRequester.requestFocus()
                        }),
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(Res.string.onboarding_login_password)) },
                    leadingIcon = { Icon(Fingerprint, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        if (blockContinue) {
                            return@KeyboardActions
                        }

                        blockContinue = true
                        scope.launch {
                            val result = DutyScheduleService.login(
                                email, password
                            )

                            blockContinue = false
                        }
                    }),
                    maxLines = 1
                )
            }
        })
    )
    var pageIndex by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "rotationTransition")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )

    val mockupPositionX by animateDpAsState(
        targetValue = if (pageIndex == 0) 190.dp else if (pageIndex == 1) (-240).dp else (-500).dp
    )
    val notification1PosX by animateDpAsState(
        targetValue = if (pageIndex == 2) 230.dp else if (pageIndex < 2) (500).dp else (-500).dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    )
    val notification2PosX by animateDpAsState(
        targetValue = if (pageIndex == 2) 150.dp else if (pageIndex < 2) (500).dp else (-500).dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .size(512.dp)
                        .offset(x = (-128).dp, y = (-64).dp)
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                        .border(
                            width = 8.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            shape = MaterialShapes.Sunny.toShape()
                        ))
                Image(
                    painter = painterResource(Res.drawable.mockup),
                    contentDescription = null,
                    modifier = Modifier
                        .size(1024.dp)
                        .offset(x = mockupPositionX, y = (-150).dp)
                        .rotate(20f)
                )
                Image(
                    painter = painterResource(Res.drawable.mock_notification_2),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .offset(x = notification1PosX, y = 135.dp)
                )
                Image(
                    painter = painterResource(Res.drawable.mock_notification_1),
                    contentDescription = null,
                    modifier = Modifier
                        .size(300.dp)
                        .offset(x = notification2PosX, y = 230.dp)
                )
                pages[pageIndex].content()
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TextButton(
                        onClick = {
                            if (pageIndex == pages.lastIndex) {
                                if (blockContinue) {
                                    return@TextButton
                                }

                                blockContinue = true
                                // DutyScheduleService = DemoService FIXME
                                scope.launch {
                                    val result = DutyScheduleService.login(
                                        email, password
                                    )
                                    if (result) {
                                    }

                                    blockContinue = false
                                }
                            } else {
                                pageIndex = pages.lastIndex
                            }
                        }
                    ) {
                        if (pageIndex == pages.lastIndex) {
                            Text(stringResource(Res.string.onboarding_action_demo))
                        } else {
                            Text(stringResource(Res.string.onboarding_action_skip))
                        }
                    }
                }
                IconButton(
                    modifier = Modifier.size(IconButtonDefaults.largeContainerSize()),
                    onClick = {
                        if (pageIndex < pages.lastIndex) {
                            pageIndex++
                        } else {
                            if (blockContinue) {
                                return@IconButton
                            }

                            blockContinue = true
                            scope.launch {
                                val result = DutyScheduleService.login(
                                    email, password
                                )

                                blockContinue = false
                            }
                        }
                    },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                    shape = if (pageIndex != pages.lastIndex) MaterialShapes.ClamShell.toShape() else if (!blockContinue) MaterialShapes.Square.toShape(
                        90
                    ) else MaterialShapes.Arrow.toShape(),
                    enabled = pageIndex != pages.lastIndex || (email.isNotBlank() && password.isNotBlank() && !blockContinue)
                ) {
                    if (pageIndex == pages.lastIndex && blockContinue) {
                        LoadingIndicator()
                    } else {
                        Icon(
                            if (pageIndex == pages.lastIndex) Check else ChevronRight,
                            modifier = Modifier.size(
                                IconButtonDefaults.extraLargeIconSize
                            ),
                            contentDescription = null
                        )
                    }
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in pages.indices) {
                            val width by animateDpAsState(
                                targetValue = if (i == pageIndex) 24.dp else 8.dp,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                ),
                                label = ""
                            )
                            Box(
                                modifier = Modifier
                                    .height(8.dp)
                                    .width(width)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable(
                                        enabled = pageIndex != i, onClick = {
                                            pageIndex = i
                                        })
                            )
                        }
                    }
                }
            }
        }
    }
}