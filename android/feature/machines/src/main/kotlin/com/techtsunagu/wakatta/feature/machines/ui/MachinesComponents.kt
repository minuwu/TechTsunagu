package com.techtsunagu.wakatta.feature.machines.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import com.techtsunagu.wakatta.core.model.SkillStatus
import com.techtsunagu.wakatta.feature.machines.R

@Composable
fun MachineQrSticker(
    seed: String,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
) {
    val qrDesc = stringResource(R.string.machines_cd_qr)
    val matrix = remember(seed) { generateQrMatrix(seed) }

    Surface(
        modifier = modifier
            .size(size)
            .semantics { contentDescription = qrDesc },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp,
    ) {
        Canvas(
            modifier = Modifier
                .padding(12.dp)
                .size(size - 24.dp)
        ) {
            val n = matrix.size
            val cellSize = this.size.width / n
            for (y in 0 until n) {
                for (x in 0 until n) {
                    if (matrix[y][x]) {
                        drawRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(x * cellSize, y * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                        )
                    }
                }
            }
        }
    }
}

private fun generateQrMatrix(seed: String): List<List<Boolean>> {
    var s = 0L
    for (c in seed) {
        s = (s * 31L + c.code.toLong()) and 0xFFFFFFFFL
    }
    fun rnd(): Double {
        s = (s * 1664525L + 1013904223L) and 0xFFFFFFFFL
        return s.toDouble() / 4294967296.0
    }

    val n = 21
    val corners = listOf(0 to 0, 14 to 0, 0 to 14)
    val matrix = MutableList(n) { MutableList(n) { false } }

    for (y in 0 until n) {
        for (x in 0 until n) {
            var isZone = false
            var isDark = false
            for ((cx, cy) in corners) {
                val lx = x - cx
                val ly = y - cy
                if (lx in -1..7 && ly in -1..7) {
                    isZone = true
                    if (lx in 0..6 && ly in 0..6) {
                        isDark = lx == 0 || lx == 6 || ly == 0 || ly == 6 || (lx in 2..4 && ly in 2..4)
                    }
                }
            }
            if (!isZone) {
                isDark = rnd() > 0.52
            }
            matrix[y][x] = isDark
        }
    }
    return matrix
}

@Composable
fun MachineStatusBadge(
    status: SkillStatus,
    isReady: Boolean,
    modifier: Modifier = Modifier,
) {
    val (label, bg, fg, icon) = when {
        status == SkillStatus.EXPIRING -> {
            Quad(
                stringResource(R.string.machines_expiring_soon),
                StatusColors.OrangeContainer,
                StatusColors.OnOrangeContainer,
                Icons.Default.Lock
            )
        }
        isReady || status == SkillStatus.VALID -> {
            Quad(
                stringResource(R.string.machines_ready_to_operate),
                StatusColors.GreenContainer,
                StatusColors.OnGreenContainer,
                Icons.Default.Check
            )
        }
        else -> {
            Quad(
                stringResource(R.string.machines_locked),
                StatusColors.RedContainer,
                StatusColors.OnRedContainer,
                Icons.Default.Lock
            )
        }
    }

    Box(
        modifier = modifier
            .background(color = bg, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = " $label",
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun StepIcon(
    iconKey: String,
    modifier: Modifier = Modifier,
) {
    val iconVector = when (iconKey.lowercase()) {
        "lock" -> Icons.Default.Lock
        "hot", "pinch", "warning" -> Icons.Default.Warning
        else -> Icons.Default.Check
    }
    Surface(
        modifier = modifier.size(36.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
