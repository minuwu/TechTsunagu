package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme

@Composable
fun WakattaListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: (@Composable () -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val itemModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    ListItem(
        content = headlineContent,
        modifier = itemModifier,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            headlineColor = MaterialTheme.colorScheme.onSurface,
            supportingColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun WakattaListItemLightPreview() {
    WakattaTheme(darkTheme = false) {
        WakattaListItem(
            headlineContent = { Text("List item title") },
            supportingContent = { Text("Supporting detail") },
        )
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun WakattaListItemDarkPreview() {
    WakattaTheme(darkTheme = true) {
        WakattaListItem(
            headlineContent = { Text("List item title") },
            supportingContent = { Text("Supporting detail") },
        )
    }
}
