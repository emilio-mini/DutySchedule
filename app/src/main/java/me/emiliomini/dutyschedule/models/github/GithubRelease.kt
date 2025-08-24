package me.emiliomini.dutyschedule.models.github

import java.time.OffsetDateTime

data class GithubRelease(
    val tag: String,
    val isPreRelease: Boolean,
    val publishedAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
    val description: String,
    val downloadUrl: String
)