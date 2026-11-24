package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.R
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme

@Composable
fun ErrorState(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    retryLabel: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            val label = retryLabel ?: stringResource(R.string.core_design_retry)
            OutlinedButton(
                onClick = onRetry,
            ) {
                Text(text = label)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun ErrorStateLightPreview() {
    WakattaTheme(darkTheme = false) {
        ErrorState(
            message = "Unable to connect to the server. Please check your connection.",
            onRetry = {},
        )
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun ErrorStateDarkPreview() {
    WakattaTheme(darkTheme = true) {
        ErrorState(
            message = "Unable to connect to the server. Please check your connection.",
            onRetry = {},
        )
    }
}
