package com.techtsunagu.wakatta.feature.auth

import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.techtsunagu.wakatta.core.design.theme.StatusColors
import kotlinx.coroutines.launch

private const val PAGES = 4

/**
 * What Wakatta? is and does, before sign-in. Always both languages: Japanese leads, English sits under it, smaller.
 * Figures from the PRD research (MHLW 2025 外国人雇用実態調査).
 */
@Composable
fun IntroScreen(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val pager = rememberPagerState { PAGES }
    val scope = rememberCoroutineScope()
    val last = pager.currentPage == PAGES - 1
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Wakatta?",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onDone, modifier = Modifier.heightIn(min = 48.dp)) {
                    BiLabel(R.string.auth_intro_skip_ja, R.string.auth_intro_skip_en)
                }
            }
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
                // Each visual plays its entrance when its page settles.
                val active = pager.settledPage == page
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    Panel {
                        when (page) {
                            0 -> ChatVisual(active)
                            1 -> StatVisual(active)
                            2 -> StepsVisual(active)
                            else -> TownVisual(active)
                        }
                    }
                    Spacer(Modifier.height(28.dp))
                    val (title, body) = when (page) {
                        0 -> (R.string.auth_intro_1_title_ja to R.string.auth_intro_1_title_en) to
                            (R.string.auth_intro_1_body_ja to R.string.auth_intro_1_body_en)
                        1 -> (R.string.auth_intro_2_title_ja to R.string.auth_intro_2_title_en) to
                            (R.string.auth_intro_2_body_ja to R.string.auth_intro_2_body_en)
                        2 -> (R.string.auth_intro_3_title_ja to R.string.auth_intro_3_title_en) to
                            (R.string.auth_intro_3_body_ja to R.string.auth_intro_3_body_en)
                        else -> (R.string.auth_intro_4_title_ja to R.string.auth_intro_4_title_en) to
                            (R.string.auth_intro_4_body_ja to R.string.auth_intro_4_body_en)
                    }
                    Text(
                        text = stringResource(title.first),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(title.second),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(body.first),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = stringResource(body.second),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(PAGES) { i ->
                        val on = i <= pager.currentPage
                        val color by animateColorAsState(
                            if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            label = "seg",
                        )
                        val w by animateFloatAsState(if (i == pager.currentPage) 2f else 1f, label = "segw")
                        Box(Modifier.weight(w).height(4.dp).background(color, CircleShape))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    if (last) {
                        BiLabel(R.string.auth_intro_start_ja, R.string.auth_intro_start_en, onPrimary = true)
                    } else {
                        BiLabel(R.string.auth_intro_next_ja, R.string.auth_intro_next_en, onPrimary = true)
                    }
                }
            }
        }
    }
}

/** A button label: Japanese, then English smaller and lighter on the same line. */
@Composable
internal fun BiLabel(@StringRes ja: Int, @StringRes en: Int, onPrimary: Boolean = false) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(stringResource(ja), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.width(6.dp))
        Text(
            stringResource(en),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.graphicsLayer { alpha = if (onPrimary) 0.8f else 0.7f },
        )
    }
}

@Composable
private fun Panel(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        modifier = Modifier.fillMaxWidth().height(280.dp),
    ) {
        Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) { content() }
    }
}

/** 0→1 once the page settles; `delay` staggers the parts of one visual. */
@Composable
private fun appear(active: Boolean, delay: Int): Float {
    val v by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = tween(durationMillis = 420, delayMillis = if (active) delay else 0, easing = FastOutSlowInEasing),
        label = "appear",
    )
    return v
}

private fun Modifier.rise(p: Float) = graphicsLayer {
    alpha = p
    translationY = (1f - p) * 24.dp.toPx()
}

