package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.DurationUtils

@Composable
fun BentoSongRow(
    context: ViewContext,
    song: Song,
    playing: Boolean,
    isPlaying: Boolean,
    columns: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    menu: (@Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null,
    onClick: () -> Unit,
) {
    SongRow(
        song = song.toUiSong(),
        playing = playing,
        isPlaying = isPlaying,
        durationText = DurationUtils.formatMs(song.duration),
        moreContentDescription = context.symphony.t.More,
        artwork = { modifier ->
            AsyncImage(
                song.createArtworkImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        },
        columns = columns,
        trailing = trailing,
        onLongClick = onLongClick,
        menu = menu,
        onClick = onClick,
    )
}
