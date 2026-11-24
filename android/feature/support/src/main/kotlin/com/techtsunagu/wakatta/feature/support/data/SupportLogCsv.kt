package com.techtsunagu.wakatta.feature.support.data

/** One row of `GET /support-log.csv` (支援項目, 日時, 氏名, 内容, 対応・相手, 記録元). */
internal data class SupportLogRow(
    val item: String,
    val date: String,
    val worker: String,
    val content: String,
    val handling: String,
    val source: String,
)

internal const val ITEM_CONSULTATION = "7"
internal const val ITEM_EXCHANGE = "8"
internal const val ITEM_INTERVIEW = "10"
internal const val ITEM_ORIENTATION = "4"

/** Rows below the header line. [SupportLogRow.item] is only the item number, [SupportLogRow.date] only the day. */
internal fun parseSupportLog(csv: String): List<SupportLogRow> = csvRows(csv.removePrefix("\uFEFF"))
    .dropWhile { it.firstOrNull() != "支援項目" }
    .drop(1)
    .filter { it.size >= 6 }
    .map { c -> SupportLogRow(c[0].substringBefore(' '), c[1].take(10), plain(c[2]), plain(c[3]), plain(c[4]), c[5]) }

/** The server puts an apostrophe before text that Excel would run as a formula. */
private fun plain(cell: String) = if (cell.length > 1 && cell[0] == '\'' && cell[1] in "=+-@\t\r") cell.drop(1) else cell

/** RFC 4180: quoted cells may hold commas, line breaks and doubled quotes. */
private fun csvRows(text: String): List<List<String>> {
    val rows = mutableListOf<List<String>>()
    var row = mutableListOf<String>()
    val cell = StringBuilder()
    var quoted = false
    var i = 0
    fun endCell() {
        row.add(cell.toString())
        cell.clear()
    }
    fun endRow() {
        endCell()
        rows.add(row)
        row = mutableListOf()
    }
    while (i < text.length) {
        val c = text[i]
        when {
            quoted && c == '"' && text.getOrNull(i + 1) == '"' -> {
                cell.append('"')
                i++
            }
            c == '"' -> quoted = !quoted
            quoted -> cell.append(c)
            c == ',' -> endCell()
            c == '\n' -> endRow()
            c != '\r' -> cell.append(c)
        }
        i++
    }
    if (cell.isNotEmpty() || row.isNotEmpty()) endRow()
    return rows
}
