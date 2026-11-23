package com.techtsunagu.wakatta.feature.home.data

import com.techtsunagu.wakatta.core.model.AttentionCard
import com.techtsunagu.wakatta.core.model.AttentionSource
import com.techtsunagu.wakatta.core.model.WakattaStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeAttentionSource @Inject constructor() : AttentionSource {

    private val cards = listOf(
        AttentionCard(
            id = "att1",
            title = "Rahim Uddin",
            subtitle = "2号プレス: 金型交換の手順",
            severity = WakattaStatus.ORANGE,
            routeKey = "chat/t_line2"
        ),
        AttentionCard(
            id = "att2",
            title = "Nguyen Van An",
            subtitle = "安全連絡: 防護メガネ着用",
            severity = WakattaStatus.RED,
            routeKey = "chat/t_safety"
        ),
        AttentionCard(
            id = "att3",
            title = "Siti Rahma",
            subtitle = "旋盤1号機: 始業前点検",
            severity = WakattaStatus.WAIT,
            routeKey = "chat/t_line2"
        )
    )

    override fun getAttentionCards(): Flow<List<AttentionCard>> = flowOf(cards)
}
