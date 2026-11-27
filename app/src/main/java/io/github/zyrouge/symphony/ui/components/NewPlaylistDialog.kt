package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/**
 * Names a new playlist. It starts with [initialSongIds]; the subtitle says
 * how many.
 */
@Composable
fun NewPlaylistDialog(
    context: ViewContext,
    initialSongIds: List<String> = listOf(),
    onDone: (Playlist) -> Unit,
    onDismissRequest: () -> Unit,
) {
    BentoNewPlaylistDialog(
        strings = context.symphony.t,
        initialSongCount = initialSongIds.size,
        onCreate = { title ->
            onDone(
                context.symphony.groove.playlist.create(
                    title = title,
                    songIds = initialSongIds,
                ),
            )
        },
        onDismissRequest = onDismissRequest,
    )
}
