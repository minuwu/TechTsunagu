package com.techtsunagu.wakatta.core.model

/**
 * Seed data mirrored from prototypes/wakatta/parts/skeleton.html DATA.
 */
object DemoSeed {
    val users = listOf(
        User(
            id = "u_tanaka",
            displayName = "田中 健一",
            role = Role.FOREMAN,
            language = "ja",
            shopId = "1",
            districtId = "1"
        ),
        User(
            id = "u_rahim",
            displayName = "Rahim Uddin",
            role = Role.WORKER,
            language = "bn",
            shopId = "1",
            districtId = "1"
        ),
        User(
            id = "u_nguyen",
            displayName = "Nguyen Van An",
            role = Role.WORKER,
            language = "vi",
            shopId = "1",
            districtId = "1"
        ),
        User(
            id = "u_siti",
            displayName = "Siti Rahma",
            role = Role.WORKER,
            language = "id",
            shopId = "1",
            districtId = "1"
        )
    )

    val demoWorker = users[1] // u_rahim
    val demoForeman = users[0] // u_tanaka

    val machines = listOf(
        Machine(
            id = "mc1",
            code = "lathe-1",
            name = mapOf("ja" to "旋盤1号機", "en" to "Lathe 1", "bn" to "লেদ ১"),
            currentVersionId = "v1",
            isReady = true
        ),
        Machine(
            id = "mc2",
            code = "press-2",
            name = mapOf("ja" to "2号プレス (200t)", "en" to "Press #2 (200t)", "bn" to "প্রেস #২"),
            currentVersionId = "v2",
            isReady = false
        ),
        Machine(
            id = "mc3",
            code = "molding-3",
            name = mapOf("ja" to "3号機 成形", "en" to "Machine 3 · Molding", "bn" to "মেশিন ৩"),
            currentVersionId = "v3",
            isReady = true
        )
    )

    val threads = listOf(
        ThreadSummary(
            id = "t_line2",
            title = mapOf("ja" to "Line 2 · プレス班", "en" to "Line 2 · Press team", "bn" to "লাইন ২ · প্রেস দল"),
            lastMessage = "今日は3号機の油の点検を先にしてください。",
            unreadCount = 2,
            updatedAt = "08:02"
        ),
        ThreadSummary(
            id = "t_rahim",
            title = mapOf("ja" to "田中 健一 (Foreman)", "en" to "Tanaka Kenichi (Foreman)", "bn" to "তানাকা কেনইচি"),
            lastMessage = "明日の点検表、ありがとう。",
            unreadCount = 0,
            updatedAt = "昨日"
        ),
        ThreadSummary(
            id = "t_safety",
            title = mapOf("ja" to "安全連絡", "en" to "Safety notices", "bn" to "নিরাপত্তা নোটিশ"),
            lastMessage = "安全第一",
            unreadCount = 0,
            updatedAt = "10:00"
        )
    )
}
