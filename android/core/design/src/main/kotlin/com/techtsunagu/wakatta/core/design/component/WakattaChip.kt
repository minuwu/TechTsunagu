package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.theme.WakattaShapes
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme

@Composable
fun WakattaChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        shape = WakattaShapes.small,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
fun WakattaChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    WakattaChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
    )
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun WakattaChipLightPreview() {
    WakattaTheme(darkTheme = false) {
        Row(modifier = Modifier.padding(8.dp)) {
            WakattaChip(text = "Active", selected = true, onClick = {})
            WakattaChip(text = "Inactive", selected = false, onClick = {})
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun WakattaChipDarkPreview() {
    WakattaTheme(darkTheme = true) {
        Row(modifier = Modifier.padding(8.dp)) {
            WakattaChip(text = "Active", selected = true, onClick = {})
            WakattaChip(text = "Inactive", selected = false, onClick = {})
        }
    }
}
