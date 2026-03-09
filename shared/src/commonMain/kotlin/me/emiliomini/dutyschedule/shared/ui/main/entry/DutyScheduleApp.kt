package me.emiliomini.dutyschedule.shared.ui.main.entry

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.api.getPlatformTaskSchedulerApi
import me.emiliomini.dutyschedule.shared.api.models.MultiplatformTask
import me.emiliomini.dutyschedule.shared.services.network.NetworkService
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.main.screens.LoadingScreen
import me.emiliomini.dutyschedule.shared.ui.theme.DutyScheduleTheme

@Composable
fun DutyScheduleApp(composableLoadActions: @Composable () -> Unit) {
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf(false) }
    var previouslyLoggedIn by remember { mutableStateOf(false) }
    var themeMode by remember { mutableIntStateOf(0) }
    var dynamicColor by remember { mutableStateOf(true) }

    LaunchedEffect(loaded) {
        if (!loaded) {
            StorageService.initialize()
            previouslyLoggedIn = DutyScheduleService.previouslyLoggedIn()
            val prefs = StorageService.USER_PREFERENCES.getOrDefault()
            themeMode = prefs.themeMode
            dynamicColor = prefs.dynamicColor
            loaded = true
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(300_000L)
            if (DutyScheduleService.isLoggedIn) {
                NetworkService.keepAlive()
            }
        }
    }

    LaunchedEffect(Unit) {
        val prefs = StorageService.USER_PREFERENCES.getOrDefault()
        if (prefs.backgroundUpdaterEnabled) {
            getPlatformTaskSchedulerApi().scheduleTask(MultiplatformTask.UpdateAlarms)
        } else {
            getPlatformTaskSchedulerApi().cancelTask(MultiplatformTask.UpdateAlarms)
        }
    }

    if (!loaded) {
        DutyScheduleTheme {
            composableLoadActions()
            Scaffold {
                LoadingScreen()
            }
        }
    } else {

        if (!DutyScheduleService.isLoggedIn && !previouslyLoggedIn) {
            DutyScheduleTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                Onboarding()
            }
        } else {
            if (!DutyScheduleService.isLoggedIn) {
                scope.launch {
                    DutyScheduleService.restoreLogin()
                }
            }

            DutyScheduleTheme(themeMode = themeMode, dynamicColor = dynamicColor) {
                Main(
                    onThemeModeChange = { newMode -> themeMode = newMode },
                    onDynamicColorChange = { enabled -> dynamicColor = enabled },
                    onLogout = {
                        scope.launch {
                            DutyScheduleService.logout()
                            previouslyLoggedIn = false
                            loaded = false
                        }
                    },
                    onRestart = {
                        loaded = false
                    }
                )
            }
        }

    }
}
