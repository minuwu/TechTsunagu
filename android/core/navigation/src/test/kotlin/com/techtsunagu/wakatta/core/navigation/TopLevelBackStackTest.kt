package com.techtsunagu.wakatta.core.navigation

import com.techtsunagu.wakatta.core.model.TopLevelTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TopLevelBackStackTest {

    private lateinit var backStack: TopLevelBackStack

    @Before
    fun setUp() {
        backStack = TopLevelBackStack()
    }

    @Test
    fun initialStateHasHomeTabAndRootKeys() {
        assertEquals(TopLevelTab.HOME, backStack.currentTab.value)
        assertEquals(listOf(HomeKey), backStack.currentStack.value)
        assertEquals(listOf(HomeKey), backStack.stackFor(TopLevelTab.HOME).toList())
        assertEquals(listOf(ChatsKey), backStack.stackFor(TopLevelTab.CHATS).toList())
        assertEquals(listOf(BoardKey), backStack.stackFor(TopLevelTab.BOARD).toList())
        assertEquals(listOf(MachinesKey), backStack.stackFor(TopLevelTab.MACHINES).toList())
        assertEquals(listOf(MeKey), backStack.stackFor(TopLevelTab.ME).toList())
    }

    @Test
    fun pushAddsKeyToCurrentTabStack() {
        backStack.push(MachineKey("m-1"))
        assertEquals(listOf(HomeKey, MachineKey("m-1")), backStack.currentStack.value)
        assertEquals(listOf(HomeKey, MachineKey("m-1")), backStack.stackFor(TopLevelTab.HOME).toList())
    }

    @Test
    fun popRemovesTopKeyWhenMoreThanOne() {
        backStack.push(MachineKey("m-1"))
        val popped = backStack.pop()
        assertTrue(popped)
        assertEquals(listOf(HomeKey), backStack.currentStack.value)
    }

    @Test
    fun popDoesNotRemoveRootKey() {
        val popped = backStack.pop()
        assertFalse(popped)
        assertEquals(listOf(HomeKey), backStack.currentStack.value)
    }

    @Test
    fun replaceTopReplacesCurrentTopKey() {
        backStack.push(MachineKey("m-1"))
        backStack.replaceTop(MachineKey("m-2"))
        assertEquals(listOf(HomeKey, MachineKey("m-2")), backStack.currentStack.value)
    }

    @Test
    fun replaceAllClearsAndSetsSingleKey() {
        backStack.push(MachineKey("m-1"))
        backStack.push(MachineKey("m-2"))
        backStack.replaceAll(MachineKey("m-3"))
        assertEquals(listOf(MachineKey("m-3")), backStack.currentStack.value)
    }

    @Test
    fun switchTabByEnumKeepsEachTabsStack() {
        backStack.push(MachineKey("m-1"))
        assertEquals(2, backStack.currentStack.value.size)

        backStack.switchTab(TopLevelTab.CHATS)
        assertEquals(TopLevelTab.CHATS, backStack.currentTab.value)
        assertEquals(listOf(ChatsKey), backStack.currentStack.value)

        backStack.push(ChatKey("thread-1"))
        assertEquals(listOf(ChatsKey, ChatKey("thread-1")), backStack.currentStack.value)

        backStack.switchTab(TopLevelTab.HOME)
        assertEquals(TopLevelTab.HOME, backStack.currentTab.value)
        assertEquals(listOf(HomeKey, MachineKey("m-1")), backStack.currentStack.value)

        backStack.switchTab(TopLevelTab.CHATS)
        assertEquals(listOf(ChatsKey, ChatKey("thread-1")), backStack.currentStack.value)
    }

    @Test
    fun switchTabByKeyKeepsEachTabsStack() {
        backStack.push(MachineKey("m-1"))

        backStack.switchTab(ChatsKey)
        assertEquals(TopLevelTab.CHATS, backStack.currentTab.value)
        assertEquals(listOf(ChatsKey), backStack.currentStack.value)

        backStack.switchTab(BoardKey)
        assertEquals(TopLevelTab.BOARD, backStack.currentTab.value)
        assertEquals(listOf(BoardKey), backStack.currentStack.value)

        backStack.switchTab(MachinesKey)
        assertEquals(TopLevelTab.MACHINES, backStack.currentTab.value)
        assertEquals(listOf(MachinesKey), backStack.currentStack.value)

        backStack.switchTab(MeKey)
        assertEquals(TopLevelTab.ME, backStack.currentTab.value)
        assertEquals(listOf(MeKey), backStack.currentStack.value)

        backStack.switchTab(HomeKey)
        assertEquals(TopLevelTab.HOME, backStack.currentTab.value)
        assertEquals(listOf(HomeKey, MachineKey("m-1")), backStack.currentStack.value)
    }
}
