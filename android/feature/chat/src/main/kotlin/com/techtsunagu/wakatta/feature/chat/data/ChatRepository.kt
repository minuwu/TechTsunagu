package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.GlossaryTerm
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.ThreadSummary
import com.techtsunagu.wakatta.feature.chat.model.AnswerResultDto
import com.techtsunagu.wakatta.feature.chat.model.ChatMessageUiModel
import com.techtsunagu.wakatta.feature.chat.model.FollowUpDto
import com.techtsunagu.wakatta.feature.chat.model.ReexplainDraft
import com.techtsunagu.wakatta.feature.chat.model.ReexplainRequest
import com.techtsunagu.wakatta.feature.chat.model.ThreadDto
import java.io.File
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getThreads(): Flow<List<ThreadDto>>
    fun getThreadSummaries(): Flow<List<ThreadSummary>>
    fun getMessages(threadId: String): Flow<List<ChatMessageUiModel>>

    /** User ids the debug role switch may act as. Empty (the default) hides the switch. */
    val debugUserIds: Map<Role, String> get() = emptyMap()

    suspend fun sendMessage(
        threadId: String,
        text: String,
        isVoice: Boolean,
        fromUserId: String,
        idempotencyKey: String,
    ): AppResult<ChatMessageUiModel>

    suspend fun submitAnswers(
        messageId: String,
        userId: String,
        answers: List<Int>,
        idempotencyKey: String,
    ): AppResult<AnswerResultDto>

    /** Speech to text for a finished recording. */
    suspend fun transcribe(audio: File?): AppResult<String>

    suspend fun getReexplainDraft(messageId: String, workerId: String): AppResult<ReexplainDraft>

    suspend fun reexplain(
        messageId: String,
        request: ReexplainRequest,
    ): AppResult<Unit>

    suspend fun nudge(
        messageId: String,
        userId: String,
    ): AppResult<Unit>

    suspend fun askExplainAgain(
        messageId: String,
        userId: String,
    ): AppResult<Unit>

    suspend fun getTerms(): AppResult<List<GlossaryTerm>>
    suspend fun addTerm(termJa: String, readingJa: String, termBn: String, termEn: String): AppResult<GlossaryTerm>
    suspend fun getFollowUps(): AppResult<List<FollowUpDto>>
}
