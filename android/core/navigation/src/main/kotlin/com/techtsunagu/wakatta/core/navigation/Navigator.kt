package com.techtsunagu.wakatta.core.navigation

import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.model.TopLevelTab

interface Navigator {
    fun push(key: NavKey)
    fun pop(): Boolean
    fun switchTab(key: NavKey)
    fun switchTab(tab: TopLevelTab)
    fun replaceTop(key: NavKey)
    fun replaceAll(key: NavKey)
}
