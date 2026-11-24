package com.techtsunagu.wakatta.feature.chat

import com.techtsunagu.wakatta.core.common.CrashGuard
import com.techtsunagu.wakatta.core.common.MviViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CrashGuardTest {
    private class Boom : MviViewModel<Int, Unit, Unit>(0) {
        override fun onEvent(event: Unit) {
            viewModelScope.launch { error("boom") }
        }
    }

    @Before fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        CrashGuard.rethrow = false
    }

    @After fun tearDown() {
        CrashGuard.rethrow = true
        Dispatchers.resetMain()
    }

    @Test fun releaseBuild_aScreenError_isReportedNotThrown() = runTest(UnconfinedTestDispatcher()) {
        val reported = async { CrashGuard.errors.first() }
        Boom().onEvent(Unit) // would crash the app without the guard
        assertEquals("boom", reported.await().message)
    }
}
