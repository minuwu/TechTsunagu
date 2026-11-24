package com.techtsunagu.wakatta.core.model

/**
 * Standard typed application error representation based on BACKEND_ARCHITECTURE.md 9.2.
 */
sealed interface AppError {
    data object Offline : AppError
    data object Timeout : AppError
    data object WrongPin : AppError
    data class PinLocked(val until: String) : AppError
    data object PinChangeRequired : AppError
    data object SessionExpired : AppError
    data object Forbidden : AppError
    data object NotFound : AppError

    /** 410: it existed and was replaced or withdrawn (a My QR its owner made new). */
    data object Gone : AppError
    data class Conflict(val code: String) : AppError
    data object PreconditionFailed : AppError
    data class Validation(val fields: List<String> = emptyList()) : AppError
    data class RateLimited(val retryAfter: Long? = null) : AppError
    data object Busy : AppError
    data object PayloadTooLarge : AppError
    data object UnsupportedMedia : AppError
    data object UpgradeRequired : AppError
    data object Server : AppError
    data class Unknown(val message: String? = null) : AppError
}
