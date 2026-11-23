package com.techtsunagu.wakatta.feature.chat

import androidx.annotation.StringRes
import com.techtsunagu.wakatta.core.model.AppError

/** Text for an error the ViewModel reported; the ViewModel itself never builds user-visible text. */
@StringRes
internal fun AppError.chatMessageRes(): Int = when (this) {
    AppError.Offline, AppError.Timeout -> R.string.chat_error_offline
    AppError.SessionExpired, AppError.Forbidden -> R.string.chat_error_session
    else -> R.string.chat_error_generic
}
