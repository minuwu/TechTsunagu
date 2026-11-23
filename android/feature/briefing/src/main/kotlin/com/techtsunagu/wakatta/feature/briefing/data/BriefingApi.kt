package com.techtsunagu.wakatta.feature.briefing.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.ExplicitNulls
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/** Briefing routes of docs/openapi.yaml. `GET /briefings` answers a different shape per role, hence two reads. */
internal interface BriefingApi {
    /** Multipart: `text` or `audio` (at least one part). */
    @Multipart
    @POST(ApiPaths.BRIEFINGS_DRAFT)
    suspend fun draft(
        @Tag key: IdempotencyKey,
        @Part parts: List<MultipartBody.Part>,
    ): Response<ApiEnvelope<ForemanBriefingDto>>

    /** `termId` is required by the schema even when null, so the body keeps explicit nulls. */
    @POST(ApiPaths.BRIEFINGS)
    suspend fun publish(
        @Tag key: IdempotencyKey,
        @Body @ExplicitNulls body: PublishDto,
    ): Response<ApiEnvelope<ForemanBriefingDto>>

    @GET(ApiPaths.BRIEFINGS)
    suspend fun foremanPage(@Query("limit") limit: Int, @Query("cursor") cursor: String?): Response<ApiEnvelope<PageDto<ForemanBriefingDto>>>

    @GET(ApiPaths.BRIEFINGS)
    suspend fun workerPage(@Query("limit") limit: Int, @Query("cursor") cursor: String?): Response<ApiEnvelope<PageDto<WorkerBriefingDto>>>

    @GET(ApiPaths.BRIEFING_RECEIPTS)
    suspend fun receipts(@Path("briefing_id") id: String): Response<ApiEnvelope<ReceiptsDto>>
}

@Serializable
internal data class PageDto<T>(val items: List<T> = emptyList(), val nextCursor: String? = null)

@Serializable
internal data class QuestionDto(
    val id: String,
    val termId: String? = null,
    val prompt: Tri = emptyMap(),
    val options: List<Tri> = emptyList(),
    val correctIndex: Int = 0,
)

/** The Worker's question: the server never sends `correctIndex` to a Worker and neither does this type. */
@Serializable
internal data class WorkerQuestionDto(
    val id: String,
    val termId: String? = null,
    val prompt: Tri = emptyMap(),
    val options: List<Tri> = emptyList(),
)

@Serializable
internal data class ForemanBriefingDto(
    val id: String,
    val text: Tri = emptyMap(),
    val status: String = "published",
    val createdAt: String = "",
    val publishedAt: String? = null,
    val transcriptJa: String = "",
    val question: QuestionDto,
    val needsEdit: Boolean = false,
)

@Serializable
internal data class WorkerBriefingDto(
    val id: String,
    val text: Tri = emptyMap(),
    val status: String = "published",
    val createdAt: String = "",
    val publishedAt: String? = null,
    val question: WorkerQuestionDto,
    val opened: Boolean = false,
    val passed: Boolean? = null,
)

/** Only these fields: the server rejects any other with 422. */
@Serializable
internal data class PublishDto(val id: String, val text: Tri, val question: QuestionDto, val createdAt: String? = null)

@Serializable
internal data class ReceiptWorkerDto(val id: String, val name: String)

@Serializable
internal data class ReceiptsDto(
    val understood: List<ReceiptWorkerDto> = emptyList(),
    val openedOnly: List<ReceiptWorkerDto> = emptyList(),
    val notOpened: List<ReceiptWorkerDto> = emptyList(),
)

internal suspend fun <T> allPages(call: suspend (cursor: String?) -> Response<ApiEnvelope<PageDto<T>>>): AppResult<List<T>> {
    val all = mutableListOf<T>()
    var cursor: String? = null
    do {
        val page = when (val r = safeApiCall { call(cursor) }) {
            is AppResult.Success -> r.value
            is AppResult.Failure -> return r
        }
        all += page.items
        cursor = page.nextCursor
    } while (cursor != null)
    return AppResult.Success(all)
}
