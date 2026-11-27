package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.ForYouNowPlayingLabels
import io.github.zyrouge.symphony.ui.components.ForYouNowPlayingTile as BentoForYouNowPlayingTile
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.model.UiPlaybackPosition
import io.github.zyrouge.symphony.utils.DurationUtils

@Composable
internal fun ForYouNowPlayingTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    onOpenPlayer: () -> Unit,
    onNowPlayingTileVisibilityChanged: (Boolean) -> Unit,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    val playbackPosition by context.symphony.radio.observatory.playbackPosition.collectAsState()
    val song by remember(queue, queueIndex) {
        derivedStateOf {
            queue.getOrNull(queueIndex)?.let { context.symphony.groove.song.get(it) }
        }
    }
    val t = context.symphony.t

    BentoForYouNowPlayingTile(
        geometry = geometry,
        song = song?.toUiSong(),
        isPlaying = isPlaying,
        position = UiPlaybackPosition(playbackPosition.played, playbackPosition.total),
        seekable = context.symphony.radio.hasPlayer,
        labels = ForYouNowPlayingLabels(
            nowPlaying = t.NowPlaying,
            nothingPlaying = t.NothingIsBeingPlayedRightNow,
            previous = t.Previous,
            play = t.Play,
            pause = t.Pause,
            next = t.Next,
        ),
        artwork = { modifier ->
            AsyncImage(
                song?.createArtworkImageRequest(context.symphony)?.build()
                    ?: Assets.createPlaceholderImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        },
        timeText = { DurationUtils.formatMs(it) },
        onOpenPlayer = onOpenPlayer,
        onSeek = { context.symphony.radio.seek(it) },
        onPrevious = { context.symphony.radio.shorty.previous() },
        onPlayPause = { context.symphony.radio.shorty.playPause() },
        onNext = { context.symphony.radio.shorty.skip() },
        onVisibilityChanged = onNowPlayingTileVisibilityChanged,
    )
}
