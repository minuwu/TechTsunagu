package com.techtsunagu.wakatta.feature.chat

import android.content.Context
import com.techtsunagu.wakatta.core.model.AppError

/** Text for an error the ViewModel reported; the ViewModel itself never builds user-visible text. */
internal fun AppError.chatMessage(context: Context): String = when (this) {
    AppError.Offline -> context.getString(R.string.chat_error_offline)
    AppError.Timeout -> context.getString(R.string.chat_error_timeout)
    is AppError.RateLimited -> retryAfter?.let { context.getString(R.string.chat_error_rate_limited_in, it) }
        ?: context.getString(R.string.chat_error_rate_limited)
    AppError.Busy -> context.getString(R.string.chat_error_ai_busy)
    AppError.SessionExpired, AppError.Forbidden -> context.getString(R.string.chat_error_session)
    is AppError.Conflict -> context.getString(R.string.chat_error_already_sent)
    else -> context.getString(R.string.chat_error_generic)
}
