package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.components.PlayerMode
import io.github.zyrouge.symphony.ui.components.TransportLabels

/** The play-mode label shown in the transport row's mode button. */
fun playerTransportPlayModeLabel(t: Strings, mode: PlayerMode) = t.PlayModeX(
    when (mode) {
        PlayerMode.REPEAT_OFF -> t.RepeatOff
        PlayerMode.REPEAT_ALL -> t.RepeatAll
        PlayerMode.REPEAT_ONE -> t.RepeatOne
        PlayerMode.SHUFFLE -> t.Shuffle
    },
)

/**
 * The transport labels for the shared row: mode, previous/play/pause/next,
 * the seek-button labels and the sleep timer (remaining minutes or "off").
 */
fun playerTransportLabels(
    t: Strings,
    mode: PlayerMode,
    seekBackSeconds: Int,
    seekForwardSeconds: Int,
    sleepTimerMinutes: Int? = null,
) = TransportLabels(
    playMode = playerTransportPlayModeLabel(t, mode),
    previous = t.Previous,
    back = t.BackXSeconds(seekBackSeconds.toString()),
    play = t.Play,
    pause = t.Pause,
    forward = t.ForwardXSeconds(seekForwardSeconds.toString()),
    next = t.Next,
    sleepTimer = sleepTimerMinutes?.let {
        t.SleepTimerXMinutesLeft(it.toString())
    } ?: t.SleepTimerOff,
)

/**
 * The seek row, transport row and a platform extras row in one column, the
 * shared assembly used by the phone sheet and the desktop player.
 */
@Composable
fun PlayerControlsContent(
    seek: @Composable () -> Unit,
    transport: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = 20.dp,
        end = 20.dp,
        top = 4.dp,
        bottom = 16.dp,
    ),
    extras: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        seek()
        transport()
        extras()
    }
}
