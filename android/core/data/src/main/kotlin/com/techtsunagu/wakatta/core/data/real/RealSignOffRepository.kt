package com.techtsunagu.wakatta.core.data.real

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Passport
import com.techtsunagu.wakatta.core.model.SignOff
import com.techtsunagu.wakatta.core.model.SignOffRepository
import com.techtsunagu.wakatta.core.network.safeApiCall

internal suspend fun fetchPassport(api: () -> SharedDataApi, workerId: String): AppResult<Passport> =
    safeApiCall { api().passport(workerId) }.map { it.toPassport() }

class RealSignOffRepository(private val api: () -> SharedDataApi) : SignOffRepository {

    /** Read from the Passport, not `GET /sign-offs`: only the Passport carries Machine names and valid/expiring/expired. */
    override suspend fun getSignOffs(workerId: String): AppResult<List<SignOff>> =
        fetchPassport(api, workerId).map { it.signOffs }

    override suspend fun getPassport(workerId: String): AppResult<Passport> = fetchPassport(api, workerId)
}
