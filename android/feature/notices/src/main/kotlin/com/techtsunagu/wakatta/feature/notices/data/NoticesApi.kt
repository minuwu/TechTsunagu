package com.techtsunagu.wakatta.feature.notices.data

import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Worker reads of docs/openapi.yaml. The Worker shape has no answer key (rule R-3); Poster routes are not here. */
internal interface NoticesApi {
    @GET(ApiPaths.NOTICES)
    suspend fun notices(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?,
    ): Response<ApiEnvelope<PageDto<WorkerNoticeDto>>>

    @GET(ApiPaths.NOTICE)
    suspend fun notice(@Path("notice_id") id: String): Response<ApiEnvelope<WorkerNoticeDto>>

    @GET(ApiPaths.EVENTS)
    suspend fun events(
        @Query("from") from: String,
        @Query("to") to: String,
    ): Response<ApiEnvelope<List<EventOccurrenceDto>>>
}

@Serializable
internal data class PageDto<T>(val items: List<T> = emptyList(), val nextCursor: String? = null)

@Serializable
internal data class EventOutDto(
    val id: String,
    val title: Tri = emptyMap(),
    val startsAt: String,
    val confirmed: Boolean = true,
    val nextDates: List<String> = emptyList(),
)

@Serializable
internal data class EventOccurrenceDto(
    val eventId: String,
    val noticeId: String,
    val title: Tri = emptyMap(),
    val at: String,
)

/** The question a Worker sees: no `correctIndex` field exists here. */
@Serializable
internal data class WorkerQuestionDto(
    val id: String,
    val termId: String? = null,
    val prompt: Tri = emptyMap(),
    val options: List<Tri> = emptyList(),
)

@Serializable
internal data class WorkerNoticeDto(
    val id: String,
    val category: String = "other",
    val sourceKind: String = "text",
    val sourceJa: String = "",
    val title: Tri = emptyMap(),
    val steps: List<Tri> = emptyList(),
    val events: List<EventOutDto> = emptyList(),
    val createdAt: String = "",
    val publishedAt: String? = null,
    val question: WorkerQuestionDto,
    val opened: Boolean = false,
    val passed: Boolean? = null,
)
