package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

interface FeatureEntries {
    /** Registers every screen this feature owns. */
    fun EntryProviderScope<NavKey>.install(navigator: Navigator)
}
