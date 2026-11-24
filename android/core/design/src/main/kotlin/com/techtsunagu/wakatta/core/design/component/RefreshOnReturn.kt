package com.techtsunagu.wakatta.core.design.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

/** Calls [onReturn] each time the screen is shown again, not the first time (the ViewModel loads that one). */
@Composable
fun RefreshOnReturn(onReturn: () -> Unit) {
    var shown by rememberSaveable { mutableStateOf(false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (shown) onReturn()
        shown = true
    }
}
