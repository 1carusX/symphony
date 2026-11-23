package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.bentoFloatingSurface
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** The floating mini player (DESIGN.md 4.2), collapsible to a round button. */
@Composable
internal fun BentoPlayerCapsule(
    context: ViewContext,
    data: BentoPlayerData,
    short: Boolean,
    showPrevious: Boolean,
    onOpen: () -> Unit,
) {
    ExpandedCapsule(
        context = context,
        data = data,
        short = short,
        showPrevious = showPrevious,
        onOpen = onOpen,
    )
}

@Composable
private fun ExpandedCapsule(
    context: ViewContext,
    data: BentoPlayerData,
    short: Boolean,
    showPrevious: Boolean,
    onOpen: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val shape = RoundedCornerShape(BentoRadii.CAPSULE.dp)
    val artSize = if (short) 40.dp else BentoSizes.CAPSULE_ART.dp
    val trackControls by context.symphony.settings.miniPlayerTrackControls.flow.collectAsState()
    val seekControls by context.symphony.settings.miniPlayerSeekControls.flow.collectAsState()
    val textMarquee by context.symphony.settings.miniPlayerTextMarquee.flow.collectAsState()
    val seekBack by context.symphony.settings.seekBackDuration.flow.collectAsState()
    val seekForward by context.symphony.settings.seekForwardDuration.flow.collectAsState()

    Row(
        modifier = Modifier
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
            .clickable(onClick = onOpen)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            data.song.createArtworkImageRequest(context.symphony).build(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(artSize)
                .clip(CircleShape),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
        ) {
            Text(
                data.song.title,
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
                data.song.artists.joinToString(),
                style = BentoTypography.secondary.copy(fontFamily = appFontFamily),
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showPrevious && trackControls) {
            BentoCapsuleIconButton(
                icon = { Icon(Icons.Filled.SkipPrevious, "Previous", tint = colors.sub) },
                label = context.symphony.t.Previous,
                onClick = { context.symphony.radio.shorty.previous() },
            )
        }
        if (seekControls) {
            BentoCapsuleIconButton(
                icon = { Icon(Icons.Filled.FastRewind, "Back $seekBack seconds", tint = colors.sub) },
                label = "Back $seekBack seconds",
                onClick = { context.symphony.radio.shorty.seekFromCurrent(-seekBack) },
            )
        }
        Box(
            modifier = Modifier
                .size(BentoSizes.CAPSULE_PLAY.dp)
                .clickable { context.symphony.radio.shorty.playPause() },
            contentAlignment = Alignment.Center,
        ) {
            // Position updates land ten times a second; interpolate them so the
            // ring sweeps at frame rate instead of jumping in visible steps.
            val sweepRatioState = animateFloatAsState(
                targetValue = data.playbackPosition.ratio,
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
                playing = data.isPlaying,
                size = 36.dp,
                iconSize = 20.dp,
                label = if (data.isPlaying) "Pause" else "Play",
                onClick = { context.symphony.radio.shorty.playPause() },
            )
        }
        if (seekControls) {
            BentoCapsuleIconButton(
                icon = { Icon(Icons.Filled.FastForward, "Forward $seekForward seconds", tint = colors.sub) },
                label = "Forward $seekForward seconds",
                onClick = { context.symphony.radio.shorty.seekFromCurrent(seekForward) },
            )
        }
        if (trackControls) {
            BentoCapsuleIconButton(
                icon = { Icon(Icons.Filled.SkipNext, "Next", tint = colors.sub) },
                label = context.symphony.t.Next,
                onClick = { context.symphony.radio.shorty.skip() },
            )
        }
    }
}

@Composable
private fun BentoCapsuleIconButton(
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

/** Accent play button with the paused-square/playing-circle morph token. */
@Composable
internal fun BentoPlayButton(
    playing: Boolean,
    size: Dp,
    iconSize: Dp,
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(colors.accent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (playing) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            label,
            tint = colors.onAccent,
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun CollapsedCapsule(
    context: ViewContext,
    data: BentoPlayerData,
    buttonSize: Dp,
    onExpand: () -> Unit,
    onOpen: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(buttonSize)
            .pointerInput(Unit) {
                var dragged = 0f
                detectHorizontalDragGestures { change, dragAmount ->
                    change.consume()
                    dragged += dragAmount
                    if (dragged < -56.dp.toPx()) {
                        onExpand()
                        dragged = 0f
                    }
                }
            }
            .clickable(onClick = onExpand),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 3.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f - 1.dp.toPx()
            drawCircle(
                color = colors.track,
                radius = radius,
                style = Stroke(width = strokeWidth),
            )
            drawArc(
                color = colors.accent,
                startAngle = -90f,
                sweepAngle = 360f * data.playbackPosition.ratio,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )
        }
        AsyncImage(
            remember(data.song.id) { data.song.createArtworkImageRequest(context.symphony).build() },
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(buttonSize - 14.dp)
                .clip(CircleShape),
        )
    }
}
