package com.techtsunagu.wakatta.core.design.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.theme.WakattaShapes
import com.techtsunagu.wakatta.core.design.theme.WakattaTheme

@Composable
fun WakattaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: Shape = WakattaShapes.large,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            border = border,
            content = content,
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Composable
private fun WakattaCardLightPreview() {
    WakattaTheme(darkTheme = false) {
        WakattaCard(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Card Content", style = MaterialTheme.typography.titleMedium)
                Text(text = "Supporting description text", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
private fun WakattaCardDarkPreview() {
    WakattaTheme(darkTheme = true) {
        WakattaCard(modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Card Content", style = MaterialTheme.typography.titleMedium)
                Text(text = "Supporting description text", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
