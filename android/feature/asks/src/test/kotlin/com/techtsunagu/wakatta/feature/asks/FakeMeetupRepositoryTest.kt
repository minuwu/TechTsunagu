package com.techtsunagu.wakatta.feature.asks

import com.techtsunagu.wakatta.core.model.AppError
import com.techtsunagu.wakatta.feature.asks.data.FakeMeetupRepository
import com.techtsunagu.wakatta.feature.asks.model.MeetupCreateRequest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeMeetupRepositoryTest {

    private lateinit var repository: FakeMeetupRepository

    @Before
    fun setUp() {
        repository = FakeMeetupRepository(simulatedLatencyMs = 0L)
    }

    @Test
    fun getMeetupsForWorkerFiltersAndSorts() = runTest {
        val meetups = repository.getMeetupsForWorker("u_rahim").first()
        assertTrue(meetups.isNotEmpty())
        assertTrue(meetups.all { it.workerId == "u_rahim" })

        // Check descending sort
        for (i in 0 until meetups.size - 1) {
            assertTrue(meetups[i].date >= meetups[i + 1].date)
        }
    }

    @Test
    fun logMeetupSuccess() = runTest {
        val res = repository.logMeetup(
            workerId = "u_rahim",
            request = MeetupCreateRequest(
                kind = "drill",
                date = "2026-11-20",
                note = "Practiced evacuation",
            ),
        )
        assertTrue(res.isSuccess)
        val created = res.getOrNull()!!
        assertEquals("u_rahim", created.workerId)
        assertEquals("drill", created.kind)
        assertEquals("Practiced evacuation", created.note)

        val updated = repository.getMeetupsForWorker("u_rahim").first()
        assertTrue(updated.any { it.id == created.id })
    }

    @Test
    fun logMeetupFailsOnFutureDate() = runTest {
        val res = repository.logMeetup(
            workerId = "u_rahim",
            request = MeetupCreateRequest(
                kind = "coffee",
                date = "2026-12-01",
                note = "Future meetup",
            ),
        )
        assertTrue(res.isFailure)
        val err = res.errorOrNull() as AppError.Validation
        assertEquals(listOf("date"), err.fields)
    }

    @Test
    fun logMeetupFailsIfUnpaired() = runTest {
        val res = repository.logMeetup(
            workerId = "u_unpaired",
            request = MeetupCreateRequest(
                kind = "coffee",
                date = "2026-11-20",
                note = "Unpaired meetup",
            ),
        )
        assertTrue(res.isFailure)
        val err = res.errorOrNull() as AppError.Conflict
        assertEquals("NO_ACTIVE_PAIR", err.code)
    }
}
