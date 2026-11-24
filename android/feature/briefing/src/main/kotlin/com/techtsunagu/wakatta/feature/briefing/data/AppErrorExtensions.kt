package com.techtsunagu.wakatta.feature.briefing.data

import com.techtsunagu.wakatta.core.model.AppError

internal fun AppError.userMessage(): String = when (this) {
    is AppError.Offline -> "Offline. Please check your connection."
    is AppError.Timeout -> "Request timed out. Please try again."
    is AppError.NotFound -> "Briefing not found."
    is AppError.Server -> "Server error. Please try again later."
    is AppError.RateLimited -> "Too many requests. Please wait."
    is AppError.Unknown -> message ?: "An error occurred."
    else -> "An error occurred."
}
