package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * An artist or album hit above the song results (DESIGN.md 5.5): 60 dp,
 * `raised`, with a 40 dp avatar or artwork. The platform supplies the
 * artwork through [artwork].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoSearchHitChip(
    title: String,
    subtitle: String,
    round: Boolean,
    artwork: @Composable (Modifier) -> Unit,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .height(60.dp)
            .widthIn(max = 240.dp)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(
                when {
                    hovered -> colors.high
                    else -> colors.raised
                },
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        artwork(
            Modifier
                .size(40.dp)
                .clip(
                    when {
                        round -> CircleShape
                        else -> RoundedCornerShape(10.dp)
                    },
                ),
        )
        Column {
            Text(
                title,
                style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                style = BentoTypography.label,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
