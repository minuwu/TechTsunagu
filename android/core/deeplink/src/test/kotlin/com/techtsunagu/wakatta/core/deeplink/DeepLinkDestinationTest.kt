package com.techtsunagu.wakatta.core.deeplink

import com.techtsunagu.wakatta.core.navigation.ConnectScanKey
import com.techtsunagu.wakatta.core.model.TopLevelTab
import com.techtsunagu.wakatta.core.navigation.BriefingCardKey
import com.techtsunagu.wakatta.core.navigation.ChatKey
import com.techtsunagu.wakatta.core.navigation.ChatsKey
import com.techtsunagu.wakatta.core.navigation.ConnectKey
import com.techtsunagu.wakatta.core.navigation.HomeKey
import com.techtsunagu.wakatta.core.navigation.InterviewKey
import com.techtsunagu.wakatta.core.navigation.MachineKey
import com.techtsunagu.wakatta.core.navigation.MachinesKey
import com.techtsunagu.wakatta.core.navigation.MeKey
import com.techtsunagu.wakatta.core.navigation.NearMissDetailKey
import com.techtsunagu.wakatta.core.navigation.NoticeKey
import org.junit.Assert.assertEquals
import org.junit.Test

class DeepLinkDestinationTest {

    @Test
    fun everyLinkGetsATabRootThenTheTarget() {
        val cases = listOf(
            AppLink.Machine("12", "ABCD1234") to DeepLinkDestination(TopLevelTab.MACHINES, listOf(MachinesKey, MachineKey("12"))),
            AppLink.Contact("AbCdEfGhIjKlMnOpQrSt-_") to DeepLinkDestination(TopLevelTab.ME, listOf(MeKey, ConnectKey, ConnectScanKey("AbCdEfGhIjKlMnOpQrSt-_"))),
            AppLink.Thread("7", "9") to DeepLinkDestination(TopLevelTab.CHATS, listOf(ChatsKey, ChatKey("7"))),
            AppLink.Notice("5", "3") to DeepLinkDestination(TopLevelTab.HOME, listOf(HomeKey, NoticeKey("5"))),
            AppLink.Briefing("8") to DeepLinkDestination(TopLevelTab.HOME, listOf(HomeKey, BriefingCardKey("8"))),
            AppLink.NearMiss("4") to DeepLinkDestination(TopLevelTab.HOME, listOf(HomeKey, NearMissDetailKey("4"))),
            AppLink.InterviewsDue to DeepLinkDestination(TopLevelTab.HOME, listOf(HomeKey, InterviewKey())),
        )
        cases.forEach { (link, expected) -> assertEquals(link.toString(), expected, link.toDestination()) }
    }

    @Test
    fun targetIsTheLastKey() {
        assertEquals(MachineKey("12"), AppLink.Machine("12", null).toDestination().target)
    }
}
