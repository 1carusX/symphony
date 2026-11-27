package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.AlbumArtist
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumArtistDropdownMenu(
    context: ViewContext,
    albumArtist: AlbumArtist,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    BentoMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        header = {
            BentoMenuHeader(
                title = albumArtist.name,
                subtitle = context.symphony.t.AlbumArtist + " · " +
                    context.symphony.t.XSongs(albumArtist.numberOfTracks.toString()),
                art = {
                    BentoMenuArt(
                        albumArtist.createArtworkImageRequest(context.symphony).build(),
                        round = true,
                    )
                },
            )
        },
    ) {
        BentoQueueMenuItems(
            context = context,
            orderedSongIds = { albumArtist.getSortedSongIds(context.symphony) },
            source = albumArtist.name,
            onDismissRequest = onDismissRequest,
            onAddToPlaylist = { showAddToPlaylistDialog = true },
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = albumArtist.getSongIds(context.symphony),
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}
