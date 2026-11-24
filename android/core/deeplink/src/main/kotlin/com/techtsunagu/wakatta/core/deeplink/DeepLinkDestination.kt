package com.techtsunagu.wakatta.core.deeplink

import androidx.navigation3.runtime.NavKey
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

/**
 * Where a link lands: the tab to select and the synthetic back stack for it (tab root first, target
 * last), so Back goes to the tab root and not out of the app. Role guards, the Worker-only scan call
 * and sign-in resume are app decisions made from the [AppLink], not here.
 */
data class DeepLinkDestination(val tab: TopLevelTab, val stack: List<NavKey>) {
    val target: NavKey get() = stack.last()
}

/**
 * Link-to-screen mapping. Extra arguments that no route key carries yet (shop code, message id,
 * event id, contact token) stay on the [AppLink]; the app passes them to the screen's own loader.
 */
fun AppLink.toDestination(): DeepLinkDestination = when (this) {
    is AppLink.Machine -> DeepLinkDestination(TopLevelTab.MACHINES, listOf(MachinesKey, MachineKey(id)))
    is AppLink.Contact -> DeepLinkDestination(TopLevelTab.ME, listOf(MeKey, ConnectKey))
    is AppLink.Thread -> DeepLinkDestination(TopLevelTab.CHATS, listOf(ChatsKey, ChatKey(id)))
    is AppLink.Notice -> DeepLinkDestination(TopLevelTab.HOME, listOf(HomeKey, NoticeKey(id)))
    is AppLink.Briefing -> DeepLinkDestination(TopLevelTab.HOME, listOf(HomeKey, BriefingCardKey(id)))
    is AppLink.NearMiss -> DeepLinkDestination(TopLevelTab.HOME, listOf(HomeKey, NearMissDetailKey(id)))
    AppLink.InterviewsDue -> DeepLinkDestination(TopLevelTab.HOME, listOf(HomeKey, InterviewKey()))
}
