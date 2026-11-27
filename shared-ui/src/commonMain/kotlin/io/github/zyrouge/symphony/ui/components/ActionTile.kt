package io.github.zyrouge.symphony.ui.components

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

// ---------------------------------------------------------------------------
// Action tiles (DESIGN.md 5.2)
// ---------------------------------------------------------------------------

/**
 * An action tile (DESIGN.md 5.2): 72 dp high, an icon well beside the label
 * and sub-label. Below 520 dp inner width the non-main tiles stack the icon
 * over the label and drop their sub-label.
 */
@Composable
fun BentoActionTile(
    icon: ImageVector,
    label: String,
    sub: String?,
    tone: BentoTileTone = BentoTileTone.PLAIN,
    main: Boolean = false,
    narrow: Boolean = false,
    fillHeight: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (fg, subColor) = when (tone) {
        BentoTileTone.PLAIN -> colors.fg to colors.sub
        BentoTileTone.RAISED -> colors.fg to colors.sub
        BentoTileTone.ACCENT -> colors.onAccent to colors.onAccentSub
        BentoTileTone.TONAL -> colors.onTonal to colors.tonalSub
    }
    val stacked = narrow && !main
    BentoToneSurface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                // In a grid cell the tile fills the row, so it lines up with its neighbours.
                // Elsewhere it grows with the font scale, like the grid's rows.
                when {
                    fillHeight -> Modifier.fillMaxHeight()
                    else -> Modifier.height(
                        BentoSizes.ACTION_TILE.dp * LocalDensity.current.fontScale.coerceAtLeast(1f),
                    )
                },
            ),
        tone = tone,
        onClick = onClick,
    ) {
        if (stacked) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(icon, label, tint = fg, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                    color = fg,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp),
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(BentoRadii.ICON_WELL_40.dp))
                        .background(fg.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, label, tint = fg, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(
                        label,
                        style = BentoTypography.rowTitle,
                        color = fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (sub != null) {
                        Text(
                            sub,
                            style = BentoTypography.secondary,
                            color = subColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
