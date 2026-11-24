package com.techtsunagu.wakatta.feature.chat.data

import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.ExplicitNulls
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/** Chat routes (docs/openapi.yaml). Enums travel as lowercase strings and are mapped in ChatWire.kt. */
internal interface ChatApi {
    @GET(ApiPaths.THREADS)
    suspend fun threads(): Response<ApiEnvelope<List<WireThread>>>

    /** Raw body: `ConditionalGet` reads the ETag and the 304. */
    @GET(ApiPaths.THREAD_MESSAGES)
    suspend fun messages(
        @Path("thread_id") id: String,
        @Header("If-None-Match") ifNoneMatch: String?,
    ): Response<ResponseBody>

    @Multipart
    @POST(ApiPaths.THREAD_MESSAGES)
    suspend fun post(
        @Path("thread_id") id: String,
        @Tag key: IdempotencyKey,
        @Part text: MultipartBody.Part? = null,
        @Part audio: MultipartBody.Part? = null,
    ): Response<ApiEnvelope<WireSenderMessage>>

    @Multipart
    @POST(ApiPaths.TRANSCRIPTIONS)
    suspend fun transcribe(
        @Part language: MultipartBody.Part,
        @Part audio: MultipartBody.Part,
    ): Response<ApiEnvelope<WireTranscription>>

    /** `@ExplicitNulls`: every `CheckQuestion` field is required, including a null `termId` and `correctIndex` 0. */
    @POST(ApiPaths.MESSAGE_SEND)
    suspend fun send(
        @Path("message_id") id: String,
        @ExplicitNulls @Body body: SendInDto,
    ): Response<ApiEnvelope<WireSenderMessage>>

    @POST(ApiPaths.MESSAGE_ANSWERS)
    suspend fun answers(
        @Path("message_id") id: String,
        @Body body: AnswersInDto,
    ): Response<ApiEnvelope<WireAnswerResult>>

    @PATCH(ApiPaths.MESSAGE)
    suspend fun editDraft(
        @Path("message_id") id: String,
        @Body body: DraftEditDto,
    ): Response<ApiEnvelope<WireSenderMessage>>

    @POST(ApiPaths.MESSAGE_REEXPLAIN)
    suspend fun reexplain(
        @Path("message_id") id: String,
        @Body body: ReexplainInDto,
    ): Response<ApiEnvelope<WireSenderMessage>>

    @POST(ApiPaths.MESSAGE_REEXPLAIN_DRAFT)
    suspend fun reexplainDraft(
        @Path("message_id") id: String,
        @Body body: ReexplainInDto,
    ): Response<ApiEnvelope<WireReexplainDraft>>

    @POST(ApiPaths.MESSAGE_ASK)
    suspend fun ask(
        @Path("message_id") id: String,
        @Body body: AskForemanInDto,
    ): Response<ApiEnvelope<WireSenderMessage>>

    @POST(ApiPaths.MESSAGE_NUDGE)
    suspend fun nudge(
        @Path("message_id") id: String,
        @Body body: NudgeInDto,
    ): Response<ApiEnvelope<Unit>>

    /** Foreman only (a Worker gets 403, XREQ-020). */
    @GET(ApiPaths.FOLLOW_UPS)
    suspend fun followUps(@Query("limit") limit: Int): Response<ApiEnvelope<WirePage<WireFollowUp>>>
}

@Serializable
internal data class WirePage<T>(val items: List<T> = emptyList(), val nextCursor: String? = null)

@Serializable
internal data class WireTranscription(val text: String = "")

@Serializable
internal data class WireThread(
    val id: String,
    val titleJa: String = "",
    val kind: String = "group",
    val memberIds: List<String> = emptyList(),
    val unread: Int = 0,
    val lastMessageAt: String? = null,
    val lastMessage: WireLastMessage? = null,
    val wkStatus: String? = null,
)

/** The newest message the caller can see, for the Chats row. */
@Serializable
internal data class WireLastMessage(
    val fromUserId: String,
    val kind: String = "text",
    val text: Tri = emptyMap(),
)

/** The Worker's view of a Check Question: this type has no answer-key field (rule R-3). */
@Serializable
internal data class WireQuestion(
    val id: String,
    val termId: String? = null,
    val prompt: Tri = emptyMap(),
    val options: List<Tri> = emptyList(),
)

/** Foreman's view. Only decoded on a Foreman session and echoed back to `send`. */
@Serializable
internal data class WireSenderQuestion(
    val id: String,
    val termId: String? = null,
    val prompt: Tri = emptyMap(),
    val options: List<Tri> = emptyList(),
    val correctIndex: Int = 0,
)

@Serializable
internal data class WireTask(
    val title: Tri,
    val assigneeIds: List<String> = emptyList(),
    val dueAt: String? = null,
    val machineId: String? = null,
)

internal interface WireMessage {
    val id: String
    val threadId: String
    val fromUserId: String
    val kind: String
    val stage: Int
    val transcriptJa: String?
    val text: Tri
    val createdAt: String
    val sentAt: String?
}

@Serializable
internal data class WireSenderMessage(
    override val id: String,
    override val threadId: String = "",
    override val fromUserId: String = "",
    override val kind: String = "text",
    override val stage: Int = 5,
    val needsEdit: Boolean = false,
    override val transcriptJa: String? = null,
    override val text: Tri = emptyMap(),
    override val createdAt: String = "",
    override val sentAt: String? = null,
    val questions: List<WireSenderQuestion> = emptyList(),
    val taskDrafts: List<WireTask> = emptyList(),
    val results: List<WireWorkerResult> = emptyList(),
) : WireMessage

/** One Worker's state on a sent message, for the Foreman's Wakatta card. */
@Serializable
internal data class WireWorkerResult(
    val userId: String,
    val status: String? = null,
    val answers: List<Int>? = null,
    val read: Boolean = false,
    val nudged: Boolean = false,
)

@Serializable
internal data class WireWorkerMessage(
    override val id: String,
    override val threadId: String = "",
    override val fromUserId: String = "",
    override val kind: String = "text",
    override val stage: Int = 5,
    override val transcriptJa: String? = null,
    override val text: Tri = emptyMap(),
    override val createdAt: String = "",
    override val sentAt: String? = null,
    val questions: List<WireQuestion> = emptyList(),
    val myStatus: String? = null,
    val nudged: Boolean = false,
    val myAnswers: List<Int>? = null,
    val answerKey: List<Int>? = null,
    val asked: Boolean = false,
) : WireMessage

@Serializable
internal data class DraftEditDto(val textJa: String)

@Serializable
internal data class AskForemanInDto(val termJa: String)

@Serializable
internal data class WireReexplainDraft(
    val text: Tri = emptyMap(),
    val question: WireSenderQuestion,
)

@Serializable
internal data class SendInDto(val text: Tri, val questions: List<WireSenderQuestion>, val tasks: List<WireTask>)

@Serializable
internal data class AnswersInDto(val answers: List<Int>)

@Serializable
internal data class ReexplainInDto(
    val userId: String,
    val termJa: String,
    val text: Tri? = null,
    val question: WireSenderQuestion? = null,
)

@Serializable
internal data class NudgeInDto(val userId: String)

@Serializable
internal data class WireAnswerResult(val status: String = "", val followUpIds: List<String> = emptyList())

@Serializable
internal data class WireFollowUp(
    val id: String,
    val messageId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val termJa: String = "",
    val status: String = "open",
    val note: String = "",
    val createdAt: String = "",
    val resolvedAt: String? = null,
)
