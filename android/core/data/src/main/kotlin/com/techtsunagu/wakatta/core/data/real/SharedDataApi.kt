package com.techtsunagu.wakatta.core.data.real

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckQuestion
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineDetail
import com.techtsunagu.wakatta.core.model.Passport
import com.techtsunagu.wakatta.core.model.SignOff
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.model.Step
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Tag

/** Shared data routes (docs/openapi.yaml). Worker bodies never carry a worker id or `correctIndex`. */
interface SharedDataApi {
    @GET(ApiPaths.MACHINES)
    suspend fun machines(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?,
    ): Response<ApiEnvelope<PageDto<MachineDto>>>

    @GET(ApiPaths.MACHINE)
    suspend fun machine(@Path("machine_id") id: String): Response<ApiEnvelope<MachineDetailDto>>

    @POST(ApiPaths.MACHINE_SCAN)
    suspend fun scan(@Path("machine_id") id: String): Response<ApiEnvelope<Unit>>

    @POST(ApiPaths.MACHINES)
    suspend fun addMachine(@Body body: MachineInDto): Response<ApiEnvelope<MachineDto>>

    @PATCH(ApiPaths.MACHINE)
    suspend fun renameMachine(@Path("machine_id") id: String, @Body body: MachineRenameDto): Response<ApiEnvelope<MachineDto>>

    @DELETE(ApiPaths.MACHINE)
    suspend fun archiveMachine(@Path("machine_id") id: String): Response<ApiEnvelope<Unit>>

    @GET(ApiPaths.WORKERS)
    suspend fun workers(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?,
    ): Response<ApiEnvelope<PageDto<AccountDto>>>

    @GET(ApiPaths.WORKER_PASSPORT)
    suspend fun passport(@Path("worker_id") id: String): Response<ApiEnvelope<PassportDto>>

    @GET(ApiPaths.GLOSSARY)
    suspend fun glossary(): Response<ApiEnvelope<List<GlossaryDto>>>

    @POST(ApiPaths.GLOSSARY)
    suspend fun addTerm(@Body body: GlossaryInDto): Response<ApiEnvelope<GlossaryDto>>

    @POST(ApiPaths.CHECKS)
    suspend fun check(@Tag key: IdempotencyKey, @Body body: CheckInDto): Response<ApiEnvelope<CheckResultDto>>

    @POST(ApiPaths.RECEIPTS)
    suspend fun markOpened(@Body body: ReceiptInDto): Response<ApiEnvelope<Unit>>

    @GET(ApiPaths.BRIEFING_RECEIPTS)
    suspend fun briefingReceipts(@Path("briefing_id") id: String): Response<ApiEnvelope<ReceiptsDto>>
}

@Serializable
data class PageDto<T>(val items: List<T> = emptyList(), val nextCursor: String? = null)

@Serializable
data class MachineDto(
    val id: String,
    val code: String,
    val name: Map<String, String> = emptyMap(),
    val currentVersionId: String? = null,
    val activeWorkerId: String? = null,
)

@Serializable
data class MachineInDto(val code: String, val name: Map<String, String>)

@Serializable
data class MachineRenameDto(val name: Map<String, String>)

@Serializable
data class StepDto(val text: Map<String, String> = emptyMap(), val icon: String? = null)

/** The Worker-side question: no answer-key field exists here (rule R-3). */
@Serializable
data class WorkerQuestionDto(
    val id: String,
    val termId: String? = null,
    val prompt: Map<String, String> = emptyMap(),
    val options: List<Map<String, String>> = emptyList(),
)

@Serializable
data class NoteForWorkerDto(
    val steps: List<StepDto> = emptyList(),
    val questions: List<WorkerQuestionDto> = emptyList(),
)

@Serializable
data class MachineDetailDto(
    val id: String,
    val code: String,
    val name: Map<String, String> = emptyMap(),
    val currentVersionId: String? = null,
    val current: NoteForWorkerDto? = null,
)

@Serializable
data class AccountDto(
    val id: String,
    val displayName: String,
    val language: String = "ja",
    val active: Boolean = true,
)

@Serializable
data class SkillMachineDto(val id: String, val name: Map<String, String> = emptyMap())

@Serializable
data class SkillCellDto(val machineId: String, val status: String = "none", val passedAt: String? = null)

@Serializable
data class PassportDto(
    val workerId: String,
    val displayName: String,
    val machines: List<SkillMachineDto> = emptyList(),
    val cells: List<SkillCellDto> = emptyList(),
)

@Serializable
data class GlossaryDto(val ja: String, val reading: String = "", val bn: String = "", val en: String = "")

@Serializable
data class GlossaryInDto(val ja: String, val reading: String, val bn: String, val en: String)

@Serializable
data class TargetDto(val kind: String, val id: String)

@Serializable
data class CheckInDto(val target: TargetDto, val language: String, val answers: List<Int>)

@Serializable
data class WrongQuestionDto(val id: String)

@Serializable
data class SignOffDto(val id: String = "")

@Serializable
data class CheckResultDto(
    val passed: Boolean,
    val wrong: List<WrongQuestionDto> = emptyList(),
    val signOff: SignOffDto? = null,
)

@Serializable
data class ReceiptInDto(val target: TargetDto)

@Serializable
data class ReceiptWorkerDto(val id: String, val name: String)

@Serializable
data class ReceiptsDto(
    val understood: List<ReceiptWorkerDto> = emptyList(),
    val openedOnly: List<ReceiptWorkerDto> = emptyList(),
    val notOpened: List<ReceiptWorkerDto> = emptyList(),
)

internal fun MachineDto.toMachine(isReady: Boolean) =
    Machine(id, code, name, currentVersionId, activeWorkerId, isReady)

internal fun MachineDetailDto.toDetail() = MachineDetail(
    id = id,
    code = code,
    name = name,
    currentVersionId = currentVersionId,
    steps = current?.steps.orEmpty().map { Step(it.text, it.icon) },
    questions = current?.questions.orEmpty().map { CheckQuestion(it.id, it.termId, it.prompt, it.options) },
)

internal fun skillStatus(wire: String): SkillStatus =
    SkillStatus.entries.firstOrNull { it.name.equals(wire, ignoreCase = true) } ?: SkillStatus.UNKNOWN

/** A Machine with no `passedAt` (status none) has no Sign-off row, as in the Fake. */
internal fun PassportDto.toPassport(): Passport {
    val names = machines.associate { it.id to it.name }
    val signOffs = cells.mapNotNull { cell ->
        // shortcut: the server sends no expiry date, so expiresAt stays null, wire when the backend ships it
        cell.passedAt?.let { SignOff(cell.machineId, names[cell.machineId].orEmpty(), it, null, skillStatus(cell.status)) }
    }
    return Passport(workerId, displayName, signOffs)
}

private const val PAGE_LIMIT = 200

/** Follows `nextCursor` until the last page. */
internal suspend fun <T> allPages(
    fetch: suspend (limit: Int, cursor: String?) -> Response<ApiEnvelope<PageDto<T>>>,
): AppResult<List<T>> {
    val all = mutableListOf<T>()
    var cursor: String? = null
    do {
        val page = when (val result = safeApiCall { fetch(PAGE_LIMIT, cursor) }) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        all += page.items
        cursor = page.nextCursor
    } while (cursor != null)
    return AppResult.Success(all)
}

internal fun <A, B> AppResult<A>.map(transform: (A) -> B): AppResult<B> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}
