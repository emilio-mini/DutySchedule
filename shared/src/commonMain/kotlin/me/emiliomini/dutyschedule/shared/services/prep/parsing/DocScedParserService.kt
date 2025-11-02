@file:OptIn(ExperimentalTime::class)

package me.emiliomini.dutyschedule.shared.services.prep.parsing

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import kotlinx.io.IOException
import me.emiliomini.dutyschedule.shared.api.getPlatformLogger
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

object DocScedParserService {
    private val logger = getPlatformLogger("DocScedParserService")

    data class HaendDay(
        val date: Instant,
        val day: List<String>,
        val night: List<String>
    )

    suspend fun parseHaendData(html: String): Result<List<HaendDay>> {
        return try {
            if (html.isBlank()) return Result.failure(IOException("Empty HTML"))

            val doc = Ksoup.parse(html)

            val table = findFahrdienstTable(doc)
                ?: return Result.failure(IOException("Fahrdienst-Tabelle nicht gefunden"))

            val (idxDate, idxTag, idxNacht) = resolveColumnIndices(table)
                ?: return Result.failure(IOException("Benötigte Spalten nicht gefunden (Datum/Fahrdienst Tag/Fahrdienst Nacht)"))

            val days = table.select("tbody tr").mapNotNull { tr ->
                val tds = tr.select("td")
                if (tds.size <= idxNacht) return@mapNotNull null

                val dateText = tds[idxDate].text().trim()
                val datePart = dateText.substringBefore(" ").split(".").reversed().joinToString("-") + "T00:00:00+00:00" // "25.08.2025 (Mo)" -> "25.08.2025"
                val date = runCatching { Instant.parse(datePart) }.getOrElse {
                    logger.w("Überspringe Zeile: ungültiges Datum '$datePart'")
                    return@mapNotNull null
                }

                val tagList = splitNames(cleanCell(tds[idxTag]))
                val nachtList = splitNames(cleanCell(tds[idxNacht]))

                HaendDay(
                    date = date,
                    day = tagList,
                    night = nachtList
                )
            }

            logger.d("Parsed ${days.size} Fahrdienst-Tage")
            Result.success(days)
        } catch (e: Exception) {
            logger.e("Fehler beim Parsen: ${e.message}")
            Result.failure(e)
        }
    }

    // ---- Helpers ----
    private fun findFahrdienstTable(doc: Document): Element? {
        // Suche die Tabelle, deren Header die Fahrdienst-Spalten enthält
        val tables = doc.select("table")
        return tables.firstOrNull { table ->
            val headers =
                table.select("thead tr").firstOrNull()?.select("td,th")?.map { it.text().trim() }
                    ?: emptyList()
            headers.any { it.contains("Fahrdienst Tag", ignoreCase = true) } &&
                    headers.any { it.contains("Fahrdienst Nacht", ignoreCase = true) } &&
                    headers.any { it.contains("Datum", ignoreCase = true) }
        } ?: tables.firstOrNull() // Fallback: erste Tabelle
    }

    private data class ColIdx(val date: Int, val tag: Int, val nacht: Int)

    private fun resolveColumnIndices(table: Element): ColIdx? {
        val headerCells = table.select("thead tr").firstOrNull()?.select("td,th") ?: return null
        if (headerCells.isEmpty()) return null

        val labels = headerCells.mapIndexed { idx, el -> el.text().trim() to idx }.toMap()

        val idxDate =
            labels.entries.firstOrNull { it.key.contains("Datum", ignoreCase = true) }?.value
        val idxTag = labels.entries.firstOrNull {
            it.key.contains(
                "Fahrdienst Tag",
                ignoreCase = true
            )
        }?.value
        val idxNacht = labels.entries.firstOrNull {
            it.key.contains(
                "Fahrdienst Nacht",
                ignoreCase = true
            )
        }?.value

        return if (idxDate != null && idxTag != null && idxNacht != null) {
            ColIdx(idxDate, idxTag, idxNacht)
        } else null
    }

    private fun cleanCell(td: Element): String? {
        val txt = td.text().trim().replace(Regex("\\s+"), " ")
        return if (txt.isEmpty() || txt == "-") null else txt
    }

    private fun splitNames(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        // Trenne an Komma, Strichpunkt, Slash, Pipe oder Zeilenumbrüchen – und trimme
        return raw.split(Regex("[,;\\n/|]+"))
            .map { it.trim() }
            .filter { it.isNotEmpty() && it != "-" }
            .distinct()
    }
}
