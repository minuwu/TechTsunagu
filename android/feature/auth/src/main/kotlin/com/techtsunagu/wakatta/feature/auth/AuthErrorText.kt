package com.techtsunagu.wakatta.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
internal fun authErrorMessage(kind: AuthErrorKind, arg: String): String = when (kind) {
    AuthErrorKind.WRONG_PIN -> stringResource(R.string.auth_error_wrong_pin)
    AuthErrorKind.LOCKED -> stringResource(R.string.auth_error_locked, arg)
    AuthErrorKind.RATE_LIMITED -> stringResource(R.string.auth_error_rate_limited, arg)
    AuthErrorKind.OFFLINE -> stringResource(R.string.auth_error_offline)
    AuthErrorKind.TIMEOUT -> stringResource(R.string.auth_error_timeout)
    AuthErrorKind.SERVER -> stringResource(R.string.auth_error_server)
    AuthErrorKind.UPGRADE_REQUIRED -> stringResource(R.string.auth_error_upgrade)
    AuthErrorKind.UNKNOWN_SHOP -> stringResource(R.string.auth_error_unknown_shop)
    AuthErrorKind.PIN_REJECTED -> stringResource(R.string.auth_error_pin_rejected)
    AuthErrorKind.UNKNOWN -> stringResource(R.string.auth_error_unknown)
}
