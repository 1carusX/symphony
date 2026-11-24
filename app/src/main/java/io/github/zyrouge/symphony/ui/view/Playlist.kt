package io.github.zyrouge.symphony.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.components.PlaylistDropdownMenu
import io.github.zyrouge.symphony.ui.components.PlaylistManageSongsDialog
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.ui.view.detail.DetailAlbumsTile
import io.github.zyrouge.symphony.ui.view.detail.DetailIconCoverTile
import io.github.zyrouge.symphony.ui.view.detail.DetailMosaicCoverTile
import io.github.zyrouge.symphony.ui.view.detail.detailAlbumsOf
import io.github.zyrouge.symphony.ui.view.detail.detailAlbumsRowSpan
import io.github.zyrouge.symphony.utils.mutate
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistViewRoute(val playlistId: String)

/**
 * The playlist detail page: a mosaic cover (an icon for Favorites and empty
 * playlists), the songs in the playlist's own sort order, an Edit songs tile
 * and "Remove from playlist" rows for playlists stored in the app, and the
 * albums in the playlist as a side tile.
 */
@Composable
fun PlaylistView(context: ViewContext, route: PlaylistViewRoute) {
    var updateCounter by remember { mutableStateOf(0) }
    val playlists by context.symphony.groove.playlist.all.collectAsState()
    val grooveVersion = LocalGrooveVersion.current
    val playlist by remember(playlists, updateCounter, grooveVersion) {
        derivedStateOf { context.symphony.groove.playlist.get(route.playlistId) }
    }
    val songIds by remember(playlist, grooveVersion) {
        derivedStateOf { playlist?.getSongIds(context.symphony) ?: emptyList() }
    }
    val albums = remember(songIds, grooveVersion) { detailAlbumsOf(context, songIds) }
    val isFavoritesPlaylist = remember(playlist) {
        playlist?.let { context.symphony.groove.playlist.isFavoritesPlaylist(it) } == true
    }
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showSongsPicker by remember { mutableStateOf(false) }

    fun incrementUpdateCounter() {
        updateCounter = if (updateCounter > 25) 0 else updateCounter + 1
    }

    val value = playlist
    if (value == null) {
        BentoCollectionUnknownPage(
            label = context.symphony.t.Playlist,
            message = context.symphony.t.UnknownPlaylistX(route.playlistId),
            icon = Icons.AutoMirrored.Filled.QueueMusic,
            onBack = { context.navController.popBackStack() },
        )
        return
    }
    // M3U playlists are re-read from their file on every scan, so edits here would be lost.
    val isEditable = value.isNotLocal

    BentoCollectionPage(
        context = context,
        label = context.symphony.t.Playlist,
        title = value.title,
        songIds = songIds,
        onBack = { context.navController.popBackStack() },
        sortByEntry = context.symphony.settings.lastUsedPlaylistSongsSortBy,
        sortReverseEntry = context.symphony.settings.lastUsedPlaylistSongsSortReverse,
        emptyMessage = context.symphony.t.DamnThisIsSoEmpty,
        cover = { geometry ->
            when {
                isFavoritesPlaylist -> DetailIconCoverTile(Icons.Filled.Favorite, geometry)
                songIds.isEmpty() -> DetailIconCoverTile(Icons.AutoMirrored.Filled.QueueMusic, geometry)
                else -> DetailMosaicCoverTile(context, songIds, geometry)
            }
        },
        trailingMenu = {
            IconButton(onClick = { showOptionsMenu = true }) {
                Icon(Icons.Filled.MoreVert, null)
                PlaylistDropdownMenu(
                    context,
                    value,
                    expanded = showOptionsMenu,
                    onSongsChanged = { incrementUpdateCounter() },
                    onRename = { incrementUpdateCounter() },
                    onDelete = { context.navController.popBackStack() },
                    onDismissRequest = { showOptionsMenu = false },
                )
            }
        },
        sideTiles = when {
            albums.isEmpty() -> emptyList()
            else -> listOf(
                DetailSideTile(
                    id = "albums",
                    rowSpan = { detailAlbumsRowSpan(it, albums.size) },
                ) { geometry ->
                    DetailAlbumsTile(context, context.symphony.t.Albums, albums, geometry)
                },
            )
        },
        onEditSongs = when {
            isEditable -> {
                { showSongsPicker = true }
            }

            else -> null
        },
        rowMenuExtra = when {
            !isEditable || isFavoritesPlaylist -> null
            else -> {
                { song, dismiss ->
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                Icons.Filled.DeleteForever,
                                null,
                                tint = ThemeColors.Red,
                            )
                        },
                        text = { Text(context.symphony.t.RemoveFromPlaylist) },
                        onClick = {
                            dismiss()
                            context.symphony.groove.playlist.update(
                                value.id,
                                songIds.mutate { remove(song.id) },
                            )
                            incrementUpdateCounter()
                        },
                    )
                }
            }
        },
    )

    if (showSongsPicker) {
        PlaylistManageSongsDialog(
            context,
            selectedSongIds = value.getSongIds(context.symphony),
            onDone = {
                context.symphony.groove.playlist.update(value.id, it)
                incrementUpdateCounter()
                showSongsPicker = false
            },
        )
    }
}
