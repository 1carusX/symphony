package io.github.zyrouge.symphony.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.East
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.ActivityUtils

@Composable
fun ConsiderContributingTile(context: ViewContext) {
    val colors = LocalBentoColors.current
    val windowInfo = LocalWindowInfo.current
    val compact = windowInfo.containerDpSize.width < 600.dp
    val gutter = if (compact) BentoSpacing.GUTTER_COMPACT.dp else BentoSpacing.GUTTER_OTHER.dp
    // The grid below adds its own 4 dp top inset; these values leave the same
    // 12 or 16 dp gap the tiles have between each other.
    val gapAfter = if (compact) 8.dp else 12.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = gutter, end = gutter, top = 8.dp, bottom = gapAfter)
            .clip(RoundedCornerShape(BentoRadii.TILE.dp))
            .background(colors.raised)
            .clickable {
                ActivityUtils.startBrowserActivity(
                    context.activity,
                    AppMeta.contributingUrl.toUri(),
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Favorite,
            null,
            tint = colors.accent,
            modifier = Modifier.size(14.dp),
        )
        Text(
            context.symphony.t.ConsiderContributing,
            style = BentoTypography.rowTitle,
            color = colors.fg,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Filled.East,
            null,
            tint = colors.sub,
            modifier = Modifier.size(16.dp),
        )
    }
}
