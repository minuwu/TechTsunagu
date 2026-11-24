package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme

@Composable
fun Avatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val initial = name.trim().take(1).uppercase()
    Box(
        modifier = modifier
            .size(size)
            .background(color = containerColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = contentColor,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun AvatarLightPreview() {
    WakattaTheme(darkTheme = false) {
        Row(modifier = Modifier.padding(8.dp)) {
            Avatar(name = "Tanaka")
            Avatar(name = "Rahim")
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun AvatarDarkPreview() {
    WakattaTheme(darkTheme = true) {
        Row(modifier = Modifier.padding(8.dp)) {
            Avatar(name = "Tanaka")
            Avatar(name = "Rahim")
        }
    }
}
