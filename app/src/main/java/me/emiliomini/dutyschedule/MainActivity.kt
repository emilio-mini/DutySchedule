@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package me.emiliomini.dutyschedule

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.services.api.PrepService
import me.emiliomini.dutyschedule.services.notifications.NotificationService
import me.emiliomini.dutyschedule.ui.base.LoadingScreen
import me.emiliomini.dutyschedule.ui.home.HomeScreen
import me.emiliomini.dutyschedule.ui.onboarding.OnboardingActivity
import me.emiliomini.dutyschedule.ui.settings.SettingsScreen
import me.emiliomini.dutyschedule.ui.theme.DutyScheduleTheme
import me.emiliomini.dutyschedule.workers.WorkerService

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationService.initialize(applicationContext)
        WorkerService.scheduleUpdateCheckWorker(applicationContext)

        setContent {
            DutyScheduleTheme {
                Scaffold {
                    LoadingScreen()
                }
            }
        }

        lifecycleScope.launch {
            if (!PrepService.previouslyLoggedIn(applicationContext) || !PrepService.restoreLogin(
                    applicationContext
                )
            ) {
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                finish()
            } else {
                setContent {
                    DutyScheduleTheme {
                        var selectedItemIndex by remember { mutableIntStateOf(0) }
                        val navItems = listOf(
                            NavItem(
                                label = stringResource(R.string.nav_schedule),
                                icon = Icons.Filled.Schedule
                            ), NavItem(
                                label = stringResource(R.string.nav_settings),
                                icon = Icons.Filled.Settings
                            )
                        )

                        when (selectedItemIndex) {
                            0 -> HomeScreen(
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

                            1 -> SettingsScreen(bottomBar = {
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
                                    PrepService.logout(applicationContext)
                                    startActivity(
                                        Intent(
                                            this@MainActivity, OnboardingActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)
