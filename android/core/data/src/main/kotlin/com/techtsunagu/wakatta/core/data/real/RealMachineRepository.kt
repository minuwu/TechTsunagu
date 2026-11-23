package com.techtsunagu.wakatta.core.data.real

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Machine
import com.techtsunagu.wakatta.core.model.MachineDetail
import com.techtsunagu.wakatta.core.model.MachineRepository
import com.techtsunagu.wakatta.core.model.Role
import com.techtsunagu.wakatta.core.model.SessionStore
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.core.network.safeApiCall
import com.techtsunagu.wakatta.core.network.safeApiCallUnit

/** [api] is a provider so mock builds never construct the HTTP stack. */
class RealMachineRepository(
    private val api: () -> SharedDataApi,
    private val session: SessionStore,
) : MachineRepository {

    /**
     * Ready is per Worker, so a Worker's list reads their own Skill Passport (valid or expiring = Ready).
     * shortcut: the server has no machine-level Ready for a Foreman, so Foreman Ready = has an approved Note Version.
     */
    override suspend fun getMachines(): AppResult<List<Machine>> {
        val machines = when (val result = allPages { limit, cursor -> api().machines(limit, cursor) }) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        val user = session.session.value?.user
        if (user?.role != Role.WORKER) {
            return AppResult.Success(machines.map { it.toMachine(isReady = it.currentVersionId != null) })
        }
        val passport = when (val result = fetchPassport(api, user.id)) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        val ready = passport.signOffs
            .filter { it.status == SkillStatus.VALID || it.status == SkillStatus.EXPIRING }
            .map { it.machineId }
            .toSet()
        return AppResult.Success(machines.map { it.toMachine(isReady = it.id in ready) })
    }

    override suspend fun getMachine(id: String): AppResult<MachineDetail> =
        safeApiCall { api().machine(id) }.map { it.toDetail() }

    override suspend fun scan(machineId: String): AppResult<Unit> = safeApiCallUnit { api().scan(machineId) }
}
