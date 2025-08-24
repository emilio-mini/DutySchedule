package me.emiliomini.dutyschedule.util

import android.content.Context
import android.content.Intent
import me.emiliomini.dutyschedule.ui.main.activity.MainActivity

object IntentUtil {
    fun getAppIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    }
}