package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun AlbumDropdownMenu(
    context: ViewContext,
    album: Album,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    BentoMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        header = {
            BentoMenuHeader(
                title = album.name,
                subtitle = listOfNotNull(
                    album.artists.joinToString().takeIf { it.isNotEmpty() },
                    (album.startYear ?: album.endYear)?.toString(),
                ).joinToString(" · "),
                art = {
                    BentoMenuArt(album.createArtworkImageRequest(context.symphony).build())
                },
            )
        },
    ) {
        BentoQueueMenuItems(
            context = context,
            orderedSongIds = { album.getSortedSongIds(context.symphony) },
            source = album.name,
            onDismissRequest = onDismissRequest,
            onAddToPlaylist = { showAddToPlaylistDialog = true },
        )
        if (album.artists.isNotEmpty()) {
            BentoMenuDivider()
            BentoGoToArtistItem(
                context = context,
                links = remember(album.artists) {
                    album.artists.map { BentoArtistLink(it, albumArtist = false) }
                },
                onDismissRequest = onDismissRequest,
            )
        }
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = album.getSongIds(context.symphony),
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}
