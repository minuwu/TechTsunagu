package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.ExplicitNulls
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import kotlinx.serialization.Serializable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/**
 * Panel, overrides, Note Versions and Shift Assignments (docs/openapi.yaml). A Worker body never carries
 * a worker id for "me", and a Worker-side note type never has an answer key (rule R-3).
 */
internal interface MachinesApi {
    /** Raw body: `ConditionalGet` reads the ETag and the 304. Foreman and Panel device only. */
    @GET(ApiPaths.MACHINE_PANEL)
    suspend fun panel(
        @Path("machine_id") id: String,
        @Header("If-None-Match") ifNoneMatch: String?,
    ): Response<ResponseBody>

    @POST(ApiPaths.MACHINE_OVERRIDES)
    suspend fun startOverride(
        @Path("machine_id") id: String,
        @Tag key: IdempotencyKey,
        @ExplicitNulls @Body body: OverrideInDto,
    ): Response<ApiEnvelope<OverrideDto>>

    @DELETE(ApiPaths.MACHINE_OVERRIDE)
    suspend fun endOverride(
        @Path("machine_id") id: String,
        @Path("override_id") overrideId: String,
    ): Response<ApiEnvelope<OverrideDto>>

    @POST(ApiPaths.MACHINE_REMIND)
    suspend fun remind(@Path("machine_id") id: String, @Body body: RemindInDto): Response<ApiEnvelope<Unit>>

    @POST(ApiPaths.MACHINE_SCAN)
    suspend fun scan(@Path("machine_id") id: String): Response<ApiEnvelope<Unit>>

    /** Worker-safe: the current Note Version without answers. */
    @GET(ApiPaths.MACHINE)
    suspend fun machine(@Path("machine_id") id: String): Response<ApiEnvelope<MachineInfoDto>>

    @GET(ApiPaths.MACHINE_VERSIONS)
    suspend fun versions(
        @Path("machine_id") id: String,
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?,
    ): Response<ApiEnvelope<PageDto<ForemanNoteDto>>>

    @GET(ApiPaths.NOTE_VERSION)
    suspend fun noteVersion(@Path("note_id") id: String): Response<ApiEnvelope<ForemanNoteDto>>

    @GET(ApiPaths.SHIFT_ASSIGNMENTS)
    suspend fun assignments(@Query("day") day: String?): Response<ApiEnvelope<List<AssignmentDto>>>

    @PUT(ApiPaths.SHIFT_ASSIGNMENTS)
    suspend fun assign(@ExplicitNulls @Body body: AssignmentInDto): Response<ApiEnvelope<AssignmentDto>>
}

@Serializable
internal data class PageDto<T>(val items: List<T> = emptyList(), val nextCursor: String? = null)

@Serializable
internal data class PanelWorkerDto(val id: String, val displayName: String)

@Serializable
internal data class PanelOverrideInfoDto(val id: String = "", val by: String = "", val until: String = "", val reason: String = "")

@Serializable
internal data class RemindInDto(val workerId: String)

@Serializable
internal data class PanelDto(
    val state: String,
    val worker: PanelWorkerDto? = null,
    val versionNo: Int? = null,
    val override: PanelOverrideInfoDto? = null,
)

@Serializable
internal data class OverrideInDto(val workerId: String?, val minutes: Int, val reason: String)

@Serializable
internal data class OverrideDto(
    val id: String,
    val machineId: String,
    val workerId: String? = null,
    val by: String = "",
    val reason: String = "",
    val startsAt: String = "",
    val endsAt: String = "",
    val revokedAt: String? = null,
)

@Serializable
internal data class StepDto(val text: Map<String, String> = emptyMap(), val icon: String? = null)

/** Worker-side question: no answer-key field exists here. */
@Serializable
internal data class WorkerQuestionDto(
    val id: String,
    val termId: String? = null,
    val prompt: Map<String, String> = emptyMap(),
    val options: List<Map<String, String>> = emptyList(),
)

@Serializable
internal data class WorkerNoteDto(
    val id: String,
    val versionNo: Int = 0,
    val steps: List<StepDto> = emptyList(),
    val questions: List<WorkerQuestionDto> = emptyList(),
)

@Serializable
internal data class MachineInfoDto(
    val id: String,
    val code: String,
    val name: Map<String, String> = emptyMap(),
    val current: WorkerNoteDto? = null,
)

/** Foreman-only: carries the answer key. Never used on a Worker path. */
@Serializable
internal data class ForemanQuestionDto(
    val id: String,
    val termId: String? = null,
    val prompt: Map<String, String> = emptyMap(),
    val options: List<Map<String, String>> = emptyList(),
    val correctIndex: Int = 0,
)

@Serializable
internal data class ForemanNoteDto(
    val id: String,
    val machineId: String,
    val versionNo: Int = 0,
    val status: String = "draft",
    val recordedBy: String = "",
    val approvedBy: String? = null,
    val steps: List<StepDto> = emptyList(),
    val questions: List<ForemanQuestionDto> = emptyList(),
    val needsEdit: Boolean = false,
    val createdAt: String = "",
    val approvedAt: String? = null,
)

@Serializable
internal data class AssignmentDto(
    val id: String,
    val workerId: String,
    val machineId: String,
    val date: String,
    val shift: String,
)

@Serializable
internal data class AssignmentInDto(val machineId: String, val shift: String, val date: String?, val workerId: String?)
