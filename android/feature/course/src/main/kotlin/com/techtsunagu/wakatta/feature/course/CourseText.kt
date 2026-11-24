package com.techtsunagu.wakatta.feature.course

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.em
import com.techtsunagu.wakatta.core.model.stripFurigana

private val RUBY = Regex("([\\u4E00-\\u9FFF々]+)\\{([^}]+)\\}")

/**
 * `kanji{reading}` markup to text.
 * shortcut: no true ruby layout; the reading follows the kanji in small type. Upgrade with a custom ruby Layout.
 */
internal fun furiganaText(ja: String, showReading: Boolean): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (m in RUBY.findAll(ja)) {
        append(stripFurigana(ja.substring(last, m.range.first)))
        append(m.groupValues[1])
        if (showReading) withStyle(SpanStyle(fontSize = 0.6.em)) { append("(${m.groupValues[2]})") }
        last = m.range.last + 1
    }
    append(stripFurigana(ja.substring(last)))
}

/** A Japanese line, with its reading above and its translation below when the user turned them on. */
@Composable
internal fun LineText(
    line: Line,
    furigana: Boolean,
    translation: Boolean,
    modifier: Modifier = Modifier,
    big: Boolean = false,
) {
    val ja = remember(line.ja, furigana) { furiganaText(line.ja, furigana) }
    Column(modifier) {
        Text(ja, style = if (big) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.bodyLarge)
        if (translation && line.tr.isNotEmpty()) {
            Text(line.tr, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
