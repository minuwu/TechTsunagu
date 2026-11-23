package com.techtsunagu.wakatta.core.model

/**
 * Translated text map where keys are language tags (e.g., "ja", "en", "bn", "vi", etc.)
 * and values are the corresponding localized strings.
 */
typealias Tri = Map<String, String>

/**
 * Selects the best text representation for the given language.
 * Fallback chain: target language -> English ("en") -> Japanese ("ja") -> first available -> empty string.
 */
fun pick(tri: Tri?, lang: String?): String {
    if (tri == null || tri.isEmpty()) return ""
    if (lang != null && tri.containsKey(lang)) {
        val text = tri[lang]
        if (!text.isNullOrEmpty()) return text
    }
    return tri["en"]?.takeIf { it.isNotEmpty() }
        ?: tri["ja"]?.takeIf { it.isNotEmpty() }
        ?: tri.values.firstOrNull { it.isNotEmpty() }
        ?: ""
}

/**
 * Removes Japanese furigana annotations formatted as 漢字{よみ} -> 漢字.
 */
fun stripFurigana(text: String): String {
    return text.replace(Regex("\\{([^}]+)\\}"), "")
}
