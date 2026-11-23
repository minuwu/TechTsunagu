package com.techtsunagu.wakatta.feature.asks.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
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

/** Worker routes of docs/openapi.yaml for Asks, the Senpai pair and Meet-ups. The Worker is the token's; no id is sent. */
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
    val text: String = "",
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

/** The Worker's Senpai, or null when none is paired (the server answers `data: null`) or the call failed. */
internal suspend fun AsksApi.pairOrNull(): SenpaiPairOutDto? =
    (safeApiCall { myPair() } as? AppResult.Success)?.value

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
