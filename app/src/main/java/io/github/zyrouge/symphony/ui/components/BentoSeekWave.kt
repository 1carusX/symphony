package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import io.github.zyrouge.symphony.services.radio.RadioPlayer
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils
import kotlin.math.sin

/**
 * The tile seek bar (DESIGN.md 4.3): an accent wave on a `track` line with a
 * 4 x 22 thumb; the wave animates while playing and flattens into a 4 dp
 * line while paused. Tap and drag seek the current song.
 */
@Composable
fun BentoSeekWave(
    context: ViewContext,
    position: RadioPlayer.PlaybackPosition,
    playing: Boolean,
    height: Dp,
    accent: Color = LocalBentoColors.current.accent,
    track: Color = LocalBentoColors.current.track,
) {
    val hasPlayer = context.symphony.radio.hasPlayer
    // Playback position only arrives every 100 ms (RadioPlayer timer). Interpolate
    // it so the wave slides at frame rate instead of stepping ten times a second.
    val ratioState = animateFloatAsState(
        targetValue = position.ratio,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "bento-seek-ratio",
    )
    val wavePhase = rememberWavePhase(playing)
    // The pointer handlers outlive recompositions, so they read the latest values.
    val latestPosition by rememberUpdatedState(position)
    val latestHasPlayer by rememberUpdatedState(hasPlayer)
    // While dragging, the thumb follows the finger and the song is sought once on release.
    var dragRatio by remember { mutableStateOf<Float?>(null) }
    val onSeekAtRatio: (Float) -> Unit = { ratio ->
        if (latestHasPlayer) {
            context.symphony.radio.seek((latestPosition.total * ratio.coerceIn(0f, 1f)).toLong())
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                stateDescription = DurationUtils.formatMs(position.played)
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
            val ratio = dragRatio ?: ratioState.value
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

/**
 * An equalizer glyph (DESIGN.md 5.1): three bars that bounce while playing
 * and rest at fixed heights otherwise.
 */
@Composable
fun BentoEqualizerGlyph(color: Color, playing: Boolean) {
    val phase = when {
        playing -> {
            val transition = rememberInfiniteTransition(label = "bento-eq")
            val animated by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "bento-eq-phase",
            )
            animated
        }

        else -> 0f
    }
    Canvas(modifier = Modifier.size(13.dp, 14.dp)) {
        val barWidth = 3.dp.toPx()
        val gap = 2.dp.toPx()
        val restHeights = listOf(0.45f, 0.85f, 0.6f)
        val delays = listOf(0.35f, 0f, 0.6f)
        restHeights.forEachIndexed { i, restHeight ->
            val heightFraction = when {
                playing -> {
                    val t = ((phase + delays[i]) % 1f) * 2f
                    val triangle = when {
                        t < 1f -> t
                        else -> 2f - t
                    }
                    0.2f + 0.8f * triangle
                }

                else -> restHeight
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (barWidth + gap), size.height * (1f - heightFraction)),
                size = Size(barWidth, size.height * heightFraction),
                cornerRadius = CornerRadius(1.dp.toPx()),
            )
        }
    }
}

private const val TAU = (2.0 * Math.PI).toFloat()

private val BentoWavePhase.value: Float
    get() = when (this) {
        is BentoWavePhase.Animated -> phase.value
        is BentoWavePhase.Resting -> phase
    }
