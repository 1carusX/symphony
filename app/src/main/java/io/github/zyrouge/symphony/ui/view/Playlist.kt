package io.github.zyrouge.symphony.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import io.github.zyrouge.symphony.ui.components.BentoMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.components.PlaylistDropdownMenu
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.utils.mutate
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistViewRoute(val playlistId: String)

@Composable
fun PlaylistView(context: ViewContext, route: PlaylistViewRoute) {
    var updateCounter by remember { mutableStateOf(0) }
    val playlists by context.symphony.groove.playlist.all.collectAsState()
    val playlist by remember(playlists, updateCounter) {
        derivedStateOf { context.symphony.groove.playlist.get(route.playlistId) }
    }
    val songIds by remember(playlist) {
        derivedStateOf { playlist?.getSongIds(context.symphony) ?: emptyList() }
    }
    val isViable = playlist != null
    val isFavoritesPlaylist = remember(playlist) {
        playlist?.let { context.symphony.groove.playlist.isFavoritesPlaylist(it) } == true
    }
    var showOptionsMenu by remember { mutableStateOf(false) }

    fun incrementUpdateCounter() {
        updateCounter = if (updateCounter > 25) 0 else updateCounter + 1
    }

    BentoCollectionPage(
        context = context,
        title = playlist?.title ?: route.playlistId,
        subtitle = context.symphony.t.Playlist,
        songIds = songIds,
        onBack = { context.navController.popBackStack() },
        emptyMessage = if (isViable) {
            context.symphony.t.DamnThisIsSoEmpty
        } else {
            context.symphony.t.UnknownPlaylistX(route.playlistId)
        },
        trailingMenu = when {
            !isViable -> null
            else -> {
                {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(Icons.Filled.MoreVert, null)
                        PlaylistDropdownMenu(
                            context,
                            playlist!!,
                            expanded = showOptionsMenu,
                            onSongsChanged = { incrementUpdateCounter() },
                            onRename = { incrementUpdateCounter() },
                            onDelete = { context.navController.popBackStack() },
                            onDismissRequest = { showOptionsMenu = false },
                        )
                    }
                }
            }
        },
        rowMenu = when {
            !isViable || isFavoritesPlaylist -> null
            else -> {
                { song ->
                    var showRowMenu by remember(song.id) { mutableStateOf(false) }
                    androidx.compose.foundation.layout.Box {
                        IconButton(onClick = { showRowMenu = true }) {
                            Icon(Icons.Filled.MoreVert, null)
                        }
                        BentoMenu(
                            expanded = showRowMenu,
                            onDismissRequest = { showRowMenu = false },
                        ) {
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
                                    showRowMenu = false
                                    playlist?.let {
                                        context.symphony.groove.playlist.update(
                                            it.id,
                                            songIds.mutate { remove(song.id) },
                                        )
                                        incrementUpdateCounter()
                                    }
                                },
                            )
                        }
                    }
                }
            }
        },
    )
}
