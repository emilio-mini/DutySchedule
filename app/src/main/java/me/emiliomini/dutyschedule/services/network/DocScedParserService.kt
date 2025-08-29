package me.emiliomini.dutyschedule.services.parsers

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Parser für DocSced-Dienstplan-HTML.
 * Trennt Fahrdienst in Tag+Nacht und liefert pro Datum die Namen (so wie sie im Plan stehen).
 */
object DocScedParserService {

    private const val TAG = "DocScedParserService"
    private val DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.GERMANY)

    data class FahrdienstDay(
        val date: LocalDate,
        val tag: List<String>,   // Fahrdienst Tag (0..n Namen)
        val nacht: List<String>  // Fahrdienst Nacht (0..n Namen)
    )

    /**
     * Parst das übergebene DocSced-HTML (eine Woche/Monat/Gesamt) und extrahiert Fahrdienst (Tag/Nacht).
     * - Robust gegenüber Farb-/Style-Attributen
     * - Ignoriert Zellen mit "-" oder leerem Text
     * - Unterstützt mehrere Namen je Zelle (trennt an Komma, Slash, Semikolon, Zeilenumbruch)
     */
    suspend fun parseFahrdienst(html: String): Result<List<FahrdienstDay>> {
        return try {
            if (html.isBlank()) return Result.failure(IOException("Empty HTML"))

            val doc = Jsoup.parse(html)

            val table = findFahrdienstTable(doc)
                ?: return Result.failure(IOException("Fahrdienst-Tabelle nicht gefunden"))

            val (idxDate, idxTag, idxNacht) = resolveColumnIndices(table)
                ?: return Result.failure(IOException("Benötigte Spalten nicht gefunden (Datum/Fahrdienst Tag/Fahrdienst Nacht)"))

            val days = table.select("tbody tr").mapNotNull { tr ->
                val tds = tr.select("td")
                if (tds.size <= idxNacht) return@mapNotNull null

                val dateText = tds[idxDate].text().trim()
                val datePart = dateText.substringBefore(" ") // "25.08.2025 (Mo)" -> "25.08.2025"
                val date = runCatching { LocalDate.parse(datePart, DATE_FMT) }.getOrElse {
                    Log.w(TAG, "Überspringe Zeile: ungültiges Datum '$dateText'")
                    return@mapNotNull null
                }

                val tagList = splitNames(cleanCell(tds[idxTag]))
                val nachtList = splitNames(cleanCell(tds[idxNacht]))

                FahrdienstDay(
                    date = date,
                    tag = tagList,
                    nacht = nachtList
                )
            }

            Log.d(TAG, "Parsed ${days.size} Fahrdienst-Tage")
            Result.success(days)
        } catch (e: Exception) {
            Log.e(TAG, "Fehler beim Parsen: ${e.message}")
            Result.failure(e)
        }
    }

    // ---- Helpers ----
    private fun findFahrdienstTable(doc: Document): Element? {
        // Suche die Tabelle, deren Header die Fahrdienst-Spalten enthält
        val tables = doc.select("table")
        return tables.firstOrNull { table ->
            val headers = table.select("thead tr").firstOrNull()?.select("td,th")?.map { it.text().trim() } ?: emptyList()
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

        val idxDate  = labels.entries.firstOrNull { it.key.contains("Datum", ignoreCase = true) }?.value
        val idxTag   = labels.entries.firstOrNull { it.key.contains("Fahrdienst Tag", ignoreCase = true) }?.value
        val idxNacht = labels.entries.firstOrNull { it.key.contains("Fahrdienst Nacht", ignoreCase = true) }?.value

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
