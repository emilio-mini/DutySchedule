package me.emiliomini.dutyschedule.shared.ui

import androidx.compose.ui.window.ComposeUIViewController
import me.emiliomini.dutyschedule.shared.ui.main.entry.DutyScheduleApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    return ComposeUIViewController {
        DutyScheduleApp {}
    }
}