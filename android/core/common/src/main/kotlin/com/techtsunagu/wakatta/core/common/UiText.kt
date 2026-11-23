package com.techtsunagu.wakatta.core.common

import androidx.annotation.StringRes
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.model.pick

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(@StringRes val resId: Int, vararg val args: Any) : UiText
    data class TriString(val tri: Tri, val contentLang: String = "ja") : UiText

    fun asString(context: android.content.Context, lang: String = "ja"): String = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId, *args)
        is TriString -> pick(tri, lang)
    }
}
