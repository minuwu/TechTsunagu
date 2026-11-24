package com.techtsunagu.wakatta.feature.machines

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.model.AppError

@Composable
internal fun ViewfinderFrame(isTorchOn: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Box(
        modifier = Modifier
            .size(240.dp)
            .background(
                color = if (isTorchOn) Color(0x33FFFFFF) else Color(0x1AFFFFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = Color(0x6638BDF8),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val yPos = size.height * laserProgress
            drawLine(
                color = Color(0xFF38BDF8),
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

internal fun AppError.scanMessageRes(): Int = when (this) {
    is AppError.Validation -> R.string.machines_scan_invalid_qr
    AppError.NotFound -> R.string.machines_scan_machine_not_found
    else -> machinesMessageRes()
}
