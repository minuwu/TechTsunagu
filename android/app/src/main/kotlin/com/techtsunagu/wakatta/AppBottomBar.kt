package com.techtsunagu.wakatta

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.model.TopLevelTab

data class TabItem(
    val tab: TopLevelTab,
    @StringRes val labelRes: Int,
    val icon: ImageVector,
)

val APP_TABS = listOf(
    TabItem(
        tab = TopLevelTab.HOME,
        labelRes = R.string.app_tab_home,
        icon = Icons.Default.Home,
    ),
    TabItem(
        tab = TopLevelTab.CHATS,
        labelRes = R.string.app_tab_chats,
        icon = Icons.AutoMirrored.Filled.Chat,
    ),
    TabItem(
        tab = TopLevelTab.BOARD,
        labelRes = R.string.app_tab_board,
        icon = Icons.Default.ViewKanban,
    ),
    TabItem(
        tab = TopLevelTab.MACHINES,
        labelRes = R.string.app_tab_machines,
        icon = Icons.Default.Build,
    ),
    TabItem(
        tab = TopLevelTab.ME,
        labelRes = R.string.app_tab_me,
        icon = Icons.Default.Person,
    ),
)

@Composable
fun AppBottomBar(
    currentTab: TopLevelTab,
    onTabSelected: (TopLevelTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    // M3 NavigationBar forces an 80dp min height, which leaves a wide gap under the labels.
    // Same surface and insets (applied once), but a compact 68dp row (~8dp under the labels).
    Surface(
        modifier = modifier,
        color = NavigationBarDefaults.containerColor,
        tonalElevation = NavigationBarDefaults.Elevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(NavigationBarDefaults.windowInsets)
                .height(68.dp)
                .selectableGroup(),
        ) {
        for (item in APP_TABS) {
            val selected = currentTab == item.tab
            val label = stringResource(item.labelRes)
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    BadgedBox(
                        badge = {
                            // Badge slot for later (e.g. unread count or notifications)
                        },
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label,
                        )
                    }
                },
                label = { Text(text = label) },
            )
        }
        }
    }
}
