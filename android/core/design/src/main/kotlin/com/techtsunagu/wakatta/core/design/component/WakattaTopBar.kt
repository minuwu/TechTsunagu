package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.techtsunagu.wakatta.core.design.R
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WakattaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        modifier = modifier,
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.core_design_back),
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
    )
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun WakattaTopBarLightPreview() {
    WakattaTheme(darkTheme = false) {
        WakattaTopBar(
            title = "Wakatta",
            onBackClick = {},
        )
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun WakattaTopBarDarkPreview() {
    WakattaTheme(darkTheme = true) {
        WakattaTopBar(
            title = "Wakatta",
            onBackClick = {},
        )
    }
}
