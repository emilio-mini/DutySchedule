package me.emiliomini.dutyschedule.shared.services.network

import me.emiliomini.dutyschedule.shared.datastores.EndpointConfig
import me.emiliomini.dutyschedule.shared.services.storage.StorageService

/**
 * The service URLs in use right now. Read straight off the store's current value so they can be
 * changed at runtime without any of the call sites having to suspend.
 */
object EndpointService {
    private const val SCHEME_SEPARATOR = "://"
    private const val DEFAULT_SCHEME = "https$SCHEME_SEPARATOR"
    private const val PATH_DELIMITERS = "/?#"

    private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*$SCHEME_SEPARATOR")

    val prepUrl: String
        get() = normalize(stored().prepUrl, EndpointConfig.DEFAULT_PREP_URL)

    val docscedUrl: String
        get() = normalize(stored().docscedUrl, EndpointConfig.DEFAULT_DOCSCED_URL)

    suspend fun update(prepUrl: String, docscedUrl: String) {
        val previousPrepUrl = this.prepUrl
        val cleanedPrepUrl = normalize(prepUrl, EndpointConfig.DEFAULT_PREP_URL)

        StorageService.ENDPOINTS.update {
            it.copy(
                prepUrl = cleanedPrepUrl,
                docscedUrl = normalize(docscedUrl, EndpointConfig.DEFAULT_DOCSCED_URL)
            )
        }

        // Cookies belong to the host they came from; pointing at a different server has to start
        // from a clean session.
        if (cleanedPrepUrl != previousPrepUrl) {
            MultiplatformNetworkAdapter.clearCookies()
        }
    }

    /**
     * Moves installs off the endpoints earlier builds shipped with.
     *
     * The store is written during onboarding and outlives logout, so a new default alone reaches
     * fresh installs only - an existing one keeps pointing at the old server after an update.
     * Only an exact match on a retired default is rewritten: a URL the user typed themselves is
     * theirs, and gets left alone.
     */
    suspend fun migrateRetiredDefaults() {
        val current = stored()

        val prepUrl = if (current.prepUrl in EndpointConfig.RETIRED_PREP_URLS) {
            EndpointConfig.DEFAULT_PREP_URL
        } else {
            current.prepUrl
        }
        val docscedUrl = if (current.docscedUrl in EndpointConfig.RETIRED_DOCSCED_URLS) {
            EndpointConfig.DEFAULT_DOCSCED_URL
        } else {
            current.docscedUrl
        }

        if (prepUrl == current.prepUrl && docscedUrl == current.docscedUrl) {
            return
        }

        update(prepUrl, docscedUrl)
    }

    private fun stored(): EndpointConfig = StorageService.ENDPOINTS.flow.value

    /**
     * Reduces whatever was pasted to the bare origin that endpoint paths get appended to: https is
     * assumed when no scheme was typed, and everything from the first path separator onwards is
     * dropped. Applied on read as well as on write so values stored by an older build still resolve.
     */
    private fun normalize(url: String, fallback: String): String {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            return fallback
        }

        val absolute = if (SCHEME.containsMatchIn(trimmed)) trimmed else DEFAULT_SCHEME + trimmed
        val authorityStart = absolute.indexOf(SCHEME_SEPARATOR) + SCHEME_SEPARATOR.length
        val remainder = absolute.substring(authorityStart)
        val pathStart = remainder.indexOfFirst { it in PATH_DELIMITERS }
        val authority = if (pathStart < 0) remainder else remainder.take(pathStart)

        return if (authority.isBlank()) fallback else absolute.take(authorityStart) + authority
    }
}
