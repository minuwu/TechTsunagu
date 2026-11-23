package com.techtsunagu.wakatta.feature.machines.data

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.NoteStatus
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.feature.machines.domain.NoteQuestionDto
import com.techtsunagu.wakatta.feature.machines.domain.NoteRepository
import com.techtsunagu.wakatta.feature.machines.domain.NoteStepDto
import com.techtsunagu.wakatta.feature.machines.domain.NoteVersionDto

/**
 * A Foreman reads every Note Version (with the answer key); a Worker reads only the current one from the
 * Machine route, which has no answer key. [api] is a provider so mock builds never build the HTTP stack.
 */
internal class RealNoteRepository(
    private val api: () -> MachinesApi,
    private val session: SessionStore,
) : NoteRepository {

    private val isForeman get() = session.session.value?.user?.role == Role.FOREMAN

    override suspend fun getNoteVersion(machineId: String): AppResult<NoteVersionDto> =
        when (val result = getVersions(machineId)) {
            is AppResult.Success -> result.value.firstOrNull()?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> result
        }

    override suspend fun getVersions(machineId: String): AppResult<List<NoteVersionDto>> {
        if (!isForeman) return currentForWorker(machineId).mapList()
        return allVersions(machineId).mapOk { list -> list.map { it.toNote() } }
    }

    /** shortcut: a Worker cannot list or open old Versions (Foreman-only routes), so the Worker sees the current one. */
    override suspend fun getVersion(versionId: String): AppResult<NoteVersionDto> =
        if (isForeman) safeApiCall { api().noteVersion(versionId) }.mapOk { it.toNote() }
        else AppResult.Failure(AppError.Forbidden)

    private suspend fun allVersions(machineId: String): AppResult<List<ForemanNoteDto>> {
        val all = mutableListOf<ForemanNoteDto>()
        var cursor: String? = null
        do {
            val page = when (val r = safeApiCall { api().versions(machineId, PAGE_LIMIT, cursor) }) {
                is AppResult.Success -> r.value
                is AppResult.Failure -> return r
            }
            all += page.items
            cursor = page.nextCursor
        } while (cursor != null)
        return AppResult.Success(all)
    }

    private suspend fun currentForWorker(machineId: String): AppResult<NoteVersionDto> =
        when (val r = safeApiCall { api().machine(machineId) }) {
            is AppResult.Success -> r.value.current?.toNote(machineId)?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.NotFound)
            is AppResult.Failure -> r
        }

    private fun AppResult<NoteVersionDto>.mapList(): AppResult<List<NoteVersionDto>> = mapOk { listOf(it) }

    private companion object {
        const val PAGE_LIMIT = 200
    }
}

internal inline fun <A, B> AppResult<A>.mapOk(transform: (A) -> B): AppResult<B> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(value))
    is AppResult.Failure -> this
}

private fun Map<String, String>.tr(lang: String): String = this[lang] ?: this["ja"].orEmpty()

// shortcut: the server has no per-step back-translation and no question-to-step link, so backJa stays empty and stepIndex 0.
private fun List<StepDto>.toSteps() = mapIndexed { i, s ->
    NoteStepDto(i + 1, s.icon.orEmpty(), s.text.tr("ja"), s.text.tr("en"), s.text.tr("bn"), "")
}

private fun note(
    id: String, machineId: String, versionNo: Int, status: NoteStatus, updatedAt: String, by: String,
    steps: List<StepDto>, questions: List<NoteQuestionDto>, needsEdit: Boolean,
) = NoteVersionDto(id, machineId, "v$versionNo", status, updatedAt, by, steps.toSteps(), questions, needsEdit)

private fun question(
    id: String, termId: String?, prompt: Map<String, String>, options: List<Map<String, String>>, correct: Int?,
) = NoteQuestionDto(
    id = id,
    stepIndex = 0,
    term = termId.orEmpty(),
    qJa = prompt.tr("ja"), qEn = prompt.tr("en"), qBn = prompt.tr("bn"),
    optionsJa = options.map { it.tr("ja") }, optionsEn = options.map { it.tr("en") }, optionsBn = options.map { it.tr("bn") },
    correctIndex = correct,
)

/** The Worker path: no answer key is read, so none can be stored. */
internal fun WorkerNoteDto.toNote(machineId: String) = note(
    id, machineId, versionNo, NoteStatus.APPROVED, "", "", steps,
    questions.map { question(it.id, it.termId, it.prompt, it.options, correct = null) }, needsEdit = false,
)

internal fun ForemanNoteDto.toNote() = note(
    id, machineId, versionNo,
    if (status.equals("approved", ignoreCase = true)) NoteStatus.APPROVED else NoteStatus.DRAFT,
    (approvedAt ?: createdAt).take(10), approvedBy ?: recordedBy, steps,
    questions.map { question(it.id, it.termId, it.prompt, it.options, it.correctIndex) }, needsEdit,
)
