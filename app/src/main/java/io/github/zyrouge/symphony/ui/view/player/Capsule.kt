package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.BentoMiniPlayerCapsule
import io.github.zyrouge.symphony.ui.components.MiniPlayerActions
import io.github.zyrouge.symphony.ui.components.MiniPlayerLabels
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/** The floating mini player (DESIGN.md 4.2). */
@Composable
internal fun BentoPlayerCapsule(
    context: ViewContext,
    data: BentoPlayerData,
    short: Boolean,
    showPrevious: Boolean,
    onOpen: () -> Unit,
) {
    val settings = context.symphony.settings
    val trackControls by settings.miniPlayerTrackControls.flow.collectAsState()
    val seekControls by settings.miniPlayerSeekControls.flow.collectAsState()
    val textMarquee by settings.miniPlayerTextMarquee.flow.collectAsState()
    val seekBack by settings.seekBackDuration.flow.collectAsState()
    val seekForward by settings.seekForwardDuration.flow.collectAsState()
    val t = context.symphony.t

    BentoMiniPlayerCapsule(
        song = data.song.toUiSong(),
        isPlaying = data.isPlaying,
        progressRatio = data.playbackPosition.ratio,
        artwork = { modifier ->
            AsyncImage(
                data.song.createArtworkImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        },
        short = short,
        showPrevious = showPrevious,
        trackControls = trackControls,
        seekControls = seekControls,
        textMarquee = textMarquee,
        seekBackSeconds = seekBack,
        seekForwardSeconds = seekForward,
        labels = MiniPlayerLabels(
            play = t.Play,
            pause = t.Pause,
            previous = t.Previous,
            next = t.Next,
            seekBack = t.BackXSeconds(seekBack.toString()),
            seekForward = t.ForwardXSeconds(seekForward.toString()),
        ),
        actions = MiniPlayerActions(
            onOpen = onOpen,
            onPlayPause = { context.symphony.radio.shorty.playPause() },
            onPrevious = { context.symphony.radio.shorty.previous() },
            onNext = { context.symphony.radio.shorty.skip() },
            onSeekBack = { context.symphony.radio.shorty.seekFromCurrent(-seekBack) },
            onSeekForward = { context.symphony.radio.shorty.seekFromCurrent(seekForward) },
        ),
    )
}
