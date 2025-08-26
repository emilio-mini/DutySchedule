package me.emiliomini.dutyschedule.services.network

import me.emiliomini.dutyschedule.models.prep.Incode

object DataExtractorService {
    private val INCODE_REGEX = "'(x-incode-[^']+)': '([^']+)'".toRegex()
    private val GUID_REGEX = "ressourceDataGuid === '([^']+)'".toRegex()
    private val ORG_TREE_REGEX = "src=\"(.+org_tree\\.js[^\"]+)\">".toRegex()
    private val ALLOWED_ORGS_REGEX = "allowedOrgUnits = \\[([^]]+)];".toRegex()

    fun extractIncode(input: String): Incode? {
        val result = INCODE_REGEX.find(input) ?: return null

        val token = result.groups[1]?.value ?: return null
        val value = result.groups[2]?.value ?: return null

        return Incode(token, value)
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