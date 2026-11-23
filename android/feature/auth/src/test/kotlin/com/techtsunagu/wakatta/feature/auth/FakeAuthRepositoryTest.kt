package com.techtsunagu.wakatta.feature.auth

import com.techtsunagu.wakatta.core.model.AppResult
import com.techtsunagu.wakatta.core.model.Role
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeAuthRepositoryTest {

    private val store = MemorySessionStore()
    private val repository = FakeAuthRepository(store)

    @Test
    fun rosterIsSplitByRole() = runBlocking {
        val foremen = (repository.roster("SHOP1234", Role.FOREMAN) as AppResult.Success).value
        val workers = (repository.roster("SHOP1234", Role.WORKER) as AppResult.Success).value
        assertEquals(1, foremen.size)
        assertEquals(3, workers.size)
    }

    @Test
    fun signInSavesTheSession() = runBlocking {
        repository.signIn("u_rahim", "9137")
        assertEquals("u_rahim", store.session.value?.user?.id)
    }
}
