package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiPlaybackPosition
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sin

/**
 * The tile seek bar (DESIGN.md 4.3): an accent wave on a `track` line with a
 * 4 x 22 thumb; the wave animates while playing and flattens into a 4 dp
 * line while paused. Tap and drag seek the current song. [onScrub] reports
 * the ratio the thumb shows while it is dragged or waiting for a seek to land,
 * and null once it follows playback again.
 */
@Composable
fun BentoSeekWave(
    position: UiPlaybackPosition,
    playing: Boolean,
    height: Dp,
    seekable: Boolean,
    timeText: (Long) -> String,
    onSeek: (Long) -> Unit,
    accent: Color = LocalBentoColors.current.accent,
    track: Color = LocalBentoColors.current.track,
    onScrub: (Float?) -> Unit = {},
) {
    // Playback position only arrives every 100 ms (RadioPlayer timer). Interpolate
    // it so the wave slides at frame rate instead of stepping ten times a second.
    val ratioState = remember { Animatable(position.ratio) }
    val wavePhase = rememberWavePhase(playing)
    // The pointer handlers outlive recompositions, so they read the latest values.
    val latestPosition by rememberUpdatedState(position)
    val latestSeekable by rememberUpdatedState(seekable)
    val latestOnSeek by rememberUpdatedState(onSeek)
    // While dragging, the thumb follows the finger and the song is sought once on release.
    var dragRatio by remember { mutableStateOf<Float?>(null) }
    // Where the last seek sent the song. The thumb stays there until the player
    // reports a position near it, rather than sliding back from the old position.
    var seekRatio by remember { mutableStateOf<Float?>(null) }
    LaunchedEffect(position) {
        val target = seekRatio
        when {
            target == null -> ratioState.animateTo(
                position.ratio,
                tween(durationMillis = 100, easing = LinearEasing),
            )

            abs(position.ratio - target) * position.durationMillis <= SEEK_SETTLED_MS -> {
                ratioState.snapTo(position.ratio)
                seekRatio = null
            }
        }
    }
    LaunchedEffect(seekRatio) {
        if (seekRatio == null) return@LaunchedEffect
        // The player never got there (no player, or the song changed): follow it again.
        delay(SEEK_TIMEOUT_MS)
        ratioState.snapTo(latestPosition.ratio)
        seekRatio = null
    }
    val latestOnScrub by rememberUpdatedState(onScrub)
    LaunchedEffect(Unit) {
        snapshotFlow { dragRatio ?: seekRatio }.collect { latestOnScrub(it) }
    }
    val onSeekAtRatio: (Float) -> Unit = { ratio ->
        if (latestSeekable) {
            seekRatio = ratio.coerceIn(0f, 1f)
            latestOnSeek((latestPosition.durationMillis * ratio.coerceIn(0f, 1f)).toLong())
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                stateDescription = timeText(position.playedMillis)
                progressBarRangeInfo = ProgressBarRangeInfo(position.ratio, 0f..1f)
            }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onSeekAtRatio(offset.x / size.width.toFloat())
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        dragRatio = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                    },
                    onDragEnd = {
                        dragRatio?.let(onSeekAtRatio)
                        dragRatio = null
                    },
                    onDragCancel = { dragRatio = null },
                ) { change, _ ->
                    change.consume()
                    dragRatio = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ratio = dragRatio ?: seekRatio ?: ratioState.value
            val centerY = size.height / 2f
            val lineHeight = 5.dp.toPx()
            val halfLine = lineHeight / 2f
            val thumbWidth = 5.dp.toPx()
            val thumbHeight = 20.dp.toPx()
            val gap = thumbWidth + 2.dp.toPx()
            val playedWidth = size.width * ratio
            val restWidth = (size.width * (1f - ratio) - gap)
                .coerceAtLeast(0f)
            drawRoundRect(
                color = track,
                topLeft = Offset(size.width - restWidth, centerY - halfLine),
                size = Size(restWidth, lineHeight),
                cornerRadius = CornerRadius(halfLine),
            )
            val playedWidthWithoutThumb = (playedWidth - gap).coerceAtLeast(0f)
            when {
                playing -> {
                    // A stroked sine: one line of constant weight that undulates.
                    // The stroke stays thinner than twice the amplitude so crests and
                    // troughs stay separate instead of fusing into beads.
                    val period = 22.dp.toPx()
                    val amplitude = 2.5.dp.toPx()
                    val phase = wavePhase.value * period
                    val step = 1.dp.toPx()
                    val strokeWidth = 2.dp.toPx()
                    val path = Path()
                    var x = 0f
                    var y = centerY + sin((x + phase) / period * TAU) * amplitude
                    path.moveTo(x, y)
                    while (x < playedWidthWithoutThumb) {
                        x = minOf(x + step, playedWidthWithoutThumb)
                        y = centerY + sin((x + phase) / period * TAU) * amplitude
                        path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = accent,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }

                else -> drawRoundRect(
                    color = accent,
                    topLeft = Offset(0f, centerY - halfLine),
                    size = Size(playedWidthWithoutThumb, lineHeight),
                    cornerRadius = CornerRadius(halfLine),
                )
            }
            drawRoundRect(
                color = accent,
                topLeft = Offset(
                    playedWidth - thumbWidth / 2f,
                    centerY - thumbHeight / 2f,
                ),
                size = Size(thumbWidth, thumbHeight),
                cornerRadius = CornerRadius(thumbWidth / 2f),
            )
        }
    }
}

internal sealed interface BentoWavePhase {
    @Immutable
    data class Animated(val phase: State<Float>) : BentoWavePhase

    @Immutable
    data class Resting(val phase: Float) : BentoWavePhase
}

@Composable
private fun rememberWavePhase(playing: Boolean): BentoWavePhase = when {
    playing -> {
        val transition = rememberInfiniteTransition(label = "bento-seek-wave")
        val phase = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "bento-seek-wave-phase",
        )
        BentoWavePhase.Animated(phase)
    }

    else -> BentoWavePhase.Resting(0f)
}

private const val TAU = (2.0 * Math.PI).toFloat()

/** A reported position this close to a seek's target (ms) means the seek landed. */
private const val SEEK_SETTLED_MS = 1500f

private const val SEEK_TIMEOUT_MS = 1500L

private val BentoWavePhase.value: Float
    get() = when (this) {
        is BentoWavePhase.Animated -> phase.value
        is BentoWavePhase.Resting -> phase
    }
