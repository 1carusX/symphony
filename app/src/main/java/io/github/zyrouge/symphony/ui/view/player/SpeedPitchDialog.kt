package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetChip
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetSwitchRow
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.roundToInt

private val playbackRatePresets = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
private val playbackRateRange = 0.5f..3f

/** Slider positions are 0.05 apart. */
private const val PLAYBACK_RATE_STEPS_PER_UNIT = 20

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
 * Speed and pitch in one sheet: a slider and presets for each, the
 * persist-until-queue-end switch, Reset and Done. Changes apply while
 * dragging.
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlaybackRateControl(
    label: String,
    value: Float,
    onChange: (Float) -> Unit,
) {
    val colors = LocalBentoColors.current
    val steps = ((playbackRateRange.endInclusive - playbackRateRange.start) *
        PLAYBACK_RATE_STEPS_PER_UNIT).roundToInt() - 1
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
        Slider(
            value = value.coerceIn(playbackRateRange),
            onValueChange = {
                onChange((it * PLAYBACK_RATE_STEPS_PER_UNIT).roundToInt() /
                    PLAYBACK_RATE_STEPS_PER_UNIT.toFloat())
            },
            valueRange = playbackRateRange,
            steps = steps,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
            thumb = {
                // A 6 x 28 handle with a ring of the sheet color, as the prototype.
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 36.dp)
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
                val fraction = ((state.value - playbackRateRange.start) /
                    (playbackRateRange.endInclusive - playbackRateRange.start))
                    .coerceIn(0f, 1f)
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
                }
            },
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            playbackRatePresets.forEach { preset ->
                BentoSheetChip(
                    label = formatPlaybackRate(preset),
                    selected = abs(preset - value) < 0.001f,
                    onClick = { onChange(preset) },
                )
            }
        }
    }
}
