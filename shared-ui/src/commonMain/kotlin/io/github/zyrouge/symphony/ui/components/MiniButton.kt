package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * A mini Play/Shuffle button of the results header (DESIGN.md 4.3): 36 dp,
 * radius 18, accent or tonal.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoMiniButton(
    icon: ImageVector,
    label: String,
    tone: BentoTileTone,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (bg, fg) = when (tone) {
        BentoTileTone.ACCENT -> colors.accent to colors.onAccent
        BentoTileTone.TONAL -> colors.tonal to colors.onTonal
        else -> colors.surface to colors.fg
    }
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(BentoRadii.CHIP.dp))
            .background(bg)
            .combinedClickable(onClick = onClick)
            .padding(start = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, label, tint = fg, modifier = Modifier.size(18.dp))
        Text(
            label,
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
        )
    }
}
