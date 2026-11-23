package com.techtsunagu.wakatta.feature.machines

import androidx.annotation.StringRes
import com.techtsunagu.wakatta.core.model.AppError

/** Text for an error a ViewModel reported; ViewModels never build user-visible text themselves. */
@StringRes
internal fun AppError.machinesMessageRes(): Int = when (this) {
    AppError.Offline, AppError.Timeout -> R.string.machines_error_offline
    AppError.NotFound -> R.string.machines_error_not_found
    else -> R.string.machines_generic_error
}
