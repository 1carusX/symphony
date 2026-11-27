package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.RadioQueue
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.BentoPlayerExtras as SharedBentoPlayerExtras
import io.github.zyrouge.symphony.ui.components.BentoPlayerTitle as SharedBentoPlayerTitle
import io.github.zyrouge.symphony.ui.components.BentoPlayerTransport as SharedBentoPlayerTransport
import io.github.zyrouge.symphony.ui.components.BentoSeekRow
import io.github.zyrouge.symphony.ui.components.PlayerMode
import io.github.zyrouge.symphony.ui.components.isShuffle
import io.github.zyrouge.symphony.ui.components.nextPlayerMode
import io.github.zyrouge.symphony.ui.components.resolvePlayerMode as resolveUiPlayerMode
import io.github.zyrouge.symphony.ui.components.toQueueLoopMode
import io.github.zyrouge.symphony.ui.contracts.UiLoopMode
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.model.UiPlaybackPosition
import io.github.zyrouge.symphony.utils.DurationUtils
import kotlinx.coroutines.delay

/** Maps the Android queue loop mode to the shared player mode. */
private fun resolvePlayerMode(loopMode: RadioQueue.LoopMode, shuffle: Boolean): PlayerMode =
    resolveUiPlayerMode(
        when (loopMode) {
            RadioQueue.LoopMode.None -> UiLoopMode.NONE
            RadioQueue.LoopMode.Queue -> UiLoopMode.QUEUE
            RadioQueue.LoopMode.Song -> UiLoopMode.SONG
        },
        shuffle,
    )

/** Maps the shared queue loop mode back to the Android queue's loop mode. */
private fun PlayerMode.toRadioLoopMode(): RadioQueue.LoopMode = when (toQueueLoopMode()) {
    UiLoopMode.NONE -> RadioQueue.LoopMode.None
    UiLoopMode.QUEUE -> RadioQueue.LoopMode.Queue
    UiLoopMode.SONG -> RadioQueue.LoopMode.Song
}


@Composable
internal fun BentoPlayerSeek(context: ViewContext, data: BentoPlayerData) {
    val position = data.playbackPosition
    BentoSeekRow(
        position = UiPlaybackPosition(position.played, position.total),
        playing = data.isPlaying,
        seekable = context.symphony.radio.hasPlayer,
        timeText = { DurationUtils.formatMs(it) },
        onSeek = { context.symphony.radio.seek(it) },
    )
}

@Composable
internal fun BentoPlayerTitle(context: ViewContext, data: BentoPlayerData) {
    val favoriteSongIds by context.symphony.groove.playlist.favorites.collectAsState()
    val isFavorite by remember(data.song.id, favoriteSongIds) {
        derivedStateOf { favoriteSongIds.contains(data.song.id) }
    }
    val additionalInfo by context.symphony.settings.nowPlayingAdditionalInfo.flow.collectAsState()
    val formatLine = if (additionalInfo) bentoPlayerFormatLine(context, data.song) else null

    SharedBentoPlayerTitle(
        song = data.song.toUiSong(),
        isFavorite = isFavorite,
        favoriteLabel = context.symphony.t.Favorite,
        unfavoriteLabel = context.symphony.t.Unfavorite,
        formatLine = formatLine,
        onToggleFavorite = {
            context.symphony.groove.playlist.run {
                if (isFavorite) unfavorite(data.song.id) else favorite(data.song.id)
            }
        },
    )
}

