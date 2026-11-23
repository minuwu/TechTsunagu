package com.techtsunagu.wakatta.feature.support

import com.techtsunagu.wakatta.core.model.InterviewStatus
import com.techtsunagu.wakatta.feature.support.domain.InterviewDueCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InterviewDueCalculatorTest {

    @Test
    fun addMonths_clampsToMonthEndCorrectly() {
        // August 31 + 3 months -> November 30 (clamped from 31)
        assertEquals("2026-11-30", InterviewDueCalculator.addMonths("2026-08-31", 3))

        // May 31 + 3 months -> August 31
        assertEquals("2026-08-31", InterviewDueCalculator.addMonths("2026-05-31", 3))

        // January 31 + 1 month -> February 28 (non-leap year)
        assertEquals("2026-02-28", InterviewDueCalculator.addMonths("2026-01-31", 1))

        // October 20 + 3 months -> January 20
        assertEquals("2027-01-20", InterviewDueCalculator.addMonths("2026-10-20", 3))
    }

    @Test
    fun calculateDue_handlesNullLastDate() {
        val result = InterviewDueCalculator.calculateDue(
            workerId = "u_new",
            workerName = "New Worker",
            lastDate = null,
            todayIso = "2026-11-24",
        )
        assertEquals("u_new", result.workerId)
        assertNull(result.lastDate)
        assertNull(result.dueDate)
        assertEquals(InterviewStatus.OVERDUE, result.status)
    }

    @Test
    fun calculateDue_handlesOverdueState() {
        // Siti: last interview 2026-07-15 -> due 2026-10-15 -> on 2026-11-24 is 40 days overdue
        val result = InterviewDueCalculator.calculateDue(
            workerId = "u_siti",
            workerName = "Siti Rahma",
            lastDate = "2026-07-15",
            todayIso = "2026-11-24",
        )
        assertEquals(InterviewStatus.OVERDUE, result.status)
        assertTrue(result.daysLeft < 0)
        assertEquals("2026-10-15", result.dueDate)
    }

    @Test
    fun calculateDue_handlesSoonState() {
        // Nguyen: last interview 2026-08-28 -> due 2026-11-28 -> on 2026-11-24 is 4 days left (<= 14 days)
        val result = InterviewDueCalculator.calculateDue(
            workerId = "u_nguyen",
            workerName = "Nguyen Van An",
            lastDate = "2026-08-28",
            todayIso = "2026-11-24",
        )
        assertEquals(InterviewStatus.SOON, result.status)
        assertEquals(4, result.daysLeft)
        assertEquals("2026-11-28", result.dueDate)
    }

    @Test
    fun calculateDue_handlesOkState() {
        // Rahim: last interview 2026-10-20 -> due 2027-01-20 -> on 2026-11-24 is 57 days left (> 14 days)
        val result = InterviewDueCalculator.calculateDue(
            workerId = "u_rahim",
            workerName = "Rahim Uddin",
            lastDate = "2026-10-20",
            todayIso = "2026-11-24",
        )
        assertEquals(InterviewStatus.OK, result.status)
        assertEquals(57, result.daysLeft)
        assertEquals("2027-01-20", result.dueDate)
    }
}
