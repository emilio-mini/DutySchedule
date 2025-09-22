package me.emiliomini.dutyschedule.ui.main.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.api.APPLICATION_CONTEXT
import me.emiliomini.dutyschedule.shared.api.LOCAL_CLIPBOARD
import me.emiliomini.dutyschedule.shared.services.prep.DutyScheduleService
import me.emiliomini.dutyschedule.shared.services.storage.StorageService
import me.emiliomini.dutyschedule.shared.ui.main.entry.Main
import me.emiliomini.dutyschedule.shared.ui.main.screens.LoadingScreen
import me.emiliomini.dutyschedule.shared.ui.theme.DutyScheduleTheme
import me.emiliomini.dutyschedule.ui.onboarding.activity.OnboardingActivity
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        APPLICATION_CONTEXT = applicationContext

        setContent {
            DutyScheduleTheme {
                Scaffold {
                    LoadingScreen()
                }
            }
        }

        lifecycleScope.launch {
            StorageService.initialize()

            if (!DutyScheduleService.previouslyLoggedIn()) {
                startActivity(Intent(this@MainActivity, OnboardingActivity::class.java))
                finish()
            } else {
                setContent {
                    LaunchedEffect(Unit) {
                        while (true) {
                            delay(TimeUnit.MINUTES.toMillis(5))
                            if (DutyScheduleService.isLoggedIn) {
                                // NetworkService.keepAlive()
                            }
                        }
                    }

                    DutyScheduleTheme {
                        LOCAL_CLIPBOARD = LocalClipboard.current

                        Main(
                            onLogout = {
                                lifecycleScope.launch {
                                    DutyScheduleService.logout()
                                    startActivity(
                                        Intent(
                                            this@MainActivity, OnboardingActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                            },
                            onRestart = {
                                startActivity(Intent(this@MainActivity, MainActivity::class.java))
                                finish()
                            }
                        )
                    }
                }
                DutyScheduleService.restoreLogin()
            }
        }
    }
}

data class NavItem(val label: String, val icon: ImageVector)
