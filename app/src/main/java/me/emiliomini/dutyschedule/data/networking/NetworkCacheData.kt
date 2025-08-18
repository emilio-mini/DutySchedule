package me.emiliomini.dutyschedule.data.networking

import okhttp3.Request
import java.time.Instant

data class NetworkCacheData<T>(
    val identifier: String,
    val request: Request,
    val data: T,
    val timestamp: Instant
)