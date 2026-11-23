package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.R
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.design.theme.WakattaShapes
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme
import com.techtsunagu.wakatta.core.model.WakattaStatus

@Composable
fun StatusChip(
    status: WakattaStatus,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val statusColor = StatusColors.forStatus(status)
    val text = label ?: when (status) {
        WakattaStatus.GREEN -> stringResource(R.string.core_design_status_green)
        WakattaStatus.ORANGE -> stringResource(R.string.core_design_status_orange)
        WakattaStatus.RED -> stringResource(R.string.core_design_status_red)
        WakattaStatus.WAIT, WakattaStatus.UNKNOWN -> stringResource(R.string.core_design_status_wait)
    }

    Surface(
        modifier = modifier,
        shape = WakattaShapes.small,
        color = statusColor.containerColor,
        contentColor = statusColor.onContainerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(statusColor.color, CircleShape),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun StatusChipLightPreview() {
    WakattaTheme(darkTheme = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            StatusChip(status = WakattaStatus.GREEN)
            StatusChip(status = WakattaStatus.ORANGE)
            StatusChip(status = WakattaStatus.RED)
            StatusChip(status = WakattaStatus.WAIT)
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun StatusChipDarkPreview() {
    WakattaTheme(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(8.dp)) {
            StatusChip(status = WakattaStatus.GREEN)
            StatusChip(status = WakattaStatus.ORANGE)
            StatusChip(status = WakattaStatus.RED)
            StatusChip(status = WakattaStatus.WAIT)
        }
    }
}