@Composable
internal fun BentoPlayerTransport(
    context: ViewContext,
    data: BentoPlayerData,
    short: Boolean,
    onShowSleepTimer: () -> Unit,
) {
    val playMode = resolvePlayerMode(data.currentLoopMode, data.currentShuffleMode)
    val seekControls by context.symphony.settings.nowPlayingSeekControls.flow.collectAsState()
    val seekBack by context.symphony.settings.seekBackDuration.flow.collectAsState()
    val seekForward by context.symphony.settings.seekForwardDuration.flow.collectAsState()
    val timerMinutes by produceState(
        initialValue = data.sleepTimer?.let {
            bentoSleepTimerMinutesLeft(it.endsAt, System.currentTimeMillis())
        },
        key1 = data.sleepTimer,
    ) {
        val timer = data.sleepTimer
        if (timer != null) {
            // Wake exactly when the shown minute changes.
            while (true) {
                val now = System.currentTimeMillis()
                value = bentoSleepTimerMinutesLeft(timer.endsAt, now)
                delay(bentoSleepTimerNextMinuteDelayMs(timer.endsAt, now) ?: break)
            }
        }
    }
    val t = context.symphony.t

    SharedBentoPlayerTransport(
        mode = playMode,
        isPlaying = data.isPlaying,
        short = short,
        labels = playerTransportLabels(t, playMode, seekBack, seekForward, timerMinutes),
        seekControls = seekControls,
        seekBackSeconds = seekBack,
        seekForwardSeconds = seekForward,
        sleepTimerBadge = timerMinutes?.let { t.XMinutesShort(it.toString()) },
        sleepTimerActive = data.sleepTimer != null,
        onCycleMode = {
            val nextMode = nextPlayerMode(playMode)
            context.symphony.radio.queue.run {
                if (nextMode.isShuffle() != currentShuffleMode) {
                    setShuffleMode(nextMode.isShuffle())
                }
                setLoopMode(nextMode.toRadioLoopMode())
            }
        },
        onPrevious = { context.symphony.radio.shorty.previous() },
        onSeekBack = { context.symphony.radio.shorty.seekFromCurrent(-seekBack) },
        onPlayPause = { context.symphony.radio.shorty.playPause() },
        onSeekForward = { context.symphony.radio.shorty.seekFromCurrent(seekForward) },
        onNext = { context.symphony.radio.shorty.skip() },
        onSleepTimerClick = {
            val next = nextBentoSleepTimerMinutes(data.sleepTimer?.duration)
            if (next == null) {
                context.symphony.radio.clearSleepTimer()
            } else {
                context.symphony.radio.setSleepTimer(
                    duration = next * 60_000L,
                    quitOnEnd = data.sleepTimer?.quitOnEnd ?: false,
                )
            }
        },
        onSleepTimerLongClick = onShowSleepTimer,
    )
}

/** Height the format line adds under the player title, in dp at the default font scale. */
internal const val PLAYER_FORMAT_LINE_HEIGHT_DP = 18f

/** The player's format line for [song], or null when nothing about its format is known. */
internal fun bentoPlayerFormatLine(context: ViewContext, song: Song) = bentoAudioFormatLine(
    path = song.path,
    samplingRateK = song.samplingRateK,
    bitrateK = song.bitrateK,
    kHz = { context.symphony.t.XKHz(it) },
    kbps = { context.symphony.t.XKbps(it) },
)

/**
 * "FLAC · 44.1 kHz · 920 kbps": the file extension, the sampling rate and the
 * bitrate, leaving out whatever is unknown. Null when all three are missing.
 */
internal fun bentoAudioFormatLine(
    path: String,
    samplingRateK: Float?,
    bitrateK: Long?,
    kHz: (String) -> String,
    kbps: (String) -> String,
): String? = listOfNotNull(
    path.substringAfterLast('/').substringAfterLast('.', "").uppercase().takeIf { it.isNotEmpty() },
    samplingRateK?.let { kHz(it.toString().removeSuffix(".0")) },
    bitrateK?.let { kbps(it.toString()) },
).joinToString(" · ").takeIf { it.isNotEmpty() }

/**
 * Factor (at most 1) that shrinks the transport buttons so the row fits in
 * [availableWidthDp]: play mode, previous, play, next and the sleep timer,
 * plus back and forward when [seekControls] is on.
 */

@Composable
internal fun BentoPlayerExtras(
    context: ViewContext,
    data: BentoPlayerData,
    stage: BentoPlayerStage,
    compact: Boolean,
    onStageChange: (BentoPlayerStage) -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onClosePlayer: () -> Unit,
) {
    val t = context.symphony.t
    SharedBentoPlayerExtras(
        lyricsLabel = t.Lyrics,
        queueLabel = t.Queue,
        moreLabel = t.More,
        lyricsSelected = stage == BentoPlayerStage.LYRICS,
        queueSelected = stage == BentoPlayerStage.QUEUE,
        onLyricsClick = {
            onStageChange(
                if (stage == BentoPlayerStage.LYRICS) BentoPlayerStage.COVER
                else BentoPlayerStage.LYRICS,
            )
        },
        onQueueClick = {
            onStageChange(
                if (stage == BentoPlayerStage.QUEUE) BentoPlayerStage.COVER
                else BentoPlayerStage.QUEUE,
            )
        },
        menu = { expanded, onDismissRequest ->
            BentoPlayerMoreMenu(
                context = context,
                data = data,
                compact = compact,
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                onShowSpeed = onShowSpeed,
                onShowPitch = onShowPitch,
                onShowSleepTimer = onShowSleepTimer,
                onClosePlayer = onClosePlayer,
            )
        },
    )
}
