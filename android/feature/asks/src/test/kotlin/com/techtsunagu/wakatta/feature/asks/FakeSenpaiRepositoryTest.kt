package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.feature.asks.data.FakeSenpaiRepository
import com.techtsunagu.wakatta.feature.asks.model.SenpaiPairCreateRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeSenpaiRepositoryTest {

    private lateinit var repository: FakeSenpaiRepository

    @Before
    fun setUp() {
        repository = FakeSenpaiRepository(simulatedLatencyMs = 0L)
    }

    @Test
    fun getSenpaiPairForWorkerReturnsPair() = runTest {
        val pair = repository.getSenpaiPairForWorker("u_rahim").first()
        assertNotNull(pair)
        assertEquals("u_rahim", pair?.workerId)
        assertEquals("佐藤 美咲", pair?.senpaiName)

        val unknown = repository.getSenpaiPairForWorker("u_unknown").first()
        assertNull(unknown)
    }

    @Test
    fun createSenpaiPairGeneratesToken() = runTest {
        val res = repository.createSenpaiPair(
            SenpaiPairCreateRequest(
                workerId = "u_new_worker",
                senpaiName = "佐藤 美咲",
                kind = "coworker",
            ),
        )
        assertTrue(res.isSuccess)
        val created = res.getOrNull()!!
        assertEquals("u_new_worker", created.workerId)
        assertTrue(created.token.isNotEmpty())
    }

    @Test
    fun createSenpaiPairFailsIfAlreadyExists() = runTest {
        val res = repository.createSenpaiPair(
            SenpaiPairCreateRequest(
                workerId = "u_rahim",
                senpaiName = "佐藤 美咲",
                kind = "coworker",
            ),
        )
        assertTrue(res.isFailure)
        val err = res.errorOrNull() as AppError.Conflict
        assertEquals("ALREADY_EXISTS", err.code)
    }

    @Test
    fun deleteSenpaiPairRemovesPair() = runTest {
        val res = repository.deleteSenpaiPair("sp1")
        assertTrue(res.isSuccess)

        val pairAfter = repository.getSenpaiPairForWorker("u_rahim").first()
        assertNull(pairAfter)

        val res2 = repository.deleteSenpaiPair("sp1")
        assertTrue(res2.isFailure)
        assertEquals(AppError.NotFound, res2.errorOrNull())
    }
}
