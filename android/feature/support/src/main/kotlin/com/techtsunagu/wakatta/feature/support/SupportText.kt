package com.techtsunagu.wakatta.feature.support

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueDto
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

/** Why a load or save failed; UI state keeps this enum and the screen maps it to a string. */
internal enum class SupportError(@StringRes val message: Int) {
    OFFLINE(R.string.support_err_offline),
    TIMEOUT(R.string.support_err_timeout),
    SERVER(R.string.support_err_server),
    NOT_FOUND(R.string.support_err_not_found),
    UNKNOWN(R.string.support_err_unknown),
}

internal fun AppError.toSupportError(): SupportError = when (this) {
    AppError.Offline -> SupportError.OFFLINE
    AppError.Timeout -> SupportError.TIMEOUT
    AppError.Server -> SupportError.SERVER
    AppError.NotFound -> SupportError.NOT_FOUND
    else -> SupportError.UNKNOWN
}

/** Form problems the worker or foreman can fix. */
internal enum class InterviewIssue(@StringRes val message: Int) {
    NOTE_REQUIRED(R.string.support_interview_err_note_required),
    FUTURE_DATE(R.string.support_interview_err_future_date),
    CONSENT_REQUIRED(R.string.support_interview_err_consent_required),
}

@Composable
internal fun dueLabel(due: InterviewDueDto): String = when {
    due.lastDate == null -> stringResource(R.string.support_due_never)
    due.daysLeft < 0 -> stringResource(R.string.support_due_overdue, -due.daysLeft)
    else -> stringResource(R.string.support_due_left, due.daysLeft)
}

/** "all" or a "yyyy-MM" filter value shown as a localized month name. */
@Composable
internal fun monthLabel(month: String): String {
    val num = month.substringAfter('-', "").toIntOrNull()
    return if (num in 1..12) {
        Month.of(num!!).getDisplayName(TextStyle.SHORT, Locale.getDefault())
    } else {
        stringResource(R.string.support_log_filter_all_months)
    }
}
