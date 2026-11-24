package com.techtsunagu.wakatta

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme
import com.techtsunagu.wakatta.core.model.TopLevelTab
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class BottomBarComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomBarShowsFiveTabs() {
        composeTestRule.setContent {
            WakattaTheme {
                AppBottomBar(
                    currentTab = TopLevelTab.HOME,
                    onTabSelected = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Board").assertIsDisplayed()
        composeTestRule.onNodeWithText("Machines").assertIsDisplayed()
        composeTestRule.onNodeWithText("Me").assertIsDisplayed()
    }
}
