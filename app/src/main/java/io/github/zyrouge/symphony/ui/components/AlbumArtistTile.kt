package io.github.zyrouge.symphony.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import io.github.zyrouge.symphony.services.groove.AlbumArtist
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.AlbumArtistViewRoute

@Composable
fun AlbumArtistTile(context: ViewContext, albumArtist: AlbumArtist) {
    SquareGrooveTile(
        image = albumArtist.createArtworkImageRequest(context.symphony).build(),
        options = { expanded, onDismissRequest ->
            AlbumArtistDropdownMenu(
                context,
                albumArtist,
                expanded = expanded,
                onDismissRequest = onDismissRequest,
            )
        },
        content = {
            Text(
                albumArtist.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onPlay = {
            context.symphony.radio.shorty.playQueue(
                albumArtist.getSortedSongIds(context.symphony),
                source = albumArtist.name,
            )
        },
        onClick = {
            context.navController.navigate(AlbumArtistViewRoute(albumArtist.name))
        }
    )
}

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
