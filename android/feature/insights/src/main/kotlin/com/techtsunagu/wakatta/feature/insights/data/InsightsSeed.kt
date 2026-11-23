package com.techtsunagu.wakatta.feature.insights.data

import com.techtsunagu.wakatta.core.model.FollowUpStatus

/** Demo data ported from the prototype (A.js seeds, E.js BASE, AGE, STREAK). Used only by the Fakes. */
internal object InsightsSeed {
    const val MINUTES_PER_DAY = 1440L
    const val MILLIS_PER_DAY = MINUTES_PER_DAY * 60_000L

    class Confusion(val workerId: String, val term: String, val daysAgo: Int)

    class FollowUpSeed(
        val id: String,
        val workerId: String,
        val term: String,
        val question: String,
        val wrong: String,
        val ageMinutes: Long,
        val status: FollowUpStatus,
    )

    val confusions = listOf(
        Confusion("u_siti", "玉掛け", 1), Confusion("u_siti", "金型交換", 2), Confusion("u_siti", "治具", 3),
        Confusion("u_siti", "切粉", 6), Confusion("u_siti", "段取り", 9), Confusion("u_siti", "玉掛け", 12),
        Confusion("u_rahim", "金型交換", 2), Confusion("u_rahim", "段取り", 4), Confusion("u_rahim", "切粉", 8),
        Confusion("u_rahim", "玉掛け", 11), Confusion("u_nguyen", "治具", 5), Confusion("u_nguyen", "玉掛け", 7),
    )

    val termEn = mapOf(
        "玉掛け" to "slinging", "金型交換" to "die change", "治具" to "jig", "切粉" to "chips",
        "段取り" to "setup", "3号機オイル" to "Machine 3 oil",
    )

    val followUps = listOf(
        FollowUpSeed("f_h3", "u_siti", "玉掛け", "What is 玉掛け (slinging)?", "Not sure", MINUTES_PER_DAY, FollowUpStatus.CLARIFIED),
        FollowUpSeed("f_h5", "u_rahim", "金型交換", "What is 金型交換 (die change)?", "Oil change", 2 * MINUTES_PER_DAY, FollowUpStatus.CLARIFIED),
        FollowUpSeed("f_m1", "u_siti", "3号機オイル", "What do you check first today?", "Air on Machine 2", 60, FollowUpStatus.OPEN),
    )

    /** Sign-offs on record (prototype DATA.signoffs count). */
    const val SIGN_OFF_RECORDS = 6

    const val BASE_SENT = 36
    const val BASE_OK = 28
    val baseTrend = listOf(62, 68, 74)

    /** Green, orange, red counts per worker. */
    val baseBars = mapOf(
        "u_rahim" to Triple(14, 3, 1), "u_nguyen" to Triple(11, 5, 2), "u_siti" to Triple(9, 6, 3),
    )

    /** Sign-off age in days per worker and machine; a missing key means no Sign-off. */
    val signOffAgeDays = mapOf(
        ("u_rahim" to "mc1") to 120, ("u_rahim" to "mc3") to 40, ("u_nguyen" to "mc1") to 356,
        ("u_nguyen" to "mc2") to 356, ("u_nguyen" to "mc3") to 15, ("u_siti" to "mc3") to 60,
    )
    const val SIGN_OFF_VALID_DAYS = 365
    const val EXPIRING_DAYS = 14

    val streak = mapOf("u_rahim" to 5, "u_nguyen" to 7, "u_siti" to 2)
}
