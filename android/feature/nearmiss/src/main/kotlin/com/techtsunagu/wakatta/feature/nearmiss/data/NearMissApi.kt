package com.techtsunagu.wakatta.feature.nearmiss.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/** Near-miss routes of docs/openapi.yaml. The photo is read with `GET ApiPaths.NEAR_MISS_PHOTO` through the app OkHttp client. */
internal interface NearMissApi {
    /** Multipart: `machineId`, then `text` and `photo` (`audio` is not sent: the screen sends its transcript as text). */
    @Multipart
    @POST(ApiPaths.NEAR_MISSES)
    suspend fun report(
        @Tag key: IdempotencyKey,
        @Part parts: List<MultipartBody.Part>,
    ): Response<ApiEnvelope<NearMissOutDto>>

    /** Foreman only. */
    @GET(ApiPaths.NEAR_MISSES)
    suspend fun reports(@Query("limit") limit: Int, @Query("cursor") cursor: String?): Response<ApiEnvelope<PageDto<NearMissOutDto>>>

    @POST(ApiPaths.NEAR_MISS_ACK)
    suspend fun ack(@Path("report_id") id: String): Response<ApiEnvelope<NearMissOutDto>>
}

@Serializable
internal data class PageDto<T>(val items: List<T> = emptyList(), val nextCursor: String? = null)

@Serializable
internal data class NearMissOutDto(
    val id: String,
    val workerId: String = "",
    val machineId: String = "",
    val text: Tri = emptyMap(),
    val photo: String? = null,
    val createdAt: String = "",
    val acknowledgedAt: String? = null,
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
