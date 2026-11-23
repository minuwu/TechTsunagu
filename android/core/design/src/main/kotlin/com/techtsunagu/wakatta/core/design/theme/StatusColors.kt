package com.techtsunagu.wakatta.core.design.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.techtsunagu.wakatta.core.model.WakattaStatus

@Immutable
data class StatusColor(
    val color: Color,
    val containerColor: Color,
    val onContainerColor: Color,
)

// Wakatta status colors matching prototype tokens (--ok, --warn, --bad, --wait)
object StatusColors {
    val Green = Color(0xFF1E8E3E)
    val GreenContainer = Color(0xFFCEEAD6)
    val OnGreenContainer = Color(0xFF0D5524)

    val Orange = Color(0xFFE37400)
    val OrangeContainer = Color(0xFFFEEFC3)
    val OnOrangeContainer = Color(0xFF6B3600)

    val Red = Color(0xFFD93025)
    val RedContainer = Color(0xFFFAD2CF)
    val OnRedContainer = Color(0xFF680003)

    val Grey = Color(0xFF9AA0A6)
    val GreyContainer = Color(0xFFE8EAED)
    val OnGreyContainer = Color(0xFF3C4043)

    fun forStatus(status: WakattaStatus): StatusColor = when (status) {
        WakattaStatus.GREEN -> StatusColor(Green, GreenContainer, OnGreenContainer)
        WakattaStatus.ORANGE -> StatusColor(Orange, OrangeContainer, OnOrangeContainer)
        WakattaStatus.RED -> StatusColor(Red, RedContainer, OnRedContainer)
        WakattaStatus.WAIT, WakattaStatus.UNKNOWN -> StatusColor(Grey, GreyContainer, OnGreyContainer)
    }
}
