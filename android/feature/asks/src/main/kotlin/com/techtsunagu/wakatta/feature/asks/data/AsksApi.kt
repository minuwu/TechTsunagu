package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreatedDto
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/** Asks, Senpai pairs and Meet-ups of docs/openapi.yaml. A Worker gets their own, a Foreman the shop's. */
internal interface AsksApi {
    @GET(ApiPaths.ASKS)
    suspend fun asks(@Query("limit") limit: Int, @Query("cursor") cursor: String?): Response<ApiEnvelope<PageDto<AskOutDto>>>

    /** Multipart: `text` or `audio` (at least one part). */
    @Multipart
    @POST(ApiPaths.ASKS)
    suspend fun createAsk(
        @Tag key: IdempotencyKey,
        @Part parts: List<MultipartBody.Part>,
    ): Response<ApiEnvelope<AskOutDto>>

    @POST(ApiPaths.ASK_FORWARD)
    suspend fun forward(@Path("ask_id") id: String): Response<ApiEnvelope<AskOutDto>>

    /** `data` is null when the Worker has no Senpai. */
    @GET(ApiPaths.SENPAI_PAIRS_ME)
    suspend fun myPair(): Response<ApiEnvelope<SenpaiPairOutDto>>

    @GET(ApiPaths.SENPAI_PAIRS)
    suspend fun pairs(@Query("limit") limit: Int, @Query("cursor") cursor: String?): Response<ApiEnvelope<PageDto<SenpaiPairOutDto>>>

    @POST(ApiPaths.SENPAI_PAIRS)
    suspend fun createPair(@Body body: SenpaiPairCreateRequest): Response<ApiEnvelope<SenpaiPairCreatedDto>>

    @DELETE(ApiPaths.SENPAI_PAIR)
    suspend fun revokePair(@Path("pair_id") id: String): Response<ApiEnvelope<Unit>>

    @GET(ApiPaths.MEETUPS)
    suspend fun meetups(@Query("limit") limit: Int, @Query("cursor") cursor: String?): Response<ApiEnvelope<PageDto<MeetupOutDto>>>

    @POST(ApiPaths.MEETUPS)
    suspend fun logMeetup(@Tag key: IdempotencyKey, @Body body: MeetupInDto): Response<ApiEnvelope<MeetupOutDto>>
}

@Serializable
internal data class PageDto<T>(val items: List<T> = emptyList(), val nextCursor: String? = null)

@Serializable
internal data class AskOutDto(
    val id: String,
    val workerId: String = "",
    val workerName: String = "",
    val text: String = "",
    val textJa: String? = null,
    val covered: Boolean = false,
    val answer: Tri? = null,
    val sourceNoticeIds: List<String> = emptyList(),
    val forwarded: Boolean = false,
    val senpaiReply: Tri? = null,
    val createdAt: String = "",
)

@Serializable
internal data class SenpaiPairOutDto(
    val id: String,
    val workerId: String = "",
    val senpaiName: String,
    val kind: String = "coworker",
    val createdAt: String = "",
    val expiresAt: String = "",
)

@Serializable
internal data class MeetupOutDto(
    val id: String,
    val pairId: String = "",
    val kind: String = "other",
    val date: String = "",
    val note: String = "",
    val loggedBy: String = "worker",
)

/** Only these three fields: the server rejects any other with 422. */
@Serializable
internal data class MeetupInDto(val kind: String, val date: String, val note: String)

/** A list that could not be loaded: the screen shows its error and a retry. */
internal class LoadFailed(val error: AppError) : Exception(error.toString())

internal fun <T> AppResult<T>.orThrow(): T = when (this) {
    is AppResult.Success -> value
    is AppResult.Failure -> throw LoadFailed(error)
}

/** The Worker's Senpai, or null when none is paired (the server answers `data: null`). */
internal suspend fun AsksApi.pairOrNull(): SenpaiPairOutDto? {
    var paired = true
    val res = safeApiCall { myPair().also { if (it.isSuccessful) paired = it.body()?.data != null } }
    return if (paired) res.orThrow() else null
}

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
