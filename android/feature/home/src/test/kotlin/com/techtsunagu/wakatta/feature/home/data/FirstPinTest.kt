package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstPinTest {
    @Test
    fun firstPinsHaveTheRoleLengthAndAreNeverARunOrARepeat() {
        repeat(500) {
            val worker = firstPin(Role.WORKER)
            val foreman = firstPin(Role.FOREMAN)
            assertEquals(4, worker.length)
            assertEquals(6, foreman.length)
            listOf(worker, foreman).forEach { pin ->
                val steps = pin.zipWithNext { a, b -> b - a }.toSet()
                assertTrue(pin, steps != setOf(0) && steps != setOf(1) && steps != setOf(-1))
            }
        }
    }
}
