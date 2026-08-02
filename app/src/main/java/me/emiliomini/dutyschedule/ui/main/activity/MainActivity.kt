package me.emiliomini.dutyschedule.ui.main.activity

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalClipboard
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.emiliomini.dutyschedule.shared.api.APPLICATION_CONTEXT
import me.emiliomini.dutyschedule.shared.api.LOCAL_CLIPBOARD
import me.emiliomini.dutyschedule.shared.ui.main.entry.AppReadyState
import me.emiliomini.dutyschedule.shared.ui.main.entry.DutyScheduleApp

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        APPLICATION_CONTEXT = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
        setContent {
            DutyScheduleApp {
                LOCAL_CLIPBOARD = LocalClipboard.current
            }
        }
        holdSplashUntilReady()
    }

    /**
     * Keeps the first frame back until the app has finished loading, so the looping splash icon
     * animation covers the startup work instead of being cut off by the first drawn frame
     */
    private fun holdSplashUntilReady() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        val content = findViewById<View>(android.R.id.content)
        var timedOut = false

        content.postDelayed({
            timedOut = true
            content.invalidate()
        }, SPLASH_MAX_HOLD_MS)

        lifecycleScope.launch {
            AppReadyState.isReady.first { it }
            content.invalidate()
        }

        content.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (!timedOut && !AppReadyState.isReady.value) {
                    return false
                }

                content.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    companion object {
        const val SPLASH_MAX_HOLD_MS = 5000L
    }
}
