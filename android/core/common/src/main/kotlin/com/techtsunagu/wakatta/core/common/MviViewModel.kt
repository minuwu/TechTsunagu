package com.techtsunagu.wakatta.core.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class MviViewModel<S, E, F>(initialState: S) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<F>(Channel.BUFFERED)
    val effects: Flow<F> = _effects.receiveAsFlow()

    /**
     * Shadows the androidx extension (a member wins), so every `viewModelScope.launch` in a screen
     * goes through [CrashGuard]: in a release build an unexpected error shows a message, not a crash.
     */
    protected val viewModelScope: CoroutineScope by lazy {
        val base = (this as ViewModel).viewModelScope
        CoroutineScope(base.coroutineContext + CoroutineExceptionHandler { _, e -> CrashGuard.report(e) })
    }

    abstract fun onEvent(event: E)

    protected fun updateState(reducer: (S) -> S) {
        _state.update(reducer)
    }

    protected fun sendEffect(effect: F) {
        viewModelScope.launch {
            _effects.send(effect)
        }
    }
}
