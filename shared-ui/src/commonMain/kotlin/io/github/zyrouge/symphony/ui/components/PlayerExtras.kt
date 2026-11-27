package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

private val PlayerExtraLabelStyle = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold)

/** A labelled extra button's padding (14 dp a side), icon (20 dp) and icon gap (6 dp). */
private const val PLAYER_EXTRA_BUTTON_CHROME_DP = 54

/**
 * The player's extra buttons: lyrics, queue and more. The last one opens the
 * platform's more menu through [menu]. Labels drop to icons only when the row
 * would not fit.
 */
@Composable
fun BentoPlayerExtras(
    lyricsLabel: String,
    queueLabel: String,
    moreLabel: String,
    lyricsSelected: Boolean,
    queueSelected: Boolean,
    onLyricsClick: () -> Unit,
    onQueueClick: () -> Unit,
    menu: @Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMore by remember { mutableStateOf(false) }
    val labels = listOf(lyricsLabel, queueLabel, moreLabel)
    val textMeasurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // At a large font or content scale the labelled buttons stop fitting and the
        // last one would be pushed out of reach, so they drop to icons only.
        val chromeWidth = with(LocalDensity.current) {
            (PLAYER_EXTRA_BUTTON_CHROME_DP * labels.size).dp.roundToPx()
        }
        val labelledWidth = chromeWidth + labels.sumOf {
            textMeasurer.measure(it, PlayerExtraLabelStyle, maxLines = 1).size.width
        }
        val showLabels = labelledWidth <= constraints.maxWidth
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerExtraButton(
                icon = { Icon(Icons.AutoMirrored.Outlined.Article, null) },
                label = lyricsLabel,
                selected = lyricsSelected,
                showLabel = showLabels,
                onClick = onLyricsClick,
            )
            PlayerExtraButton(
                icon = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, null) },
                label = queueLabel,
                selected = queueSelected,
                showLabel = showLabels,
                onClick = onQueueClick,
            )
            Box {
                PlayerExtraButton(
                    icon = { Icon(Icons.Outlined.MoreHoriz, null) },
                    label = moreLabel,
                    selected = null,
                    showLabel = showLabels,
                    onClick = { showMore = true },
                )
                menu(showMore) { showMore = false }
            }
        }
    }
}

@Composable
private fun PlayerExtraButton(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean?,
    showLabel: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(BentoRadii.CHIP.dp))
            .background(if (selected == true) colors.tonal else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp)
            .semantics {
                role = Role.Button
                if (!showLabel) contentDescription = label
                selected?.let {
                    toggleableState = if (it) ToggleableState.On else ToggleableState.Off
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides
                if (selected == true) colors.onTonal else colors.sub,
        ) {
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                icon()
            }
        }
        if (showLabel) {
            Text(
                label,
                style = PlayerExtraLabelStyle,
                color = if (selected == true) colors.onTonal else colors.sub,
                maxLines = 1,
            )
        }
    }
}
