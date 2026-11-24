package com.techtsunagu.wakatta.feature.auth

import com.techtsunagu.wakatta.core.model.AppError
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** What the screen can say about a failure; the text lives in string resources. */
enum class AuthErrorKind {
    WRONG_PIN,
    LOCKED,
    RATE_LIMITED,
    OFFLINE,
    TIMEOUT,
    SERVER,
    UPGRADE_REQUIRED,
    UNKNOWN_SHOP,
    PIN_REJECTED,
    CODE_TAKEN,
    NO_MATCH,
    UNKNOWN,
}

/** The kind plus one text argument: the unlock time for [AuthErrorKind.LOCKED], seconds for RATE_LIMITED. */
internal fun AppError.toAuthError(): Pair<AuthErrorKind, String> = when (this) {
    AppError.WrongPin -> AuthErrorKind.WRONG_PIN to ""
    is AppError.PinLocked -> AuthErrorKind.LOCKED to formatUnlockTime(until)
    is AppError.RateLimited -> AuthErrorKind.RATE_LIMITED to (retryAfter ?: DEFAULT_WAIT_SECONDS).toString()
    AppError.Offline -> AuthErrorKind.OFFLINE to ""
    AppError.Timeout -> AuthErrorKind.TIMEOUT to ""
    AppError.Server, AppError.Busy -> AuthErrorKind.SERVER to ""
    AppError.UpgradeRequired -> AuthErrorKind.UPGRADE_REQUIRED to ""
    AppError.NotFound -> AuthErrorKind.UNKNOWN_SHOP to ""
    is AppError.Validation -> AuthErrorKind.PIN_REJECTED to ""
    is AppError.Conflict -> (if (code == "SHOP_CODE_TAKEN") AuthErrorKind.CODE_TAKEN else AuthErrorKind.UNKNOWN) to ""
    else -> AuthErrorKind.UNKNOWN to ""
}

private const val DEFAULT_WAIT_SECONDS = 60L

private val unlockFormat = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Asia/Tokyo"))

/** `details.until` is an ISO-8601 instant; people read it in shop time (Asia/Tokyo). */
internal fun formatUnlockTime(until: String): String =
    runCatching { unlockFormat.format(OffsetDateTime.parse(until)) }.getOrDefault(until)
