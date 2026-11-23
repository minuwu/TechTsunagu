package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    icon: (@Composable () -> Unit)? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (icon != null) {
            Box(modifier = Modifier.padding(bottom = 16.dp)) {
                icon()
            }
        }
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(text = actionLabel)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun EmptyStateLightPreview() {
    WakattaTheme(darkTheme = false) {
        EmptyState(
            title = "No Messages",
            message = "There are no messages in this thread yet.",
            actionLabel = "Start Chat",
            onActionClick = {},
        )
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun EmptyStateDarkPreview() {
    WakattaTheme(darkTheme = true) {
        EmptyState(
            title = "No Messages",
            message = "There are no messages in this thread yet.",
            actionLabel = "Start Chat",
            onActionClick = {},
        )
    }
}
