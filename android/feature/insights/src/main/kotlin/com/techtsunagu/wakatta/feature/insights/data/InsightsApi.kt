package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Insights and learning API routes matching docs/openapi.yaml.
 * Uses only ApiPaths constants.
 */
internal interface InsightsApi {

    @GET(ApiPaths.MY_LEARNING)
    suspend fun myLearning(
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): Response<ApiEnvelope<LearningDto>>

    @GET(ApiPaths.ANALYTICS_UNDERSTANDING)
    suspend fun understanding(
        @Query("days") days: Int = 30,
    ): Response<ApiEnvelope<UnderstandingDto>>

    @GET(ApiPaths.HEATMAP)
    suspend fun heatmap(): Response<ApiEnvelope<HeatmapDto>>

    @GET(ApiPaths.METRICS_EFFORT)
    suspend fun effort(): Response<ApiEnvelope<EffortDto>>

    @GET(ApiPaths.SKILL_MATRIX)
    suspend fun skillMatrix(): Response<ApiEnvelope<SkillMatrixDto>>

    @GET(ApiPaths.FOLLOW_UPS)
    suspend fun followUps(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("cursor") cursor: String? = null,
    ): Response<ApiEnvelope<FollowUpPageDto>>

    @POST(ApiPaths.GLOSSARY)
    suspend fun addGlossaryTerm(
        @Body body: GlossaryInDto,
    ): Response<ApiEnvelope<GlossaryOutDto>>
}

// ---- DTOs ----

@Serializable
internal data class LearningDto(
    val streakDays: Int = 0,
    val nextCursor: String? = null,
    val items: List<LearningItemDto> = emptyList(),
)

@Serializable
internal data class LearningItemDto(
    val followUpId: String,
    val termJa: String,
    val status: String,
    val missedAt: String,
    val clarifiedAt: String? = null,
    val understoodAt: String? = null,
)

@Serializable
internal data class UnderstandingDto(
    val days: Int = 30,
    val trend: List<DayCountsDto> = emptyList(),
    val perWorker: List<WorkerCountsDto> = emptyList(),
    val topTerms: List<TermCountDto> = emptyList(),
)

@Serializable
internal data class DayCountsDto(
    val day: String,
    val green: Int = 0,
    val orange: Int = 0,
    val red: Int = 0,
)

@Serializable
internal data class WorkerCountsDto(
    val workerId: String,
    val displayName: String = "",
    val green: Int = 0,
    val orange: Int = 0,
    val red: Int = 0,
)

@Serializable
internal data class TermCountDto(
    val termJa: String,
    val count: Int = 0,
)

@Serializable
internal data class HeatmapDto(
    val machines: List<HeatmapMachineDto> = emptyList(),
    val rows: List<HeatmapRowDto> = emptyList(),
)

@Serializable
internal data class HeatmapMachineDto(
    val id: String,
    val code: String,
    val name: Map<String, String> = emptyMap(),
)

@Serializable
internal data class HeatmapRowDto(
    val termId: String,
    val ja: String,
    val reading: String = "",
    val translations: Map<String, String> = emptyMap(),
    val cells: List<HeatmapCellDto> = emptyList(),
    val total: Int = 0,
)

@Serializable
internal data class HeatmapCellDto(
    val machineId: String,
    val count: Int = 0,
)

@Serializable
internal data class EffortDto(
    val drafts: Int = 0,
    val approved: Int = 0,
    val medianSecondsToApprove: Double? = null,
    val medianEditsPerDraft: Double? = null,
    val cleanAiShare: Double? = null,
    val firstTryPassShare: Double? = null,
)

@Serializable
internal data class SkillMatrixDto(
    val machines: List<SkillMachineDto> = emptyList(),
    val rows: List<SkillRowDto> = emptyList(),
)

@Serializable
internal data class SkillMachineDto(
    val id: String,
    val code: String,
    val name: Map<String, String> = emptyMap(),
)

@Serializable
internal data class SkillRowDto(
    val workerId: String,
    val displayName: String = "",
    val cells: List<SkillCellDto> = emptyList(),
)

@Serializable
internal data class SkillCellDto(
    val machineId: String,
    val status: String,
    val passedAt: String? = null,
)

@Serializable
internal data class FollowUpPageDto(
    val items: List<FollowUpDto> = emptyList(),
    val nextCursor: String? = null,
)

@Serializable
internal data class FollowUpDto(
    val id: String,
    val messageId: String = "",
    val userId: String = "",
    val displayName: String = "",
    val termJa: String = "",
    val status: String = "",
    val note: String = "",
    val createdAt: String = "",
    val resolvedAt: String? = null,
)

@Serializable
internal data class GlossaryInDto(
    val ja: String,
    val reading: String,
    val bn: String,
    val en: String,
)

@Serializable
internal data class GlossaryOutDto(
    val id: String,
    val ja: String,
    val reading: String = "",
    val bn: String = "",
    val en: String = "",
    val translations: Map<String, String> = emptyMap(),
)
