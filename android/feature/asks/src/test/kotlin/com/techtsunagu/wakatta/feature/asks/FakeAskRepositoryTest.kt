package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.feature.asks.data.FakeAskRepository
import com.techtsunagu.wakatta.feature.asks.model.AskCreateRequest
import com.techtsunagu.wakatta.feature.asks.model.AskKind
import com.techtsunagu.wakatta.feature.asks.model.AskStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAskRepositoryTest {

    private lateinit var repository: FakeAskRepository

    @Before
    fun setUp() {
        repository = FakeAskRepository(simulatedLatencyMs = 0L)
    }

    @Test
    fun getAsksFiltersByWorker() = runTest {
        val rahimAsks = repository.getAsks("u_rahim").first()
        assertTrue(rahimAsks.isNotEmpty())
        assertTrue(rahimAsks.all { it.workerId == "u_rahim" })

        val allAsks = repository.getAsks(null).first()
        assertTrue(allAsks.size >= rahimAsks.size)
    }

    @Test
    fun sendAskMatchesNoticeOnKeyword() = runTest {
        val result = repository.sendAsk(
            workerId = "u_rahim",
            request = AskCreateRequest(text = "When is garbage day?"),
        )
        assertTrue(result.isSuccess)
        val ask = result.getOrNull()!!
        assertTrue(ask.covered)
        assertEquals(AskStatus.ANSWERED, ask.status)
        assertNotNull(ask.matchNotice)
        assertEquals("n_burn", ask.matchNotice?.id)
        assertEquals(listOf("n_burn"), ask.sourceNoticeIds)
    }

    @Test
    fun sendAskForwardsWhenNoNoticeMatches() = runTest {
        val result = repository.sendAsk(
            workerId = "u_rahim",
            request = AskCreateRequest(text = "Where can I find halal food?"),
        )
        assertTrue(result.isSuccess)
        val ask = result.getOrNull()!!
        assertFalse(ask.covered)
        assertEquals(AskStatus.FORWARDED, ask.status)
        assertEquals("佐藤 美咲", ask.senpaiName)
        assertEquals("Sato Misaki", ask.senpaiEnName)
    }

    @Test
    fun sendAskFailsOnEmptyText() = runTest {
        val result = repository.sendAsk(
            workerId = "u_rahim",
            request = AskCreateRequest(text = "   "),
        )
        assertTrue(result.isFailure)
        assertTrue(result.errorOrNull() is AppError.Validation)
    }

    @Test
    fun forwardAskFailsWhenUnpaired() = runTest {
        val sendRes = repository.sendAsk(
            workerId = "u_unpaired",
            request = AskCreateRequest(text = "Unpaired worker question"),
        )
        val askId = sendRes.getOrNull()!!.id

        val forwardRes = repository.forwardAsk(askId)
        assertTrue(forwardRes.isFailure)
        val err = forwardRes.errorOrNull() as AppError.Conflict
        assertEquals("NO_ACTIVE_PAIR", err.code)
    }

    @Test
    fun replyAskUpdatesStatusAndReplies() = runTest {
        val result = repository.replyAsk(
            askId = "ak3",
            replyJa = "市役所で買えますよ。",
            replyEn = "You can buy it at the city office.",
            replyBn = "আপনি সিটি অফিসে এটি কিনতে পারেন।",
        )
        assertTrue(result.isSuccess)
        val updated = result.getOrNull()!!
        assertEquals(AskStatus.REPLIED, updated.status)
        assertEquals("市役所で買えますよ。", updated.replyJa)
        assertEquals("You can buy it at the city office.", updated.replyEn)
    }
}
