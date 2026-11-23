package com.techtsunagu.wakatta.feature.notices

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.NoticeCategory
import com.techtsunagu.wakatta.core.model.NoticeSourceKind
import com.techtsunagu.wakatta.core.model.NoticeStatus
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.core.model.WorkerInfo
import com.techtsunagu.wakatta.feature.notices.model.CheckQuestionDto
import com.techtsunagu.wakatta.feature.notices.model.CheckQuestionWithAnswer
import com.techtsunagu.wakatta.feature.notices.model.EventDto
import com.techtsunagu.wakatta.feature.notices.model.NoticeDraftDto
import com.techtsunagu.wakatta.feature.notices.model.NoticeForPoster
import com.techtsunagu.wakatta.feature.notices.model.NoticeForWorker
import com.techtsunagu.wakatta.feature.notices.model.NoticeWorkerStatus
import com.techtsunagu.wakatta.feature.notices.model.StepDto
import com.techtsunagu.wakatta.feature.notices.model.TodayWordInfo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34], qualifiers = "w412dp-h2000dp")
class NoticesComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // --- LifeContent tests ---

    @Test
    fun lifeContentLoadingState() {
        composeTestRule.setContent {
            LifeContent(
                state = LifeUiState(isLoading = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun lifeContentErrorState() {
        composeTestRule.setContent {
            LifeContent(
                state = LifeUiState(error = "Network error"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Network error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun lifeContentEmptyState() {
        composeTestRule.setContent {
            LifeContent(
                state = LifeUiState(
                    isLoading = false,
                    isForeman = false,
                    upcomingEvents = emptyList(),
                    notices = emptyList(),
                ),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("No upcoming events").assertIsDisplayed()
        composeTestRule.onNodeWithText("No notices found").assertIsDisplayed()
    }

    @Test
    fun lifeContentSuccessStateWorker() {
        val testEvent = EventDto(
            id = "ev_1",
            noticeId = "n_1",
            title = mapOf("en" to "Garbage Day"),
            date = "2026-11-26",
            time = "8:00",
        )
        val testNotice = NoticeForWorker(
            id = "n_1",
            title = mapOf("en" to "Burnable Garbage"),
            category = NoticeCategory.GARBAGE,
            sourceKind = NoticeSourceKind.TEXT,
            from = "Neighborhood Association",
            publishedAt = "2026-11-20",
            question = CheckQuestionDto(
                id = "q_1",
                prompt = mapOf("en" to "What time?"),
                options = listOf(mapOf("en" to "8:00")),
            ),
            wakattaStatus = WakattaStatus.WAIT,
        )

        composeTestRule.setContent {
            LifeContent(
                state = LifeUiState(
                    isLoading = false,
                    isForeman = false,
                    userLanguage = "en",
                    upcomingEvents = listOf(testEvent),
                    notices = listOf(testNotice),
                    pendingNoticeCount = 1,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("This week & coming up").assertExists()
        composeTestRule.onNodeWithText("Garbage Day").assertExists()
        composeTestRule.onNodeWithText("Burnable Garbage").assertExists()
        composeTestRule.onNodeWithText("Ask about life here").assertExists()
        composeTestRule.onNodeWithText("My Senpai").assertExists()
    }

    @Test
    fun lifeContentSuccessStateForeman() {
        val testPosterNotice = NoticeForPoster(
            id = "n_p_1",
            title = mapOf("ja" to "防災訓練のお知らせ"),
            category = NoticeCategory.DISASTER,
            sourceKind = NoticeSourceKind.PHOTO,
            from = "自治会",
            publishedAt = "2026-11-20",
            question = CheckQuestionWithAnswer(
                id = "q_1",
                prompt = mapOf("ja" to "場所は？"),
                options = listOf(mapOf("ja" to "公民館")),
                correctIndex = 0,
            ),
            status = NoticeStatus.PUBLISHED,
            workerStatuses = listOf(
                NoticeWorkerStatus(WorkerInfo("1", "Rahim", "bn"), WakattaStatus.GREEN, 0, "公民館")
            ),
        )

        composeTestRule.setContent {
            LifeContent(
                state = LifeUiState(
                    isLoading = false,
                    isForeman = true,
                    userLanguage = "ja",
                    posterNotices = listOf(testPosterNotice),
                    todayWord = TodayWordInfo(
                        term = "ধন্যবাদ",
                        reading = "dhonnobad",
                        meaning = mapOf("ja" to "ありがとう"),
                        workerName = "Rahim",
                        languageName = "ベンガル語",
                    ),
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Published notices").assertExists()
        composeTestRule.onNodeWithText("防災訓練のお知らせ").assertExists()
        composeTestRule.onNodeWithText("ধন্যবাদ").assertExists()
        composeTestRule.onNodeWithText("Senpai & Support").assertExists()
    }

    // --- NoticeContent tests ---

    @Test
    fun noticeContentLoadingState() {
        composeTestRule.setContent {
            NoticeContent(
                state = NoticeUiState(isLoading = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun noticeContentErrorState() {
        composeTestRule.setContent {
            NoticeContent(
                state = NoticeUiState(error = "Notice not found"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Notice not found").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun noticeContentEmptyState() {
        composeTestRule.setContent {
            NoticeContent(
                state = NoticeUiState(isLoading = false, workerNotice = null, posterNotice = null),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("No notices found").assertIsDisplayed()
    }

    @Test
    fun noticeContentSuccessStateWorker() {
        val notice = NoticeForWorker(
            id = "n_1",
            title = mapOf("en" to "Disaster Drill Notice"),
            category = NoticeCategory.DISASTER,
            sourceKind = NoticeSourceKind.TEXT,
            sourceText = "Drill starts at 9:00 at community hall.",
            from = "Neighborhood Association",
            publishedAt = "2026-11-20",
            steps = listOf(
                StepDto(text = mapOf("en" to "Go to the community hall at 9:00."))
            ),
            events = listOf(
                EventDto("ev_1", "n_1", mapOf("en" to "Drill Day"), "2026-12-07", "9:00")
            ),
            question = CheckQuestionDto(
                id = "q_1",
                prompt = mapOf("en" to "Where is the drill?"),
                options = listOf(mapOf("en" to "Community hall"), mapOf("en" to "Station")),
            ),
        )

        composeTestRule.setContent {
            NoticeContent(
                state = NoticeUiState(
                    isLoading = false,
                    isForeman = false,
                    userLanguage = "en",
                    workerNotice = notice,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Disaster Drill Notice").assertExists()
        composeTestRule.onNodeWithText("What to do").assertExists()
        composeTestRule.onNodeWithText("Go to the community hall at 9:00.").assertExists()
        composeTestRule.onNodeWithText("Dates").assertExists()
        composeTestRule.onNodeWithText("Where is the drill?").assertExists()
        composeTestRule.onNodeWithText("Community hall").assertExists()
    }

    @Test
    fun noticeContentSuccessStateForeman() {
        val posterNotice = NoticeForPoster(
            id = "n_p_1",
            title = mapOf("ja" to "秋祭りのお知らせ"),
            category = NoticeCategory.EVENT,
            sourceKind = NoticeSourceKind.TEXT,
            sourceText = "神社で秋祭りを行います。",
            from = "自治会",
            publishedAt = "2026-11-20",
            steps = listOf(
                StepDto(text = mapOf("ja" to "神社に行きます。"))
            ),
            question = CheckQuestionWithAnswer(
                id = "q_1",
                prompt = mapOf("ja" to "場所はどこですか？"),
                options = listOf(mapOf("ja" to "神社"), mapOf("ja" to "駅")),
                correctIndex = 0,
            ),
            workerStatuses = listOf(
                NoticeWorkerStatus(WorkerInfo("1", "Rahim", "bn"), WakattaStatus.GREEN, 0, "神社")
            ),
        )

        composeTestRule.setContent {
            NoticeContent(
                state = NoticeUiState(
                    isLoading = false,
                    isForeman = true,
                    userLanguage = "ja",
                    posterNotice = posterNotice,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("秋祭りのお知らせ").assertExists()
        composeTestRule.onNodeWithText("Check Results").assertExists()
        composeTestRule.onNodeWithText("Rahim").assertExists()
    }

    // --- NoticeNewContent tests ---

    @Test
    fun noticeNewContentPickStage() {
        composeTestRule.setContent {
            NoticeNewContent(
                state = NoticeNewUiState(stage = NoticeNewStage.PICK),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Choose a sample flyer").assertIsDisplayed()
        composeTestRule.onNodeWithText("町内一斉清掃").assertIsDisplayed()
    }

    @Test
    fun noticeNewContentBusyStage() {
        composeTestRule.setContent {
            NoticeNewContent(
                state = NoticeNewUiState(
                    stage = NoticeNewStage.BUSY,
                    busyMessage = "AI: Processing flyer...",
                ),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("AI: Processing flyer...").assertIsDisplayed()
    }

    @Test
    fun noticeNewContentReviewStage() {
        val draft = NoticeDraftDto(
            id = "draft_1",
            sourceKind = NoticeSourceKind.TEXT,
            sourceText = "大掃除のお知らせ",
            titleJa = "大掃除",
            titleEn = "Clean up",
            stepsJa = listOf("東公園に集まります。"),
            stepsEn = listOf("Gather at East Park."),
            events = listOf(
                EventDto("ev_1", "draft_1", mapOf("ja" to "大掃除の日"), "2026-12-13", "8:00", confirmed = true)
            ),
            question = CheckQuestionWithAnswer(
                id = "q_1",
                prompt = mapOf("ja" to "何時？"),
                options = listOf(mapOf("ja" to "8:00")),
                correctIndex = 0,
            ),
        )

        composeTestRule.setContent {
            NoticeNewContent(
                state = NoticeNewUiState(
                    stage = NoticeNewStage.REVIEW,
                    draft = draft,
                    editableTitle = "大掃除",
                    events = draft.events,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Title").assertExists()
        composeTestRule.onNodeWithText("Easy steps (Japanese / English)").assertExists()
        composeTestRule.onNodeWithText("Found dates (confirm with checkbox)").assertExists()
        composeTestRule.onNodeWithText("Publish notice").assertExists()
    }

    @Test
    fun noticeNewContentPublishSuccess() {
        composeTestRule.setContent {
            NoticeNewContent(
                state = NoticeNewUiState(
                    publishSuccess = true,
                    publishedNoticeId = "n_published_123",
                ),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Notice published successfully").assertIsDisplayed()
    }
}
