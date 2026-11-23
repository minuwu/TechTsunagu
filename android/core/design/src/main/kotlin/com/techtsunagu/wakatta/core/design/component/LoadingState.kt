package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
        )
        val text = message ?: stringResource(R.string.core_design_loading)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun LoadingStateLightPreview() {
    WakattaTheme(darkTheme = false) {
        LoadingState()
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun LoadingStateDarkPreview() {
    WakattaTheme(darkTheme = true) {
        LoadingState()
    }
}
