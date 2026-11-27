package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * A tile-shaped surface in one of the bento tones (DESIGN.md 4.1): tone fill,
 * the 7 % hover/press overlay, the radius growing to 30 on hover, and the
 * 1 dp line ring in the Contrast scheme. Unlike [BentoTile] the padding and
 * inner layout belong to the caller.
 */
@Composable
fun BentoToneSurface(
    modifier: Modifier = Modifier,
    tone: BentoTileTone = BentoTileTone.PLAIN,
    radiusDp: Int = BentoRadii.TILE,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val (fill, fg) = when (tone) {
        BentoTileTone.PLAIN -> colors.surface to colors.fg
        BentoTileTone.RAISED -> colors.raised to colors.fg
        BentoTileTone.ACCENT -> colors.accent to colors.onAccent
        BentoTileTone.TONAL -> colors.tonal to colors.onTonal
    }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(
        when {
            hovered -> BentoRadii.TILE_HOVER.dp
            else -> radiusDp.dp
        },
    )
    Box(
        modifier = modifier
            .let { m ->
                when (onClick) {
                    null -> m
                    else -> m
                        .hoverable(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick,
                        )
                }
            }
            .clip(shape)
            .then(
                when (colorScheme) {
                    BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, shape)
                    else -> Modifier
                },
            )
            .background(fill)
            .then(
                when {
                    hovered || pressed -> Modifier.background(fg.copy(alpha = 0.07f))
                    else -> Modifier
                },
            ),
    ) {
        content()
    }
}
