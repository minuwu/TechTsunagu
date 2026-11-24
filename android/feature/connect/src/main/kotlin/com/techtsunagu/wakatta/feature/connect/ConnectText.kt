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
    QR_REPLACED(R.string.connect_err_qr_replaced),
    QR_UNKNOWN(R.string.connect_err_qr_unknown),
    OWN_QR(R.string.connect_err_own_qr),
    BLOCKED(R.string.connect_err_blocked),
    BAD_CODE(R.string.connect_err_bad_code),
    NO_QR_IN_PHOTO(R.string.connect_err_no_qr_in_photo),
    NO_SPEECH(R.string.connect_err_no_speech),
    RATE_LIMITED(R.string.connect_err_rate_limited),
    ALREADY_CONNECTED(R.string.connect_err_already_connected),
    UNKNOWN(R.string.connect_err_unknown),
}

internal fun AppError.toConnectError(): ConnectError = when (this) {
    AppError.Offline -> ConnectError.OFFLINE
    AppError.Timeout -> ConnectError.TIMEOUT
    AppError.Server -> ConnectError.SERVER
    AppError.NotFound -> ConnectError.NOT_FOUND
    AppError.Gone -> ConnectError.QR_REPLACED
    AppError.Forbidden -> ConnectError.BLOCKED
    is AppError.RateLimited -> ConnectError.RATE_LIMITED
    is AppError.Conflict -> if (code == "OWN_QR") ConnectError.OWN_QR else ConnectError.ALREADY_CONNECTED
    else -> ConnectError.UNKNOWN
}

/** Formats an ISO-8601 timestamp as a time (HH:mm) in the local timezone. */
internal fun formatContactTime(iso: String?): String? {
    if (iso.isNullOrBlank()) return null
    return try {
        val odt = runCatching { java.time.OffsetDateTime.parse(iso) }.getOrNull()
            ?: runCatching { java.time.Instant.parse(iso).atZone(java.time.ZoneId.systemDefault()).toOffsetDateTime() }.getOrNull()
        if (odt != null) {
            val local = odt.atZoneSameInstant(java.time.ZoneId.systemDefault())
            val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            local.format(formatter)
        } else if (iso.contains("T")) {
            val timePart = iso.substringAfter("T").take(5)
            if (timePart.length == 5 && timePart[2] == ':') timePart else null
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
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
