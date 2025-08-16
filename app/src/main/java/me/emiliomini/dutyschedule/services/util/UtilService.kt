package me.emiliomini.dutyschedule.services.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

object UtilService {

    fun installApk(context: Context, apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val authority = "${context.packageName}.provider"
        val uri = FileProvider.getUriForFile(context, authority, apkFile)

        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}