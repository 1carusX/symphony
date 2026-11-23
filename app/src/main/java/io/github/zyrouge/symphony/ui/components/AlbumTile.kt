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
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute

@Composable
fun AlbumTile(context: ViewContext, album: Album) {
    SquareGrooveTile(
        image = album.createArtworkImageRequest(context.symphony).build(),
        options = { expanded, onDismissRequest ->
            AlbumDropdownMenu(
                context,
                album,
                expanded = expanded,
                onDismissRequest = onDismissRequest,
            )
        },
        content = {
            Text(
                album.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (album.artists.isNotEmpty()) {
                Text(
                    album.artists.joinToString(),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        onPlay = {
            context.symphony.radio.shorty.playQueue(album.getSortedSongIds(context.symphony))
        },
        onClick = {
            context.navController.navigate(AlbumViewRoute(album.id))
        }
    )
}

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
