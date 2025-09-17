@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package me.emiliomini.dutyschedule.ui.main.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.R
import me.emiliomini.dutyschedule.services.network.NetworkService
import me.emiliomini.dutyschedule.services.notifications.NotificationService
import me.emiliomini.dutyschedule.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.services.storage.DataStores
import me.emiliomini.dutyschedule.ui.main.screens.AlarmsScreen
import me.emiliomini.dutyschedule.ui.main.screens.ArchiveScreen
import me.emiliomini.dutyschedule.ui.main.screens.DashboardScreen
import me.emiliomini.dutyschedule.ui.main.screens.HomeScreen
import me.emiliomini.dutyschedule.ui.main.screens.LoadingScreen
import me.emiliomini.dutyschedule.ui.main.screens.SettingsScreen
import me.emiliomini.dutyschedule.ui.onboarding.activity.OnboardingActivity
import me.emiliomini.dutyschedule.ui.theme.DutyScheduleTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        DataStores.initialize(applicationContext)
        NotificationService.initialize(applicationContext)

        setContent {
            DutyScheduleTheme {
                Scaffold {
                    LoadingScreen()
                }
            }
        }

        lifecycleScope.launch {
            DutyScheduleService.loadSelf(null, null)

            if (!DutyScheduleService.previouslyLoggedIn()) {
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                finish()
            } else {
                setContent {
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(TimeUnit.MINUTES.toMillis(5))
                            if (DutyScheduleService.isLoggedIn) {
                                NetworkService.keepAlive()
                            }
                        }
                    }

                    DutyScheduleTheme {
                        var selectedItemIndex by remember { mutableIntStateOf(0) }
                        val navItems = listOf(
                            NavItem(
                                label = stringResource(R.string.nav_dashboard),
                                icon = Icons.Rounded.Dashboard
                            ),
                            NavItem(
                                label = stringResource(R.string.nav_schedule),
                                icon = Icons.Rounded.Schedule
                            ),
                            NavItem(
                                label = stringResource(R.string.nav_archive),
                                icon = Icons.Rounded.Archive
                            ),
                            NavItem(
                                label = stringResource(R.string.nav_alarms),
                                icon = Icons.Rounded.Alarm
                            ),
                        )

                        when (selectedItemIndex) {
                            0 -> DashboardScreen(
                                bottomBar = {
                                    NavigationBar {
                                        navItems.forEachIndexed { index, item ->
                                            NavigationBarItem(
                                                selected = selectedItemIndex == index,
                                                onClick = { selectedItemIndex = index },
                                                icon = {
                                                    Icon(
                                                        item.icon, contentDescription = item.label
                                                    )
                                                },
                                                label = { Text(item.label) })
                                        }
                                    }
                                }, onLogout = {
                                    lifecycleScope.launch {
                                        DutyScheduleService.logout()
                                        startActivity(
                                            Intent(
                                                this@MainActivity, OnboardingActivity::class.java
                                            )
                                        )
                                        finish()
                                    }
                                })

                            1 -> HomeScreen(
                                bottomBar = {
                                    NavigationBar {
                                        navItems.forEachIndexed { index, item ->
                                            NavigationBarItem(
                                                selected = selectedItemIndex == index,
                                                onClick = { selectedItemIndex = index },
                                                icon = {
                                                    Icon(
                                                        item.icon, contentDescription = item.label
                                                    )
                                                },
                                                label = { Text(item.label) })
                                        }
                                    }
                                })

                            2 -> ArchiveScreen(
                                bottomBar = {
                                    NavigationBar {
                                        navItems.forEachIndexed { index, item ->
                                            NavigationBarItem(
                                                selected = selectedItemIndex == index,
                                                onClick = { selectedItemIndex = index },
                                                icon = {
                                                    Icon(
                                                        item.icon, contentDescription = item.label
                                                    )
                                                },
                                                label = { Text(item.label) })
                                        }
                                    }
                                })

                            3 -> AlarmsScreen(
                                bottomBar = {
                                    NavigationBar {
                                        navItems.forEachIndexed { index, item ->
                                            NavigationBarItem(
                                                selected = selectedItemIndex == index,
                                                onClick = { selectedItemIndex = index },
                                                icon = {
                                                    Icon(
                                                        item.icon, contentDescription = item.label
                                                    )
                                                },
                                                label = { Text(item.label) })
                                        }
                                    }
                                })
                        }
                    }
                }
                DutyScheduleService.restoreLogin()
            }
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)
