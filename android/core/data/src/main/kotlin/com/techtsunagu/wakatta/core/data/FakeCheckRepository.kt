package com.techtsunagu.wakatta.core.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.CheckResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.model.SignOff
import com.techtsunagu.wakatta.core.model.SkillStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeCheckRepository(
    private val delayMs: Long = 200L,
    private val signOffRepository: FakeSignOffRepository? = null
) : CheckRepository {

    @Inject
    constructor(signOffRepository: FakeSignOffRepository) : this(200L, signOffRepository)


    override suspend fun submitCheck(
        targetKind: CheckTargetKind,
        targetId: String,
        language: String,
        answers: List<Int>,
        idempotencyKey: String
    ): AppResult<CheckResult> {
        if (delayMs > 0) delay(delayMs)
        // Grade server-side simulation:
        // In this fake, the correct option index for questions is 0.
        // Worker responses never contain correctIndex; evaluation happens here.
        // A Note Check targets a Note Version ("nv_<machine>_<tag>") on the live server; the Fake keys by Machine.
        val machineId = Regex("^nv_(.+)_[^_]+$").find(targetId)?.groupValues?.get(1) ?: targetId
        val wrong = mutableListOf<String>()
        answers.forEachIndexed { index, selected ->
            if (selected != 0) {
                wrong.add("q_${machineId}_${index + 1}")
            }
        }
        val passed = wrong.isEmpty()
        val signOffGranted = passed && (targetKind == CheckTargetKind.NOTE)

        if (signOffGranted) {
            signOffRepository?.addSignOff(
                workerId = "u_rahim",
                signOff = SignOff(
                    machineId = machineId,
                    machineName = mapOf("ja" to "Machine $machineId", "en" to "Machine $machineId"),
                    passedAt = "2026-11-23T12:00:00Z",
                    expiresAt = "2026-12-23T12:00:00Z",
                    status = SkillStatus.VALID
                )
            )
        }

        return AppResult.Success(
            CheckResult(
                passed = passed,
                wrongQuestions = wrong,
                signOffGranted = signOffGranted
            )
        )
    }
}