@Composable
private fun ChatVisual(active: Boolean) {
    val a = appear(active, 0)
    val b = appear(active, 350)
    val ok by animateFloatAsState(
        targetValue = if (active) 1f else 0f,
        animationSpec = if (active) spring(dampingRatio = 0.45f, stiffness = 300f) else tween(0),
        label = "ok",
    )
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.rise(a), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Default.Mic, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(8.dp))
            Bubble(stringResource(R.string.auth_intro_1_say_ja), MaterialTheme.colorScheme.primaryContainer)
        }
        Icon(
            Icons.Default.KeyboardArrowDown, null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterHorizontally).graphicsLayer { alpha = b },
        )
        Row(Modifier.fillMaxWidth().rise(b), horizontalArrangement = Arrangement.End) {
            Bubble(stringResource(R.string.auth_intro_1_say_en), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        }
        Row(
            Modifier
                .align(Alignment.End)
                .graphicsLayer { scaleX = ok; scaleY = ok; alpha = ok.coerceIn(0f, 1f) }
                .background(StatusColors.GreenContainer, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Check, null, tint = StatusColors.Green, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                stringResource(R.string.auth_intro_1_ok_ja),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = StatusColors.OnGreenContainer,
            )
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.auth_intro_1_ok_en), style = MaterialTheme.typography.labelSmall, color = StatusColors.OnGreenContainer)
        }
    }
}

@Composable
private fun Bubble(text: String, bg: Color, fg: Color = MaterialTheme.colorScheme.onSurface) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = fg,
        modifier = Modifier
            .background(bg, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    )
}

@Composable
private fun StatVisual(active: Boolean) {
    val fill by animateFloatAsState(
        targetValue = if (active) 0.462f else 0f,
        animationSpec = tween(900, delayMillis = if (active) 150 else 0, easing = FastOutSlowInEasing),
        label = "fill",
    )
    val t = appear(active, 0)
    Column(Modifier.fillMaxWidth().rise(t)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "%.1f".format(fill * 100),
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "%",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 10.dp, start = 2.dp),
            )
        }
        Box(Modifier.fillMaxWidth().height(10.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape)) {
            Box(Modifier.fillMaxWidth(fill).fillMaxHeight().background(MaterialTheme.colorScheme.primary, CircleShape))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.auth_intro_2_stat_ja),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            stringResource(R.string.auth_intro_2_stat_en),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.auth_intro_2_src_ja) + " · " + stringResource(R.string.auth_intro_2_src_en),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StepsVisual(active: Boolean) {
    val steps = listOf(
        R.string.auth_intro_3_s1_ja to R.string.auth_intro_3_s1_en,
        R.string.auth_intro_3_s2_ja to R.string.auth_intro_3_s2_en,
        R.string.auth_intro_3_s3_ja to R.string.auth_intro_3_s3_en,
    )
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        steps.forEachIndexed { i, (ja, en) ->
            Row(Modifier.rise(appear(active, i * 220)), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${i + 1}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(ja), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(stringResource(en), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (i == 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(StatusColors.Green, StatusColors.Orange, StatusColors.Red).forEach {
                            Box(Modifier.size(14.dp).background(it, CircleShape))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TownVisual(active: Boolean) {
    val tiles: List<Triple<ImageVector, Int, Int>> = listOf(
        Triple(Icons.Default.Notifications, R.string.auth_intro_4_t1_ja, R.string.auth_intro_4_t1_en),
        Triple(Icons.Default.Person, R.string.auth_intro_4_t2_ja, R.string.auth_intro_4_t2_en),
        Triple(Icons.Default.Face, R.string.auth_intro_4_t3_ja, R.string.auth_intro_4_t3_en),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        tiles.forEachIndexed { i, (icon, ja, en) ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f).rise(appear(active, i * 160)),
            ) {
                Column(Modifier.padding(vertical = 18.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(48.dp).background(MaterialTheme.colorScheme.surfaceContainerLowest, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) }
                    Spacer(Modifier.height(10.dp))
                    Text(stringResource(ja), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(stringResource(en), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
