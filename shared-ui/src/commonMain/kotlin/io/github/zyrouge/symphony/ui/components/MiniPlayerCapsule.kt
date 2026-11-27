package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** User-visible labels of the mini player; the platform supplies translations. */
data class MiniPlayerLabels(
    val play: String,
    val pause: String,
    val previous: String,
    val next: String,
    val seekBack: String,
    val seekForward: String,
)

/** Callbacks of the mini player. */
data class MiniPlayerActions(
    val onOpen: () -> Unit,
    val onPlayPause: () -> Unit,
    val onPrevious: () -> Unit,
    val onNext: () -> Unit,
    val onSeekBack: () -> Unit,
    val onSeekForward: () -> Unit,
)

/**
 * The floating mini player (DESIGN.md 4.2): artwork, title and artists, a
 * progress ring around the play button and optional previous/next and
 * seek-back/seek-forward controls. The platform supplies the artwork.
 */
@Composable
fun BentoMiniPlayerCapsule(
    song: UiSong,
    isPlaying: Boolean,
    progressRatio: Float,
    artwork: @Composable (Modifier) -> Unit,
    short: Boolean,
    showPrevious: Boolean,
    trackControls: Boolean,
    seekControls: Boolean,
    textMarquee: Boolean,
    seekBackSeconds: Int,
    seekForwardSeconds: Int,
    labels: MiniPlayerLabels,
    actions: MiniPlayerActions,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val shape = RoundedCornerShape(BentoRadii.CAPSULE.dp)
    val artSize = if (short) 40.dp else BentoSizes.CAPSULE_ART.dp
    // At a large font or content scale only the play button stays beside the title,
    // so the title keeps a readable stretch; the player has the other controls.
    val density = LocalDensity.current
    val windowWidthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp().value }
    val crowded = bentoTooNarrowForText(windowWidthDp - 32f, density.fontScale)

    Row(
        modifier = modifier
            .widthIn(
                max = when {
                    short -> 480.dp
                    else -> BentoSizes.CAPSULE_MAX_WIDTH.dp
                },
            )
            .fillMaxWidth()
            .height(
                when {
                    short -> BentoSizes.CAPSULE_HEIGHT_SHORT.dp
                    else -> BentoSizes.CAPSULE_HEIGHT.dp
                },
            )
            .bentoFloatingSurface(shape, colors.high)
            .clickable(onClick = actions.onOpen)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        artwork(
            Modifier
                .size(artSize)
                .clip(CircleShape),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
        ) {
            Text(
                song.title,
                style = BentoTypography.rowTitle.copy(fontFamily = appFontFamily),
                color = colors.fg,
                maxLines = 1,
                overflow = if (textMarquee) TextOverflow.Clip else TextOverflow.Ellipsis,
                modifier = if (textMarquee) {
                    Modifier.basicMarquee(iterations = Int.MAX_VALUE)
                } else {
                    Modifier
                },
            )
            Text(
                song.artists.joinToString(),
                style = BentoTypography.secondary.copy(fontFamily = appFontFamily),
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showPrevious && trackControls && !crowded) {
            CapsuleIconButton(
                icon = { Icon(Icons.Filled.SkipPrevious, labels.previous, tint = colors.sub) },
                label = labels.previous,
                onClick = actions.onPrevious,
            )
        }
        if (seekControls && !crowded) {
            CapsuleIconButton(
                icon = { Icon(Icons.Filled.FastRewind, labels.seekBack, tint = colors.sub) },
                label = labels.seekBack,
                onClick = actions.onSeekBack,
            )
        }
        Box(
            modifier = Modifier
                .size(BentoSizes.CAPSULE_PLAY.dp)
                .clickable(onClick = actions.onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            // Position updates land ten times a second; interpolate them so the
            // ring sweeps at frame rate instead of jumping in visible steps.
            val sweepRatioState = animateFloatAsState(
                targetValue = progressRatio,
                animationSpec = tween(durationMillis = 100, easing = LinearEasing),
                label = "bento-capsule-sweep",
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val sweepRatio = sweepRatioState.value
                val strokeWidth = 3.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val topLeft = Offset(center.x - radius, center.y - radius)
                val arcSize = Size(radius * 2f, radius * 2f)
                drawCircle(
                    color = colors.track,
                    radius = radius,
                    style = Stroke(width = strokeWidth),
                )
                drawArc(
                    color = colors.accent,
                    startAngle = -90f,
                    sweepAngle = 360f * sweepRatio,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            BentoPlayButton(
                playing = isPlaying,
                size = 36.dp,
                iconSize = 20.dp,
                label = if (isPlaying) labels.pause else labels.play,
                onClick = actions.onPlayPause,
            )
        }
        if (seekControls && !crowded) {
            CapsuleIconButton(
                icon = { Icon(Icons.Filled.FastForward, labels.seekForward, tint = colors.sub) },
                label = labels.seekForward,
                onClick = actions.onSeekForward,
            )
        }
        if (trackControls && !crowded) {
            CapsuleIconButton(
                icon = { Icon(Icons.Filled.SkipNext, labels.next, tint = colors.sub) },
                label = labels.next,
                onClick = actions.onNext,
            )
        }
    }
}

@Composable
private fun CapsuleIconButton(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(BentoSizes.TOUCH_TARGET_MIN.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}
