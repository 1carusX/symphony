package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.Artist
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/**
 * The artist menu (prototype MENUS.grp): the artist's avatar, name and song
 * count, then Shuffle play, Play next, Add to queue and Add to playlist.
 */
@Composable
fun ArtistDropdownMenu(
    context: ViewContext,
    artist: Artist,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    BentoMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        header = {
            BentoMenuHeader(
                title = artist.name,
                subtitle = context.symphony.t.Artist + " · " +
                    context.symphony.t.XSongs(artist.numberOfTracks.toString()),
                art = {
                    BentoMenuArt(
                        artist.createArtworkImageRequest(context.symphony).build(),
                        round = true,
                    )
                },
            )
        },
    ) {
        BentoQueueMenuItems(
            context = context,
            orderedSongIds = { artist.getSortedSongIds(context.symphony) },
            onDismissRequest = onDismissRequest,
            onAddToPlaylist = { showAddToPlaylistDialog = true },
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = artist.getSongIds(context.symphony),
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}
