package com.techtsunagu.wakatta.core.common

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Errors a screen did not expect. A release build reports them here and keeps running;
 * a debug build (and every unit test) rethrows, so bugs stay loud while we build.
 */
object CrashGuard {
    /** The app sets this from BuildConfig.DEBUG; true until then, so tests still fail on a bug. */
    @Volatile var rethrow: Boolean = true

    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** The activity shows one short message per error. */
    val errors: SharedFlow<Throwable> = _errors.asSharedFlow()

    fun report(error: Throwable) {
        if (rethrow) throw error
        _errors.tryEmit(error)
    }
}
