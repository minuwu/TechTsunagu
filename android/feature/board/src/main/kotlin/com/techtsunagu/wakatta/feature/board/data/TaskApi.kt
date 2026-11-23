package com.techtsunagu.wakatta.feature.board.data

import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/** Task Board routes (docs/openapi.yaml). Bodies are `TaskCreateRequest` and `TaskUpdateRequest`; a `null` field is left out. */
internal interface TaskApi {
    @GET(ApiPaths.TASKS)
    suspend fun tasks(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?,
    ): Response<ApiEnvelope<TaskPage>>

    @POST(ApiPaths.TASKS)
    suspend fun create(
        @Tag key: IdempotencyKey,
        @Body body: TaskCreateRequest,
    ): Response<ApiEnvelope<TaskDto>>

    /** `If-Match` carries the version number the card was read with; a stale one answers 412. */
    @PATCH(ApiPaths.TASK)
    suspend fun update(
        @Path("task_id") id: String,
        @Header("If-Match") version: String,
        @Body body: TaskUpdateRequest,
    ): Response<ApiEnvelope<TaskDto>>
}

@Serializable
internal data class TaskPage(val items: List<TaskDto> = emptyList(), val nextCursor: String? = null)
