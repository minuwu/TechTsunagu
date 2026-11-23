package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Tri
import com.techtsunagu.wakatta.core.network.ApiConfig.ApiPaths
import com.techtsunagu.wakatta.core.network.ApiEnvelope
import com.techtsunagu.wakatta.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET

/** What the Foreman Home needs from `GET /board`: Machines with a Worker assigned without a valid Sign-off. */
data class HomeBoard(val lockedMachines: List<BoardLocked>, val openConfusions: Int)

data class BoardLocked(val machineId: String, val name: Tri, val workerNames: List<String>)

interface HomeBoardSource {
    /** Success(null) means "no live data": the Home keeps its demo content (the Fake). */
    suspend fun getBoard(): AppResult<HomeBoard?>
}

@Singleton
class FakeHomeBoardSource @Inject constructor() : HomeBoardSource {
    override suspend fun getBoard(): AppResult<HomeBoard?> = AppResult.Success(null)
}

internal interface HomeApi {
    /** Foreman only. */
    @GET(ApiPaths.BOARD)
    suspend fun board(): Response<ApiEnvelope<WireBoard>>
}

@Serializable
internal data class WireBoard(val machines: List<WireBoardMachine> = emptyList(), val openConfusions: Int = 0)

@Serializable
internal data class WireBoardMachine(
    val id: String,
    val name: Tri = emptyMap(),
    val assignments: List<WireAssignment> = emptyList(),
)

@Serializable
internal data class WireAssignment(val displayName: String = "", val flag: Boolean = false)

internal class RealHomeBoardSource(private val api: () -> HomeApi) : HomeBoardSource {
    override suspend fun getBoard(): AppResult<HomeBoard?> = when (val r = safeApiCall { api().board() }) {
        is AppResult.Success -> AppResult.Success(r.value.toHomeBoard())
        is AppResult.Failure -> r
    }
}

internal fun WireBoard.toHomeBoard() = HomeBoard(
    lockedMachines = machines.filter { m -> m.assignments.any { it.flag } }.map { m ->
        BoardLocked(m.id, m.name, m.assignments.filter { it.flag }.map { it.displayName }.distinct())
    },
    openConfusions = openConfusions,
)
