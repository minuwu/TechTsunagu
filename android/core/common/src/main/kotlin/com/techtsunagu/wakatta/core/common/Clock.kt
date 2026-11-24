package com.techtsunagu.wakatta.core.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface AppClock {
    fun now(): Instant
    fun nowEpochMillis(): Long
    fun today(): LocalDate
    fun formatTokyo(instant: Instant, pattern: String): String
}

class TokyoClock : AppClock {
    private val tokyoZone = ZoneId.of("Asia/Tokyo")

    override fun now(): Instant = Instant.now()

    override fun nowEpochMillis(): Long = System.currentTimeMillis()

    override fun today(): LocalDate = LocalDate.now(tokyoZone)

    override fun formatTokyo(instant: Instant, pattern: String): String {
        val formatter = DateTimeFormatter.ofPattern(pattern).withZone(tokyoZone)
        return formatter.format(instant)
    }
}
