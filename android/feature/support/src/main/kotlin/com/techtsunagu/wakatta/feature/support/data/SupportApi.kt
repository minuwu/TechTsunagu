package com.techtsunagu.wakatta.feature.support.data

import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/** Support and Interview routes of docs/openapi.yaml. */
internal interface SupportApi {

    /** Support Log CSV for 登録支援機関 (not enveloped, UTF-8 with BOM). */
    @GET(ApiPaths.SUPPORT_LOG_CSV)
    suspend fun getSupportLogCsv(
        @Query("from") from: String,
        @Query("to") to: String,
    ): Response<ResponseBody>

    /** Periodic Interviews due date per Worker. */
    @GET(ApiPaths.INTERVIEWS_DUE)
    suspend fun getDueInterviews(): Response<ApiEnvelope<List<InterviewDueDtoWire>>>

    /** Past interviews for a specific Worker, newest first. */
    @GET(ApiPaths.WORKER_INTERVIEWS)
    suspend fun getWorkerInterviews(
        @Path("worker_id") workerId: String,
    ): Response<ApiEnvelope<List<InterviewOutWire>>>

    /** Record a Periodic Interview for a Worker. */
    @POST(ApiPaths.WORKER_INTERVIEWS)
    suspend fun recordInterview(
        @Path("worker_id") workerId: String,
        @Tag key: IdempotencyKey,
        @Body body: InterviewInWire,
    ): Response<ApiEnvelope<InterviewOutWire>>

    /** Today's Word. */
    @GET(ApiPaths.TODAY_WORD)
    suspend fun getTodayWord(): Response<ApiEnvelope<TodayWordWire>>
}

@Serializable
internal data class InterviewDueDtoWire(
    val workerId: String,
    val displayName: String,
    val lastHeldOn: String? = null,
    val dueOn: String,
    val status: String,
)

@Serializable
internal data class InterviewOutWire(
    val id: String,
    val workerId: String,
    val heldOn: String,
    val method: String,
    val onlineConsentAt: String? = null,
    val noteJa: String = "",
    val recordedBy: String = "",
    val createdAt: String = "",
)

/** Only fields allowed by docs/openapi.yaml; extra fields cause 422. */
@Serializable
internal data class InterviewInWire(
    val heldOn: String,
    val method: String,
    val onlineConsentAt: String? = null,
    val noteJa: String = "",
)

@Serializable
internal data class TodayWordWire(
    val date: String,
    val language: String,
    val word: String,
    val reading: String,
    val meaningJa: String,
)
