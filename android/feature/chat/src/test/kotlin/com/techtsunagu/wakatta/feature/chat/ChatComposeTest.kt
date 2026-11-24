package com.techtsunagu.wakatta.feature.chat

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.MessageKind
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel
import com.techtsunagu.wakatta.feature.chat.model.CheckQuestionForSender
import com.techtsunagu.wakatta.feature.chat.model.CheckQuestionForWorker
import com.techtsunagu.wakatta.feature.chat.model.ThreadUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ChatComposeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // ---------------- ChatsContent Tests ----------------

    @Test
    fun rendersChatsContent_loading() {
        composeTestRule.setContent {
            ChatsContent(
                state = ChatsUiState(isLoading = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun rendersChatsContent_empty() {
        composeTestRule.setContent {
            ChatsContent(
                state = ChatsUiState(isEmpty = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("No chats yet").assertIsDisplayed()
    }

    @Test
    fun rendersChatsContent_error() {
        composeTestRule.setContent {
            ChatsContent(
                state = ChatsUiState(error = "Network timeout"),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Network timeout").assertIsDisplayed()
    }

    @Test
    fun rendersChatsContent_success() {
        val sampleThreads = listOf(
            ThreadUiModel(
                id = "t_line2",
                title = mapOf("ja" to "Line 2 · プレス班", "en" to "Line 2 · Press team"),
                lastMessage = "今日は3号機の油の点検を先にしてください。",
                lastMessageAt = "08:02",
                unreadCount = 2,
                worstStatus = WakattaStatus.ORANGE,
                isGroup = true,
                membersCount = 4,
            ),
        )

        composeTestRule.setContent {
            ChatsContent(
                state = ChatsUiState(threads = sampleThreads),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Line 2 · プレス班").assertIsDisplayed()
        composeTestRule.onNodeWithText("今日は3号機の油の点検を先にしてください。").assertIsDisplayed()
        composeTestRule.onNodeWithText("08:02").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    // ---------------- ChatContent Tests ----------------

    @Test
    fun rendersChatContent_loading() {
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(isLoading = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun rendersChatContent_empty() {
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(isEmpty = true),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("No messages").assertIsDisplayed()
    }

    @Test
    fun rendersChatContent_error() {
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(error = AppError.Offline),
                onEvent = {},
            )
        }
        composeTestRule.onNodeWithText("No connection. Your message is still here: try again.").assertIsDisplayed()
    }

    @Test
    fun rendersChatContent_success_foreman() {
        val sampleMessages = listOf(
            ChatMessageUiModel(
                id = "m1",
                threadId = "t_line2",
                fromUserId = "u_tanaka",
                fromUserName = "田中 健一 (Foreman)",
                isMine = true,
                kind = MessageKind.VOICE,
                text = mapOf("ja" to "3号機の油圧オイルを点検してください。"),
                originalText = "3号機の油圧オイルを点検してください。",
                translatedText = "Check hydraulic oil on Machine 3.",
                time = "08:02",
                isVoice = true,
                hasCheck = true,
                workerStatuses = mapOf("u_rahim" to WakattaStatus.GREEN),
                stage = 5,
            ),
        )

        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(
                    title = "Line 2 · プレス班",
                    currentRole = Role.FOREMAN,
                    canSwitchRole = true,
                    messages = sampleMessages,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Line 2 · プレス班").assertIsDisplayed()
        composeTestRule.onNodeWithText("3号機の油圧オイルを点検してください。").assertIsDisplayed()
        composeTestRule.onNodeWithText("Foreman").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 Understood").assertIsDisplayed()
    }

    @Test
    fun rendersChatContent_success_worker_withCheck() {
        val q1 = CheckQuestionForWorker(
            id = "q1",
            prompt = mapOf("en" to "What do you check first?"),
            options = listOf(mapOf("en" to "Machine 3 Oil"), mapOf("en" to "Safety Shoes")),
        )

        val sampleMessages = listOf(
            ChatMessageUiModel(
                id = "m1",
                threadId = "t_line2",
                fromUserId = "u_tanaka",
                fromUserName = "田中 健一 (Foreman)",
                isMine = false,
                kind = MessageKind.TEXT,
                text = mapOf("en" to "Check the oil first.", "ja" to "油を先に点検してください。"),
                originalText = "油を先に点検してください。",
                translatedText = "Check the oil first.",
                time = "08:02",
                hasCheck = true,
                workerQuestions = listOf(q1),
            ),
        )

        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(
                    title = "Line 2 · プレス班",
                    currentRole = Role.WORKER,
                    canSwitchRole = true,
                    messages = sampleMessages,
                ),
                onEvent = {},
            )
        }

        composeTestRule.onNodeWithText("Line 2 · プレス班").assertIsDisplayed()
        composeTestRule.onNodeWithText("油を先に点検してください。").assertExists() // the original shows; the translation sits under "Show translation"
        composeTestRule.onNodeWithText("Worker").assertIsDisplayed()
        composeTestRule.onNodeWithText("1 quick question").assertIsDisplayed()
        composeTestRule.onNodeWithText("Machine 3 Oil").assertExists()
        composeTestRule.onNodeWithText("Not sure").assertExists()
    }

    @Test
    fun chatOpensAtTheNewestMessage() {
        val many = (1..50).map { i ->
            ChatMessageUiModel(
                id = "m$i",
                threadId = "t1",
                fromUserId = "u1",
                fromUserName = "Rahim",
                isMine = false,
                kind = MessageKind.TEXT,
                text = mapOf("ja" to "メッセージ $i"),
                originalText = "メッセージ $i",
                translatedText = "Message $i",
                time = "08:00",
                stage = 5,
            )
        }
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(threadId = "t1", title = "Line 2", currentRole = Role.WORKER, messages = many),
                onEvent = {},
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("50", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("ジ 1", substring = true).assertDoesNotExist()
    }

    @Test
    fun foremanCanNudgeAWorkerOnceTheMessageIsSent() {
        val sent = ChatMessageUiModel(
            id = "m1",
            threadId = "t1",
            fromUserId = "u_tanaka",
            fromUserName = "Tanaka",
            isMine = true,
            kind = MessageKind.TEXT,
            text = mapOf("ja" to "点検してください"),
            originalText = "点検してください",
            translatedText = "Please inspect",
            time = "08:00",
            hasCheck = true,
            workerStatuses = mapOf("u_rahim" to WakattaStatus.WAIT),
            stage = 4,
            isWkFoldExpanded = true,
        )
        composeTestRule.setContent {
            ChatContent(state = ChatUiState(threadId = "t1", title = "Line 2", currentRole = Role.FOREMAN, messages = listOf(sent)), onEvent = {})
        }

        composeTestRule.onNodeWithText("Nudge").assertExists()
    }

    // ---------------- Read-aloud for every message (M9) ----------------

    @Test
    fun aTextMessageIsReadAloudInItsOwnLanguage() {
        val events = mutableListOf<ChatEvent>()
        val message = ChatMessageUiModel(
            id = "m1",
            threadId = "t1",
            fromUserId = "u_rahim",
            fromUserName = "Rahim",
            isMine = false,
            kind = MessageKind.TEXT,
            text = mapOf("en" to "The press is making a noise.", "ja" to "プレスから音がします。"),
            originalText = "The press is making a noise.",
            translatedText = "プレスから音がします。",
            originalLang = "en",
            translatedLang = "ja",
            time = "09:00",
        )
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(threadId = "t1", title = "Line 2", currentRole = Role.FOREMAN, messages = listOf(message)),
                onEvent = { events += it },
            )
        }
        composeTestRule.onNodeWithContentDescription("Read aloud").performClick()
        assertEquals(ChatEvent.PlayVoice("The press is making a noise.", "en"), events.last())
    }

    // ---------------- Draft review card (M2) ----------------

    private fun draft(needsEdit: Boolean = false, en: String? = "Check the oil on Machine 3.") = ChatMessageUiModel(
        id = "d1",
        threadId = "t1",
        fromUserId = "u_tanaka",
        fromUserName = "Tanaka",
        isMine = true,
        kind = MessageKind.TEXT,
        text = listOfNotNull("ja" to "3号機の油を点検してください。", en?.let { "en" to it }).toMap(),
        originalText = "3号機の油を点検してください。",
        translatedText = en ?: "3号機の油を点検してください。",
        time = "08:00",
        stage = if (needsEdit) 0 else 3,
        isDraft = true,
        needsEdit = needsEdit,
        hasCheck = true,
        senderQuestions = listOf(
            CheckQuestionForSender(
                id = "q1",
                prompt = mapOf("en" to "Which machine?"),
                options = listOf(mapOf("en" to "Machine 3"), mapOf("en" to "Machine 5")),
                correctIndex = 0,
            ),
        ),
    )

    private fun showDraft(message: ChatMessageUiModel, events: MutableList<ChatEvent>) {
        composeTestRule.setContent {
            ChatContent(
                state = ChatUiState(threadId = "t1", title = "Line 2", currentRole = Role.FOREMAN, messages = listOf(message)),
                onEvent = { events += it },
            )
        }
    }

    @Test
    fun foremanReviewsTheDraftAndSendsIt() {
        val events = mutableListOf<ChatEvent>()
        showDraft(draft(), events)

        composeTestRule.onNodeWithText("Check before sending").assertExists()
        composeTestRule.onNodeWithText("Check the oil on Machine 3.").assertExists()
        composeTestRule.onNodeWithText("Which machine?", substring = true).assertExists()
        composeTestRule.onNodeWithText("Not translated", substring = true).assertDoesNotExist()

        composeTestRule.onNodeWithText("Send").performClick()
        assertEquals(listOf<ChatEvent>(ChatEvent.SendDraft("d1")), events)
    }

    @Test
    fun anUntranslatedDraftSaysSoAndItsSendIsJapaneseOnly() {
        val events = mutableListOf<ChatEvent>()
        showDraft(draft(needsEdit = true, en = null), events)

        composeTestRule.onNodeWithText("Not translated", substring = true).assertExists()
        composeTestRule.onNodeWithText("Send").assertDoesNotExist()
        composeTestRule.onNodeWithText("Send in Japanese only").performClick()
        assertEquals(listOf<ChatEvent>(ChatEvent.SendDraft("d1")), events)
    }

    @Test
    fun anUnsentDraftIsLabelledNotSent_andAnUntranslatedOneJapaneseOnly() { // M4
        showDraft(draft(needsEdit = true, en = null), mutableListOf())
        composeTestRule.onNodeWithText("Draft, not sent · Japanese only · 08:00").assertExists()
    }

    @Test
    fun aTranslatedDraftIsLabelledNotSentOnly() { // M4
        showDraft(draft(), mutableListOf())
        composeTestRule.onNodeWithText("Draft, not sent · 08:00").assertExists()
    }

    @Test
    fun foremanEditsTheDraftsJapanese() {
        val events = mutableListOf<ChatEvent>()
        showDraft(draft(needsEdit = true, en = null), events)

        composeTestRule.onNodeWithText("Edit").performClick()
        composeTestRule.onNode(hasSetTextAction() and hasText("Japanese")).performTextReplacement(" ")
        composeTestRule.onNodeWithText("Save").assertIsNotEnabled()
        composeTestRule.onNode(hasSetTextAction() and hasText("Japanese")).performTextReplacement("5号機の油を点検してください。")
        composeTestRule.onNodeWithText("Save").performClick()

        assertEquals(listOf<ChatEvent>(ChatEvent.EditDraft("d1", "5号機の油を点検してください。")), events)
    }
}
