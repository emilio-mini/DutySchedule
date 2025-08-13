package me.emiliomini.dutyschedule.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import me.emiliomini.dutyschedule.MainActivity
import me.emiliomini.dutyschedule.ui.theme.DutyScheduleTheme


class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DutyScheduleTheme {
                OnboardingScreen(
                    successAction = {
                        val intent = Intent(this@OnboardingActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    })
            }
        }
    }
}

@Composable
fun OnboardingScreen(successAction: () -> Unit = {}) {
    var step by remember { mutableIntStateOf(0) }

    BackHandler(enabled = step > 0) {
        step--
    }

    when (step) {
        0 -> AppWelcomeScreen(aboutAction = {
            // TODO: Show about screen
        }, continueAction = { step++ })

        1 -> AppPermissionScreen(skipAction = {
            // TODO: Warn user about skipping
            step++
        }, continueAction = { step++ })

        2 -> AppLoginScreen(
            successAction = {
                successAction()
            })
    }
}
