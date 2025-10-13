package me.emiliomini.dutyschedule.ui.main.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalClipboard
import me.emiliomini.dutyschedule.shared.api.APPLICATION_CONTEXT
import me.emiliomini.dutyschedule.shared.api.LOCAL_CLIPBOARD
import me.emiliomini.dutyschedule.shared.ui.main.entry.DutyScheduleApp

class MainActivity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        APPLICATION_CONTEXT = applicationContext

        setContent {
            DutyScheduleApp {
                LOCAL_CLIPBOARD = LocalClipboard.current
            }
        }
    }
}
