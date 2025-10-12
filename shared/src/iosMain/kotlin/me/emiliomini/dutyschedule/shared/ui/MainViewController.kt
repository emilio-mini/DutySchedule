package me.emiliomini.dutyschedule.shared.ui

import androidx.compose.ui.window.ComposeUIViewController
import me.emiliomini.dutyschedule.shared.ui.main.entry.Main
import me.emiliomini.dutyschedule.shared.ui.theme.DutyScheduleTheme
import platform.UIKit.UIViewController

fun MainViewController(
    onLogout: () -> Unit,
    onRestart: () -> Unit
): UIViewController {
    return ComposeUIViewController {
        DutyScheduleTheme {
            Main(onLogout = onLogout, onRestart = onRestart)
        }
    }
}