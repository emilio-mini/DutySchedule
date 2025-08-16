package me.emiliomini.dutyschedule.data.models.vc

import java.time.OffsetDateTime

data class GithubRelease(
    val tag: String,
    val isPreRelease: Boolean,
    val publishedAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val description: String,
    val downloadUrl: String
)
