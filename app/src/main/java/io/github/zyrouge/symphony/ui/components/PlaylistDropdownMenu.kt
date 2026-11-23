package io.github.zyrouge.symphony.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.MediaExposer
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.Logger

@Composable
fun PlaylistDropdownMenu(
    context: ViewContext,
    playlist: Playlist,
    expanded: Boolean,
    onSongsChanged: (() -> Unit) = {},
    onRename: (() -> Unit) = {},
    onDelete: (() -> Unit) = {},
    onDismissRequest: () -> Unit,
) {
    val savePlaylistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(MediaExposer.MIMETYPE_M3U)
    ) { uri ->
        uri?.let { _ ->
            try {
                context.symphony.groove.playlist.savePlaylistToUri(playlist, uri)
                Toast.makeText(
                    context.activity,
                    context.symphony.t.ExportedX(playlist.title),
                    Toast.LENGTH_SHORT,
                ).show()
            } catch (err: Exception) {
                Logger.error("PlaylistTile", "export failed (activity result)", err)
                Toast.makeText(
                    context.activity,
                    context.symphony.t.ExportFailedX(
                        err.localizedMessage ?: err.toString()
                    ),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
    }

    var showSongsPicker by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    BentoMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        header = {
            BentoMenuHeader(
                title = playlist.title,
                subtitle = context.symphony.t.Playlist + " · " +
                    context.symphony.t.XSongs(playlist.numberOfTracks.toString()),
                art = {
                    BentoMenuArt(playlist.createArtworkImageRequest(context.symphony).build())
                },
            )
        },
    ) {
        BentoQueueMenuItems(
            context = context,
            orderedSongIds = { playlist.getSortedSongIds(context.symphony) },
            queueSongIds = { playlist.getSongIds(context.symphony) },
            onDismissRequest = onDismissRequest,
            onAddToPlaylist = { showAddToPlaylistDialog = true },
        )
        BentoMenuDivider()
        if (playlist.isNotLocal) {
            BentoMenuItem(
                label = context.symphony.t.ManageSongs,
                icon = Icons.Outlined.Edit,
                onClick = {
                    onDismissRequest()
                    showSongsPicker = true
                }
            )
            BentoMenuItem(
                label = context.symphony.t.Rename,
                icon = Icons.Rounded.Title,
                onClick = {
                    onDismissRequest()
                    showRenameDialog = true
                }
            )
            BentoMenuItem(
                label = context.symphony.t.Export,
                icon = Icons.Rounded.Upload,
                onClick = {
                    onDismissRequest()
                    try {
                        savePlaylistLauncher.launch("${playlist.title}.m3u")
                    } catch (err: Exception) {
                        Logger.error("PlaylistTile", "export failed", err)
                        Toast.makeText(
                            context.activity,
                            context.symphony.t.ExportFailedX(
                                err.localizedMessage ?: err.toString()
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
        BentoMenuItem(
            label = context.symphony.t.Details,
            icon = Icons.Outlined.Info,
            onClick = {
                onDismissRequest()
                showInfoDialog = true
            }
        )
        if (!context.symphony.groove.playlist.isBuiltInPlaylist(playlist)) {
            BentoMenuDivider()
            BentoMenuItem(
                label = context.symphony.t.DeletePlaylist,
                icon = Icons.Outlined.Delete,
                destructive = true,
                onClick = {
                    onDismissRequest()
                    showDeleteDialog = true
                }
            )
        }
    }

    if (showInfoDialog) {
        PlaylistInformationDialog(
            context,
            playlist = playlist,
            onDismissRequest = {
                showInfoDialog = false
            }
        )
    }

    if (showSongsPicker) {
        PlaylistManageSongsDialog(
            context,
            selectedSongIds = playlist.getSongIds(context.symphony),
            onDone = {
                context.symphony.groove.playlist.update(playlist.id, it)
                onSongsChanged()
                showSongsPicker = false
            }
        )
    }

    if (showDeleteDialog) {
        ConfirmationDialog(
            context,
            title = {
                Text(context.symphony.t.DeletePlaylist)
            },
            description = {
                Text("“${playlist.title}” and its song order are removed. The songs stay in your library.")
            },
            confirmLabel = context.symphony.t.Delete,
            cancelLabel = context.symphony.t.Cancel,
            onResult = { result ->
                showDeleteDialog = false
                if (result) {
                    onDelete()
                    context.symphony.groove.playlist.delete(playlist.id)
                }
            }
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = playlist.getSongIds(context.symphony),
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }

    if (showRenameDialog) {
        RenamePlaylistDialog(
            context,
            playlist = playlist,
            onRename = onRename,
            onDismissRequest = {
                showRenameDialog = false
            }
        )
    }
}
