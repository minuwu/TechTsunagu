package com.techtsunagu.wakatta.feature.asks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.feature.asks.model.AskDto
import com.techtsunagu.wakatta.feature.asks.model.AskKind
import com.techtsunagu.wakatta.feature.asks.model.AskStatus
import com.techtsunagu.wakatta.feature.asks.model.MeetupDto
import com.techtsunagu.wakatta.feature.asks.model.NoticeMatchDto
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairDto
import com.techtsunagu.wakatta.feature.asks.model.TodayWordDto
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w411dp-h1000dp")
class AsksComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun askContentRendersLoading() {
        composeTestRule.setContent {
            AskContent(
                state = AskUiState(isLoading = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun askContentRendersError() {
        composeTestRule.setContent {
            AskContent(
                state = AskUiState(error = "Network timeout"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Error: Network timeout").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun askContentRendersEmpty() {
        composeTestRule.setContent {
            AskContent(
                state = AskUiState(isEmpty = true, workerName = "Rahim"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Hi Rahim. Ask me about garbage days, drills, festivals and local rules. If the notices do not cover it, I will ask your Senpai.").assertIsDisplayed()
    }

    @Test
    fun askContentRendersWorkerSuccess() {
        val sampleAsk = AskDto(
            id = "ak1",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            text = "Where do I throw garbage?",
            kind = AskKind.TEXT,
            covered = true,
            matchNotice = NoticeMatchDto(
                id = "n_burn",
                title = mapOf("ja" to "燃えるごみ", "en" to "Burnable garbage"),
                answer = mapOf("ja" to "月・木 8:00まで", "en" to "Mon and Thu, by 8:00"),
                snippet = mapOf("ja" to "ごみ置き場に出します", "en" to "Take it to the station"),
            ),
            sourceNoticeIds = listOf("n_burn"),
            status = AskStatus.ANSWERED,
            createdAt = "11/24 08:00",
        )

        composeTestRule.setContent {
            AskContent(
                state = AskUiState(asks = listOf(sampleAsk)),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Where do I throw garbage?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Mon and Thu, by 8:00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Open notice").assertIsDisplayed()
    }

    @Test
    fun askContentRendersForemanSuccess() {
        val sampleForwarded = AskDto(
            id = "ak_fwd",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            text = "Where is the clinic?",
            textJa = "病院はどこですか？",
            kind = AskKind.TEXT,
            covered = false,
            status = AskStatus.FORWARDED,
            createdAt = "11/24 09:00",
        )

        composeTestRule.setContent {
            AskContent(
                state = AskUiState(
                    isForeman = true,
                    asks = listOf(sampleForwarded),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Asks (To Senpai)").assertIsDisplayed()
        composeTestRule.onNodeWithText("病院はどこですか？").assertIsDisplayed()
        composeTestRule.onNodeWithText("いい質問ですね。明日の昼休みに、いっしょに調べましょう。").assertIsDisplayed()
    }

    @Test
    fun senpaiContentRendersLoading() {
        composeTestRule.setContent {
            SenpaiContent(
                state = SenpaiUiState(isLoading = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun senpaiContentRendersError() {
        composeTestRule.setContent {
            SenpaiContent(
                state = SenpaiUiState(error = "Database connection failed"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Error: Database connection failed").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun senpaiContentRendersEmpty() {
        composeTestRule.setContent {
            SenpaiContent(
                state = SenpaiUiState(isEmpty = true, meetups = emptyList()),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("No meet-ups yet").assertIsDisplayed()
    }

    @Test
    fun senpaiContentRendersWorkerSuccess() {
        val pair = SenpaiPairDto(
            id = "sp1",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            senpaiName = "佐藤 美咲",
            senpaiEnName = "Sato Misaki",
            meetupCount = 2,
        )
        val meetup = MeetupDto(
            id = "mu1",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            senpaiId = "u_sato",
            senpaiName = "佐藤 美咲",
            kind = "coffee",
            date = "2026-11-10",
            note = "Japanese practice at cafe",
        )

        composeTestRule.setContent {
            SenpaiContent(
                state = SenpaiUiState(
                    pair = pair,
                    meetups = listOf(meetup),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Sato Misaki").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ask a question").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log a meet-up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Japanese practice at cafe").assertIsDisplayed()
    }

    @Test
    fun senpaiContentRendersForemanSuccess() {
        val word = TodayWordDto(
            workerId = "u_rahim",
            workerName = "Rahim",
            languageName = "ベンガル語",
            word = "ধন্যবাদ",
            reading = "dhonnobad",
            meaningJa = "ありがとう",
            meaningEn = "Thank you",
        )
        val pair = SenpaiPairDto(
            id = "sp1",
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            senpaiName = "佐藤 美咲",
            meetupCount = 3,
            lastMeetupDate = "2026-11-15",
        )

        composeTestRule.setContent {
            SenpaiContent(
                state = SenpaiUiState(
                    isForeman = true,
                    todayWord = word,
                    pairs = listOf(pair),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("ধন্যবাদ").assertIsDisplayed()
        composeTestRule.onNodeWithText("dhonnobad = ありがとう").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create invitation link").assertIsDisplayed()
    }
}
