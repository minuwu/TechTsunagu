package com.techtsunagu.wakatta.feature.machines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.component.WakattaCard
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.PanelState
import com.techtsunagu.wakatta.feature.machines.domain.PanelOverrideDto

@Composable
internal fun PanelHeroSection(state: PanelUiState) {
    val (label, bg, fg, icon) = when (state.state) {
        PanelState.READY -> Quad(stringResource(R.string.machines_panel_state_ready), StatusColors.GreenContainer, StatusColors.OnGreenContainer, Icons.Default.Check)
        PanelState.OVERRIDE -> Quad(stringResource(R.string.machines_panel_state_override), StatusColors.OrangeContainer, StatusColors.OnOrangeContainer, Icons.Default.Warning)
        // Neutral grey, never green: the Panel does not know the real state.
        PanelState.UNKNOWN -> Quad(stringResource(R.string.machines_panel_state_unknown), StatusColors.GreyContainer, StatusColors.OnGreyContainer, Icons.Default.Warning)
        else -> Quad(stringResource(R.string.machines_panel_state_locked), StatusColors.RedContainer, StatusColors.OnRedContainer, Icons.Default.Lock)
    }

    WakattaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(color = bg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = fg,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = fg
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = state.machineName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            val subtext = if (state.state == PanelState.UNKNOWN) {
                stringResource(R.string.machines_panel_unknown)
            } else if (state.isForeman) {
                stringResource(
                    R.string.machines_signed_off_ratio,
                    state.signedOffCount,
                    state.totalWorkerCount
                )
            } else {
                if (state.isUserSignedOff) {
                    stringResource(R.string.machines_panel_signed_off_on, state.noteVersion)
                } else {
                    stringResource(R.string.machines_panel_no_signoff_yet, state.noteVersion)
                }
            }

            Text(
                text = subtext,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

internal data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
internal fun ActiveOverrideBanner(
    override: PanelOverrideDto,
    isForeman: Boolean,
    onEndOverride: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = StatusColors.OrangeContainer,
        contentColor = StatusColors.OnOrangeContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = StatusColors.Orange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.machines_panel_override_banner),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isForeman) {
                    Button(
                        onClick = onEndOverride,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusColors.Orange,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.machines_panel_override_end_btn),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.machines_panel_override_by, override.by),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(R.string.machines_panel_override_reason, override.reason),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
