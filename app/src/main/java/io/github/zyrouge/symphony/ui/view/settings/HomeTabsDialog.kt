package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.view.HomePage

@Composable
fun HomeTabsDialog(context: ViewContext, onDismiss: () -> Unit) {
    val homeTabs by context.symphony.settings.homeTabs.flow.collectAsState()
    val scheme = LocalBentoColorScheme.current
    val colors = LocalBentoColors.current

    BentoDialog(
        title = context.symphony.t.HomeTabs,
        onDismiss = onDismiss,
    ) {
        Text(
            context.symphony.t.SelectAtLeast2Tabs,
            style = BentoTypography.secondary,
            color = colors.sub,
        )
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HomePage.entries.chunked(2).forEach { rowEntries ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowEntries.forEach { page ->
                        val selected = page in homeTabs
                        val canToggle = !selected || homeTabs.size > 2
                        HomeTabOption(
                            label = page.label(context),
                            selected = selected,
                            enabled = canToggle,
                            ring = scheme == BentoColorScheme.CONTRAST,
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (!canToggle) return@HomeTabOption
                                context.symphony.settings.homeTabs.setValue(
                                    if (selected) homeTabs - page else homeTabs + page,
                                )
                            },
                        )
                    }
                    if (rowEntries.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTabOption(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    ring: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val fill = when {
        selected -> colors.tonal
        else -> colors.raised
    }
    val text = when {
        selected -> colors.onTonal
        enabled -> colors.fg
        else -> colors.muted
    }
    val shape = RoundedCornerShape(BentoRadii.TILE_SMALL.dp)
    val ringModifier = when {
        ring -> Modifier.border(1.dp, colors.line, shape)
        else -> Modifier
    }
    Row(
        modifier = modifier
            .heightIn(min = 46.dp)
            .clip(shape)
            .then(ringModifier)
            .background(fill)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Medium),
            color = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(Icons.Rounded.Check, null, tint = colors.onTonal)
        }
    }
}
