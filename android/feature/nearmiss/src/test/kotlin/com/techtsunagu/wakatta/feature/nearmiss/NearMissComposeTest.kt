package com.techtsunagu.wakatta.feature.nearmiss

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissDto
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissSeverity
import com.techtsunagu.wakatta.feature.nearmiss.domain.NearMissStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class NearMissComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- NearMissNewContent Tests ---

    @Test
    fun rendersNearMissNewContentLoading() {
        composeTestRule.setContent {
            NearMissNewContent(
                state = NearMissNewUiState(isLoading = true, machines = emptyList()),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissNewContentError() {
        composeTestRule.setContent {
            NearMissNewContent(
                state = NearMissNewUiState(error = "Network timeout"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Error: Network timeout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissNewContentForemanPlaceholder() {
        composeTestRule.setContent {
            NearMissNewContent(
                state = NearMissNewUiState(isForeman = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Near-miss reports arrive from workers' phones").assertIsDisplayed()
        composeTestRule.onNodeWithText("View reports list").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissNewContentSentSuccess() {
        composeTestRule.setContent {
            NearMissNewContent(
                state = NearMissNewUiState(isSent = true, foremanName = "Tanaka"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Sent to Tanaka").assertIsDisplayed()
        composeTestRule.onNodeWithText("He gets a notice now. Thank you for speaking up.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
        composeTestRule.onNodeWithText("Report another").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissNewContentForm() {
        composeTestRule.setContent {
            NearMissNewContent(
                state = NearMissNewUiState(
                    machines = DemoSeed.machines,
                    machineId = "mc1",
                    text = "A metal chip flew out",
                    foremanName = "Tanaka",
                ),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("1 · Which machine?").assertExists()
        composeTestRule.onNodeWithText("2 · What happened? Hold to speak").assertExists()
        composeTestRule.onNodeWithText("3 · Photo (optional)").assertExists()
        composeTestRule.onNodeWithText("4 · How serious?").assertExists()
        composeTestRule.onNodeWithText("Send to Tanaka").assertExists()
    }

    // --- NearMissListContent Tests ---

    @Test
    fun rendersNearMissListContentLoading() {
        composeTestRule.setContent {
            NearMissListContent(
                state = NearMissListUiState(isLoading = true, allReports = emptyList()),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissListContentError() {
        composeTestRule.setContent {
            NearMissListContent(
                state = NearMissListUiState(error = "Connection failed"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Error: Connection failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissListContentEmptyOpen() {
        composeTestRule.setContent {
            NearMissListContent(
                state = NearMissListUiState(isEmpty = true, filter = NearMissStatus.OPEN),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("No open reports").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissListContentEmptySeen() {
        composeTestRule.setContent {
            NearMissListContent(
                state = NearMissListUiState(isEmpty = true, filter = NearMissStatus.ACK),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Nothing seen yet").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissListContentWithReports() {
        val sampleReport = NearMissDto(
            id = "nm_1",
            userId = "u_rahim",
            machineId = "mc1",
            text = mapOf("en" to "Sample near-miss event", "ja" to "サンプルのヒヤリハット"),
            severity = NearMissSeverity.MID,
            createdAt = "2026-11-23T10:00:00Z",
            status = NearMissStatus.OPEN,
        )
        composeTestRule.setContent {
            NearMissListContent(
                state = NearMissListUiState(
                    displayedReports = listOf(sampleReport),
                    allReports = listOf(sampleReport),
                    openCount = 1,
                    seenCount = 0,
                    userLang = "en",
                ),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Open 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Seen 0").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sample near-miss event").assertIsDisplayed()
    }

    // --- NearMissDetailContent Tests ---

    @Test
    fun rendersNearMissDetailContentLoading() {
        composeTestRule.setContent {
            NearMissDetailContent(
                state = NearMissDetailUiState(isLoading = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissDetailContentNotFound() {
        composeTestRule.setContent {
            NearMissDetailContent(
                state = NearMissDetailUiState(report = null, isLoading = false),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Report not found").assertIsDisplayed()
    }

    @Test
    fun rendersNearMissDetailContentSuccessForeman() {
        val sampleReport = NearMissDto(
            id = "nm_detail_1",
            userId = "u_rahim",
            machineId = "mc1",
            text = mapOf("en" to "Chuck cover opened unexpectedly", "ja" to "チャックカバーが突然開いた"),
            severity = NearMissSeverity.HIGH,
            createdAt = "2026-11-23T10:00:00Z",
            status = NearMissStatus.OPEN,
        )
        composeTestRule.setContent {
            NearMissDetailContent(
                state = NearMissDetailUiState(
                    report = sampleReport,
                    isForeman = true,
                    worker = WorkerInfo("u_rahim", "Rahim Uddin", "bn"),
                    machine = DemoSeed.machines[0],
                ),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Japanese translation").assertExists()
        composeTestRule.onNodeWithText("チャックカバーが突然開いた").assertExists()
        composeTestRule.onNodeWithText("Original text").assertExists()
        composeTestRule.onNodeWithText("Chuck cover opened unexpectedly").assertExists()
        composeTestRule.onNodeWithText("Create Task").assertExists()
        composeTestRule.onNodeWithText("Acknowledge").assertExists()
    }

    @Test
    fun rendersNearMissDetailContentSuccessWorker() {
        val sampleReport = NearMissDto(
            id = "nm_detail_2",
            userId = "u_rahim",
            machineId = "mc1",
            text = mapOf("en" to "Chuck cover opened unexpectedly", "ja" to "チャックカバーが突然開いた"),
            severity = NearMissSeverity.MID,
            createdAt = "2026-11-23T10:00:00Z",
            status = NearMissStatus.OPEN,
        )
        composeTestRule.setContent {
            NearMissDetailContent(
                state = NearMissDetailUiState(
                    report = sampleReport,
                    isForeman = false,
                    foremanName = "Tanaka",
                    userLang = "en",
                ),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("What you said").assertExists()
        composeTestRule.onNodeWithText("Chuck cover opened unexpectedly").assertExists()
        composeTestRule.onNodeWithText("What Tanaka reads (Japanese)").assertExists()
        composeTestRule.onNodeWithText("チャックカバーが突然開いた").assertExists()
    }
}
