package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** Labels of the player transport, already localized by the caller. */
@Immutable
data class TransportLabels(
    val playMode: String,
    val previous: String,
    val back: String,
    val play: String,
    val pause: String,
    val forward: String,
    val next: String,
    val sleepTimer: String,
)

/**
 * The player transport row (DESIGN.md 4.3): loop mode, previous, play/pause
 * and next, plus seek buttons when [seekControls] is on and an optional sleep
 * timer button with its remaining-minutes badge.
 */
@Composable
fun BentoPlayerTransport(
    mode: PlayerMode,
    isPlaying: Boolean,
    short: Boolean,
    labels: TransportLabels,
    seekControls: Boolean,
    seekBackSeconds: Int,
    seekForwardSeconds: Int,
    sleepTimerBadge: String?,
    sleepTimerActive: Boolean,
    onCycleMode: () -> Unit,
    onPrevious: () -> Unit,
    onSeekBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onNext: () -> Unit,
    onSleepTimerClick: () -> Unit,
    onSleepTimerLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSleepTimer: Boolean = true,
) {
    val colors = LocalBentoColors.current
    val playSize = if (short) BentoSizes.PLAY_BUTTON_MD else BentoSizes.PLAY_BUTTON_LG
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // Shrink every button by the same factor when the row would not fit, e.g. with the
        // seek buttons on a 360 dp phone or in the small floating window.
        val scale = bentoTransportScale(maxWidth.value, playSize, seekControls)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BentoPlayerIconButton(
                icon = when (mode) {
                    PlayerMode.REPEAT_ALL, PlayerMode.REPEAT_OFF -> Icons.Filled.Repeat
                    PlayerMode.REPEAT_ONE -> Icons.Filled.RepeatOne
                    PlayerMode.SHUFFLE -> Icons.Filled.Shuffle
                },
                label = labels.playMode,
                size = BentoSizes.ICON_BUTTON.dp * scale,
                iconSize = BentoSizes.ICON.dp,
                tint = if (mode == PlayerMode.REPEAT_OFF) colors.muted else colors.sub,
                onClick = onCycleMode,
            )
            BentoPlayerIconButton(
                icon = Icons.Filled.SkipPrevious,
                label = labels.previous,
                size = BentoSizes.PREV_NEXT_BUTTON.dp * scale,
                iconSize = BentoSizes.PREV_NEXT_ICON.dp * scale,
                tint = colors.fg,
                onClick = onPrevious,
            )
            if (seekControls) {
                BentoPlayerIconButton(
                    icon = Icons.Filled.FastRewind,
                    label = labels.back,
                    size = BentoSizes.ICON_BUTTON.dp * scale,
                    iconSize = BentoSizes.ICON.dp,
                    tint = colors.sub,
                    onClick = onSeekBack,
                )
            }
            BentoPlayButton(
                playing = isPlaying,
                size = playSize.dp * scale,
                iconSize = (if (short) 24.dp else 34.dp) * scale,
                label = if (isPlaying) labels.pause else labels.play,
                onClick = onPlayPause,
            )
            if (seekControls) {
                BentoPlayerIconButton(
                    icon = Icons.Filled.FastForward,
                    label = labels.forward,
                    size = BentoSizes.ICON_BUTTON.dp * scale,
                    iconSize = BentoSizes.ICON.dp,
                    tint = colors.sub,
                    onClick = onSeekForward,
                )
            }
            BentoPlayerIconButton(
                icon = Icons.Filled.SkipNext,
                label = labels.next,
                size = BentoSizes.PREV_NEXT_BUTTON.dp * scale,
                iconSize = BentoSizes.PREV_NEXT_ICON.dp * scale,
                tint = colors.fg,
                onClick = onNext,
            )
            if (showSleepTimer) {
                BentoPlayerIconButton(
                    icon = Icons.Outlined.Timer,
                    label = labels.sleepTimer,
                    size = BentoSizes.ICON_BUTTON.dp * scale,
                    iconSize = BentoSizes.ICON.dp,
                    tint = if (sleepTimerActive) colors.onTonal else colors.sub,
                    background = if (sleepTimerActive) colors.tonal else Color.Transparent,
                    selected = sleepTimerActive,
                    badge = sleepTimerBadge,
                    onClick = onSleepTimerClick,
                    onLongClick = onSleepTimerLongClick,
                )
            }
        }
    }
}

/**
 * Scales the transport buttons so the whole row fits [availableWidthDp]: the
 * loop and sleep buttons, the play button, plus back and forward when
 * [seekControls] is on.
 */
fun bentoTransportScale(availableWidthDp: Float, playSizeDp: Int, seekControls: Boolean): Float {
    val iconButtons = if (seekControls) 4 else 2
    val naturalWidth = iconButtons * BentoSizes.ICON_BUTTON +
        2 * BentoSizes.PREV_NEXT_BUTTON +
        playSizeDp
    return (availableWidthDp / naturalWidth).coerceIn(0f, 1f)
}
