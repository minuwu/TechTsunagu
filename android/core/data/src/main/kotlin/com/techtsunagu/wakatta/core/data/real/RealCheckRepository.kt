package com.techtsunagu.wakatta.core.data.real

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.CheckRepository
import com.techtsunagu.wakatta.core.model.CheckResult
import com.techtsunagu.wakatta.core.model.CheckTargetKind
import com.techtsunagu.wakatta.core.network.IdempotencyKey
import com.techtsunagu.wakatta.core.network.safeApiCall

/** The server grades; the app never sees an answer key. [submitCheck]'s `targetId` of a NOTE is the Note Version id. */
class RealCheckRepository(private val api: () -> SharedDataApi) : CheckRepository {

    override suspend fun submitCheck(
        targetKind: CheckTargetKind,
        targetId: String,
        language: String,
        answers: List<Int>,
        idempotencyKey: String,
    ): AppResult<CheckResult> {
        if (targetKind == CheckTargetKind.UNKNOWN) return AppResult.Failure(AppError.Validation(listOf("kind")))
        val body = CheckInDto(TargetDto(targetKind.name.lowercase(), targetId), language, answers)
        return safeApiCall { api().check(IdempotencyKey(idempotencyKey), body) }
            .map { CheckResult(it.passed, it.wrong.map { q -> q.id }, it.signOff != null) }
    }
}
