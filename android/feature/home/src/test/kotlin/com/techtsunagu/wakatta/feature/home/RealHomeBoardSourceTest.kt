package com.techtsunagu.wakatta.feature.home

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.DemoSeed
import com.techtsunagu.wakatta.core.model.Session
import com.techtsunagu.wakatta.core.network.ApiClientFactory
import com.techtsunagu.wakatta.feature.home.data.HomeApi
import com.techtsunagu.wakatta.feature.home.data.RealHomeBoardSource
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test

class RealHomeBoardSourceTest {
    @Test
    fun onlyMachinesWithAFlaggedAssignmentAreLockedAndOpenConfusionsAreCounted() = runBlocking {
        val server = MockWebServer().also { it.start() }
        val session = TestSessionStore(Session("t", user = DemoSeed.demoForeman))
        val api = ApiClientFactory
            .retrofit(server.url("/").toString(), ApiClientFactory.okHttp("1.0", { "en" }, session, debug = false))
            .create(HomeApi::class.java)
        val rahim = """{"shift":"day","workerId":"12","displayName":"Rahim","status":"valid","flag":false}"""
        val nguyen = """{"shift":"day","workerId":"13","displayName":"Nguyen","status":"none","flag":true}"""
        server.enqueue(
            MockResponse.Builder().body(
                """{"success":true,"data":{"date":"2026-11-24","openConfusions":2,"machines":[""" +
                    """{"id":"1","code":"a","name":{"ja":"A"},"versionNo":1,"openConfusions":0,"assignments":[$rahim]},""" +
                    """{"id":"2","code":"b","name":{"ja":"B"},"versionNo":1,"openConfusions":1,"assignments":[$nguyen]}]},"message":"ok"}""",
            ).build(),
        )
        val board = (RealHomeBoardSource { api }.getBoard() as AppResult.Success).value!!
        assertEquals(listOf("2"), board.lockedMachines.map { it.machineId })
        assertEquals(listOf("Nguyen"), board.lockedMachines[0].workerNames)
        assertEquals(2, board.openConfusions)
        server.close()
    }
}
