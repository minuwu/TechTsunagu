package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.pick

internal fun resolveContentLang(tri: Tri?, lang: String?): String {
    if (tri.isNullOrEmpty()) return ""
    if (lang != null && !tri[lang].isNullOrEmpty()) return lang
    if (!tri["en"].isNullOrEmpty()) return "en"
    if (!tri["ja"].isNullOrEmpty()) return "ja"
    return tri.entries.firstOrNull { it.value.isNotEmpty() }?.key ?: ""
}

@Composable
fun TriText(
    tri: Tri?,
    lang: String?,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val text = pick(tri, lang)
    val contentLang = resolveContentLang(tri, lang)
    val textStyle = if (contentLang.isNotEmpty()) {
        style.copy(localeList = LocaleList(Locale(contentLang)))
    } else {
        style
    }
    Text(
        text = text,
        modifier = modifier,
        style = textStyle,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun TriTextLightPreview() {
    val sampleTri = mapOf(
        "ja" to "金型交換の前に電源をロックアウトしてください。",
        "en" to "Please lock out the power before die change.",
        "bn" to "ডাই পরিবর্তনের আগে পাওয়ার লকআউট করুন।",
    )
    WakattaTheme(darkTheme = false) {
        Column {
            TriText(tri = sampleTri, lang = "ja")
            TriText(tri = sampleTri, lang = "en")
            TriText(tri = sampleTri, lang = "bn")
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun TriTextDarkPreview() {
    val sampleTri = mapOf(
        "ja" to "金型交換の前に電源をロックアウトしてください。",
        "en" to "Please lock out the power before die change.",
        "bn" to "ডাই পরিবর্তনের আগে পাওয়ার লকআউট করুন।",
    )
    WakattaTheme(darkTheme = true) {
        Column {
            TriText(tri = sampleTri, lang = "ja")
            TriText(tri = sampleTri, lang = "en")
            TriText(tri = sampleTri, lang = "bn")
        }
    }
}
