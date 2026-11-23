package com.techtsunagu.wakatta.feature.connect

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.techtsunagu.wakatta.core.model.AppError

/** Why something failed; UI state keeps this enum and the screen maps it to a plain-language string. */
internal enum class ConnectError(@StringRes val message: Int) {
    OFFLINE(R.string.connect_err_offline),
    TIMEOUT(R.string.connect_err_timeout),
    SERVER(R.string.connect_err_server),
    NOT_FOUND(R.string.connect_err_not_found),
    BLOCKED(R.string.connect_err_blocked),
    BAD_CODE(R.string.connect_err_bad_code),
    NO_SPEECH(R.string.connect_err_no_speech),
    UNKNOWN(R.string.connect_err_unknown),
}

internal fun AppError.toConnectError(): ConnectError = when (this) {
    AppError.Offline -> ConnectError.OFFLINE
    AppError.Timeout -> ConnectError.TIMEOUT
    AppError.Server -> ConnectError.SERVER
    AppError.NotFound -> ConnectError.NOT_FOUND
    AppError.Forbidden -> ConnectError.BLOCKED
    else -> ConnectError.UNKNOWN
}

/** The person's own name for their language; these are endonyms, not translated. */
internal fun languageName(code: String): String = when (code) {
    "ja" -> "日本語"
    "en" -> "English"
    "bn" -> "বাংলা"
    "vi" -> "Tiếng Việt"
    "id" -> "Bahasa Indonesia"
    else -> code
}

/** The app language code ("ja", "en", "bn") used to pick which side of a translation to show first. */
@Composable
internal fun appLanguage(): String = LocalConfiguration.current.locales[0].language

/** Shared name rule for a Talk Thread: 1 to 40 characters. */
internal fun isValidTalkName(name: String): Boolean = name.trim().length in 1..40
