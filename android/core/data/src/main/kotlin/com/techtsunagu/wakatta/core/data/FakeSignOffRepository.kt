package com.techtsunagu.wakatta.core.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Passport
import com.techtsunagu.wakatta.core.model.SignOff
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.model.SkillStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FakeSignOffRepository(
    private val delayMs: Long = 200L
) : SignOffRepository {

    @Inject
    constructor() : this(200L)


    private val signOffMap = mutableMapOf<String, MutableList<SignOff>>().apply {
        put(
            "u_rahim",
            mutableListOf(
                SignOff(
                    machineId = "mc1",
                    machineName = mapOf("ja" to "旋盤1号機", "en" to "Lathe 1", "bn" to "লেদ ১"),
                    passedAt = "2026-11-20T09:00:00Z",
                    expiresAt = "2026-12-20T09:00:00Z",
                    status = SkillStatus.VALID
                ),
                SignOff(
                    machineId = "mc3",
                    machineName = mapOf("ja" to "3号機 成形", "en" to "Machine 3 · Molding", "bn" to "মেশিন ৩"),
                    passedAt = "2026-11-21T14:30:00Z",
                    expiresAt = "2026-12-21T14:30:00Z",
                    status = SkillStatus.VALID
                )
            )
        )
    }

    override suspend fun getSignOffs(workerId: String): AppResult<List<SignOff>> {
        if (delayMs > 0) delay(delayMs)
        val list = signOffMap[workerId] ?: emptyList()
        return AppResult.Success(list)
    }

    override suspend fun getPassport(workerId: String): AppResult<Passport> {
        if (delayMs > 0) delay(delayMs)
        val user = DemoSeed.users.find { it.id == workerId }
        val name = user?.displayName ?: "Worker"
        val signOffs = signOffMap[workerId] ?: emptyList()
        return AppResult.Success(Passport(workerId = workerId, workerName = name, signOffs = signOffs))
    }

    internal fun addSignOff(workerId: String, signOff: SignOff) {
        val list = signOffMap.getOrPut(workerId) { mutableListOf() }
        list.removeAll { it.machineId == signOff.machineId }
        list.add(signOff)
    }
}
