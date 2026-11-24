package com.techtsunagu.wakatta.feature.chat

import com.techtsunagu.wakatta.core.model.FollowUpStatus
import com.techtsunagu.wakatta.core.model.WakattaStatus
import com.techtsunagu.wakatta.feature.chat.data.FakeChatRepository
import com.techtsunagu.wakatta.feature.chat.model.ReexplainRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeChatRepositoryTest {

    private lateinit var repository: FakeChatRepository

    @Before
    fun setUp() {
        repository = FakeChatRepository().apply { setLatency(0L) }
    }

    @Test
    fun getThreadsReturnsSeededData() = runTest {
        val threads = repository.getThreads().first()
        assertEquals(3, threads.size)
        assertTrue(threads.any { it.id == "t_line2" })
    }

    @Test
    fun getMessagesReturnsSeededMessages() = runTest {
        val messages = repository.getMessages("t_line2").first()
        assertTrue(messages.size >= 4)
        val m1 = messages.first { it.id == "m1" }
        assertTrue(m1.isVoice)
        assertTrue(m1.hasCheck)
    }

    @Test
    fun sendMessageAddsNewMessageToThread() = runTest {
        val res = repository.sendMessage(
            threadId = "t_line2",
            text = "Testing message send",
            isVoice = false,
            fromUserId = "u_tanaka",
            idempotencyKey = "key-1",
        )
        assertTrue(res.isSuccess)
        val messages = repository.getMessages("t_line2").first()
        assertTrue(messages.any { it.text["ja"] == "Testing message send" })
    }

    @Test
    fun gradingRuleAllCorrectReturnsGreen() = runTest {
        // m1 correct answer is index 0
        val res = repository.submitAnswers(
            messageId = "m1",
            userId = "u_rahim",
            answers = listOf(0),
            idempotencyKey = "key-2",
        )
        assertTrue(res.isSuccess)
        val data = res.getOrNull()
        assertNotNull(data)
        assertEquals(WakattaStatus.GREEN, data?.status)
        assertTrue(data?.followUpIds?.isEmpty() == true)
    }

    @Test
    fun gradingRuleNoneCorrectReturnsRedAndFollowUp() = runTest {
        // m1 correct answer is 0. Answering with 1 is wrong.
        val res = repository.submitAnswers(
            messageId = "m1",
            userId = "u_siti",
            answers = listOf(1),
            idempotencyKey = "key-3",
        )
        assertTrue(res.isSuccess)
        val data = res.getOrNull()
        assertNotNull(data)
        assertEquals(WakattaStatus.RED, data?.status)
        assertFalse(data?.followUpIds?.isEmpty() == true)

        val followUps = repository.getFollowUps().getOrNull()
        assertTrue(followUps?.any { it.messageId == "m1" && it.status == FollowUpStatus.OPEN } == true)
    }

    @Test
    fun reexplainAppendsMessageAndResolvesFollowUp() = runTest {
        val req = ReexplainRequest(
            userId = "u_siti",
            termJa = "油圧オイル",
            easyJa = "やさしい説明です",
            explanation = mapOf("en" to "Easy explanation"),
            newQuestionPrompt = mapOf("ja" to "新しい質問？", "en" to "New question?"),
            newQuestionOptions = listOf(mapOf("ja" to "選択肢A", "en" to "Option A")),
            newQuestionCorrectIndex = 0,
        )

        val res = repository.reexplain("m1", req)
        assertTrue(res.isSuccess)

        val messages = repository.getMessages("t_line2").first()
        assertTrue(messages.any { it.kind == com.techtsunagu.wakatta.core.model.MessageKind.REEXPLAIN })
    }

    @Test
    fun nudgeMarksMessageNudged() = runTest {
        val res = repository.nudge("m1", "u_siti")
        assertTrue(res.isSuccess)

        val messages = repository.getMessages("t_line2").first()
        val m1 = messages.first { it.id == "m1" }
        assertTrue(m1.isNudged)
    }

    @Test
    fun glossaryAddAndRetrieveTerms() = runTest {
        val initial = repository.getTerms().getOrNull() ?: emptyList()
        val res = repository.addTerm("旋盤", "せんばん", "লেদ", "Lathe")
        assertTrue(res.isSuccess)

        val updated = repository.getTerms().getOrNull() ?: emptyList()
        assertEquals(initial.size + 1, updated.size)
        assertTrue(updated.any { it.ja == "旋盤" })
    }

    @Test
    fun reexplainDraftUsesTheFollowUpAndFailsWithoutOne() = runTest {
        val ok = repository.getReexplainDraft("m1", "u_siti")
        assertEquals("Siti Rahma", (ok as com.techtsunagu.wakatta.core.model.AppResult.Success).value.workerName)

        val missing = repository.getReexplainDraft("m1", "u_nobody")
        assertEquals(com.techtsunagu.wakatta.core.model.AppError.NotFound, missing.errorOrNull())
    }
}
