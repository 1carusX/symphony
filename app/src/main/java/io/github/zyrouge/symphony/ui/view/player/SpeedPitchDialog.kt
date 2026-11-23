package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetSwitchRow
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.roundToInt

/** The slider's stops; it snaps to these and nothing in between. */
internal val playbackRateStops = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

/** Index of the stop closest to [value], for rates set before the slider snapped. */
internal fun nearestPlaybackRateStop(value: Float): Int =
    playbackRateStops.indices.minBy { abs(playbackRateStops[it] - value) }

/** Width of the custom thumb; the Material slider insets its track by half of it. */
private val PlaybackRateThumbWidth = 14.dp

/** "Speed & Pitch", the title of the sheet and of its player menu item. */
internal fun speedAndPitchLabel(context: ViewContext) =
    "${context.symphony.t.Speed} & ${context.symphony.t.Pitch}"

/** "1×", "1.25×", "0.5×". */
internal fun formatPlaybackRate(value: Float): String =
    BigDecimal(value.toDouble())
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString() + "×"

/**
 * Speed and pitch in one sheet: a slider for each that snaps to the preset
 * stops (0.5x to 2x), the persist-until-queue-end switch, Reset and Done.
 * Changes apply while dragging.
 */
@Composable
fun NowPlayingSpeedPitchDialog(
    context: ViewContext,
    currentSpeed: Float,
    currentPitch: Float,
    persistedSpeed: Float,
    persistedPitch: Float,
    onDismissRequest: () -> Unit,
) {
    var persistent by remember {
        mutableStateOf(currentSpeed == persistedSpeed && currentPitch == persistedPitch)
    }

    BentoSheetDialog(
        title = speedAndPitchLabel(context),
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Reset,
                onClick = {
                    context.symphony.radio.setSpeed(1f, persistent)
                    context.symphony.radio.setPitch(1f, persistent)
                },
            )
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        PlaybackRateControl(
            label = context.symphony.t.Speed,
            value = currentSpeed,
            onChange = { context.symphony.radio.setSpeed(it, persistent) },
        )
        PlaybackRateControl(
            label = context.symphony.t.Pitch,
            value = currentPitch,
            onChange = { context.symphony.radio.setPitch(it, persistent) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.PersistUntilQueueEnd,
            checked = persistent,
            onCheckedChange = {
                persistent = it
                context.symphony.radio.setSpeed(currentSpeed, it)
                context.symphony.radio.setPitch(currentPitch, it)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackRateControl(
    label: String,
    value: Float,
    onChange: (Float) -> Unit,
) {
    val colors = LocalBentoColors.current
    val lastStop = playbackRateStops.lastIndex
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = BentoTypography.rowTitle, color = colors.fg)
            Text(
                formatPlaybackRate(value),
                style = BentoTypography.body.copy(
                    fontWeight = BentoTypography.rowTitle.fontWeight,
                    fontFeatureSettings = "tnum",
                ),
                color = colors.accent,
            )
        }
        // The stops sit evenly along the track whatever their values, so the
        // slider works on stop indexes and snaps with Material's steps.
        Slider(
            value = nearestPlaybackRateStop(value).toFloat(),
            onValueChange = { onChange(playbackRateStops[it.roundToInt().coerceIn(0, lastStop)]) },
            valueRange = 0f..lastStop.toFloat(),
            steps = lastStop - 1,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = label
                    stateDescription = formatPlaybackRate(value)
                },
            thumb = {
                // A 6 x 28 handle with a ring of the sheet color, as the prototype.
                Box(
                    modifier = Modifier
                        .size(width = PlaybackRateThumbWidth, height = 36.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(colors.`float`),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 6.dp, height = 28.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.accent),
                    )
                }
            },
            track = { state ->
                val fraction = (state.value / lastStop).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.track),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .background(colors.accent),
                    )
                    // One dot per stop, so the slider reads as notched rather than smooth.
                    Canvas(modifier = Modifier.matchParentSize()) {
                        val radius = 2.dp.toPx()
                        val inset = size.height / 2f
                        for (stop in 0..lastStop) {
                            val x = inset + (size.width - inset * 2f) * stop / lastStop
                            drawCircle(
                                color = if (stop <= state.value) colors.onAccent else colors.sub,
                                radius = radius,
                                center = Offset(x, size.height / 2f),
                            )
                        }
                    }
                }
            },
        )
        PlaybackRateStopLabels(value = value)
    }
}

/**
 * The stop values under the track, each centred on its stop: the Material
 * slider insets the track by half the thumb width on both sides.
 */
@Composable
private fun PlaybackRateStopLabels(value: Float) {
    val colors = LocalBentoColors.current
    val current = nearestPlaybackRateStop(value)
    val lastStop = playbackRateStops.lastIndex
    Layout(
        modifier = Modifier.fillMaxWidth(),
        content = {
            playbackRateStops.forEachIndexed { index, stop ->
                Text(
                    formatPlaybackRate(stop),
                    style = BentoTypography.label.copy(fontFeatureSettings = "tnum"),
                    color = if (index == current) colors.accent else colors.sub,
                    maxLines = 1,
                )
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val width = constraints.maxWidth
        val inset = (PlaybackRateThumbWidth / 2).roundToPx()
        val height = placeables.maxOf { it.height }
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val center = inset + (width - inset * 2) * index / lastStop
                placeable.place(
                    (center - placeable.width / 2).coerceIn(0, width - placeable.width),
                    0,
                )
            }
        }
    }
}
