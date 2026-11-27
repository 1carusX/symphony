package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * A smart filter tile (DESIGN.md 5.5): 64 dp (56 in the side column), an
 * icon well, the filter name and its song count; the active filter turns
 * accent and also orders the results.
 */
@Composable
fun BentoSmartFilterTile(
    icon: ImageVector,
    label: String,
    count: String,
    active: Boolean,
    sideColumn: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (fg, subColor, well) = when {
        active -> Triple(colors.onAccent, colors.onAccentSub, colors.onAccent)
        else -> Triple(colors.fg, colors.sub, colors.fg)
    }
    BentoToneSurface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = when {
                    sideColumn -> BentoSizes.SMART_FILTER_SIDE_COLUMN.dp
                    else -> BentoSizes.SMART_FILTER.dp
                },
            ),
        tone = when {
            active -> BentoTileTone.ACCENT
            else -> BentoTileTone.PLAIN
        },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(BentoRadii.ICON_WELL_40.dp))
                    .background(well.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, label, tint = fg, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    label,
                    style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    count,
                    style = BentoTypography.secondary,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
