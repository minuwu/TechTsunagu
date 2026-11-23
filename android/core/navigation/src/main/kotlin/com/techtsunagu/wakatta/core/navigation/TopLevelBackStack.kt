package com.techtsunagu.wakatta.core.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.navigation3.runtime.NavKey
import com.techtsunagu.wakatta.core.model.TopLevelTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TopLevelBackStack : Navigator {
    private val stacks: Map<TopLevelTab, SnapshotStateList<NavKey>> = mapOf(
        TopLevelTab.HOME to mutableStateListOf<NavKey>(HomeKey),
        TopLevelTab.CHATS to mutableStateListOf<NavKey>(ChatsKey),
        TopLevelTab.BOARD to mutableStateListOf<NavKey>(BoardKey),
        TopLevelTab.MACHINES to mutableStateListOf<NavKey>(MachinesKey),
        TopLevelTab.ME to mutableStateListOf<NavKey>(MeKey)
    )

    private val _currentTab = MutableStateFlow(TopLevelTab.HOME)
    val currentTab: StateFlow<TopLevelTab> = _currentTab.asStateFlow()

    private val _currentStack = MutableStateFlow<List<NavKey>>(listOf(HomeKey))
    val currentStack: StateFlow<List<NavKey>> = _currentStack.asStateFlow()

    fun stackFor(tab: TopLevelTab): SnapshotStateList<NavKey> = stacks.getValue(tab)

    val currentBackStack: SnapshotStateList<NavKey>
        get() = stacks.getValue(_currentTab.value)

    override fun switchTab(tab: TopLevelTab) {
        _currentTab.value = tab
        _currentStack.value = stacks.getValue(tab).toList()
    }

    override fun switchTab(key: NavKey) {
        val tab = when (key) {
            is HomeKey -> TopLevelTab.HOME
            is ChatsKey -> TopLevelTab.CHATS
            is BoardKey -> TopLevelTab.BOARD
            is MachinesKey -> TopLevelTab.MACHINES
            is MeKey -> TopLevelTab.ME
            else -> null
        }
        if (tab != null) {
            switchTab(tab)
        }
    }

    override fun push(key: NavKey) {
        val tab = _currentTab.value
        val list = stacks.getValue(tab)
        list.add(key)
        _currentStack.value = list.toList()
    }

    override fun pop(): Boolean {
        val tab = _currentTab.value
        val list = stacks.getValue(tab)
        if (list.size > 1) {
            list.removeAt(list.size - 1)
            _currentStack.value = list.toList()
            return true
        }
        return false
    }

    override fun replaceTop(key: NavKey) {
        val tab = _currentTab.value
        val list = stacks.getValue(tab)
        if (list.isNotEmpty()) {
            list[list.size - 1] = key
        } else {
            list.add(key)
        }
        _currentStack.value = list.toList()
    }

    override fun replaceAll(key: NavKey) {
        val tab = _currentTab.value
        val list = stacks.getValue(tab)
        list.clear()
        list.add(key)
        _currentStack.value = list.toList()
    }
}
