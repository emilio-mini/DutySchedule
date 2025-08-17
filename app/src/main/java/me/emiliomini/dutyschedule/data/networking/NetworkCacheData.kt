package me.emiliomini.dutyschedule.data.networking

import java.time.Instant

data class NetworkCacheData<T>(
    val data: T,
    val timestamp: Instant
)