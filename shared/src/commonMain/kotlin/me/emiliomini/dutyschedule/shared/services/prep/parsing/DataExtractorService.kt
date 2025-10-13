package me.emiliomini.dutyschedule.shared.services.prep.parsing

import me.emiliomini.dutyschedule.shared.datastores.Incode
import me.emiliomini.dutyschedule.shared.util.toTimestamp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object DataExtractorService {
    private val INCODE_REGEX = "'(x-incode-[^']+)': '([^']+)'".toRegex()
    private val GUID_REGEX = "ressourceDataGuid === '([^']+)'".toRegex()
    private val ORG_TREE_REGEX = "src=\"(.+org_tree\\.js[^\"]+)\">".toRegex()
    private val ALLOWED_ORGS_REGEX = "allowedOrgUnits = \\[([^]]+)];".toRegex()

    @OptIn(ExperimentalTime::class)
    fun extractIncode(input: String): Incode? {
        val result = INCODE_REGEX.find(input) ?: return null

        val token = result.groups[1]?.value ?: return null
        val value = result.groups[2]?.value ?: return null

        return Incode(token, value, Clock.System.now().toTimestamp())
    }

    fun extractGUID(input: String): String? {
        return GUID_REGEX.find(input)?.groups[1]?.value
    }

    fun extractOrgTreeUrl(input: String): String? {
        val url = ORG_TREE_REGEX.find(input)?.groups[1]?.value ?: return null

        return url.replace('\\', '/')
    }

    fun extractAllowedOrgs(input: String): List<String>? {
        val orgList = ALLOWED_ORGS_REGEX.find(input)?.groups[1]?.value ?: return null

        return orgList
            .split(",")
            .map { it.trim() }
            .map { it.filter { char -> char != '"' } }
    }
}