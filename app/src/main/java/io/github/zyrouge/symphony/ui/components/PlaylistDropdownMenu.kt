package io.github.zyrouge.symphony.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.services.groove.MediaExposer
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.ThemeColors
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
        onDismissRequest = onDismissRequest
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.ShufflePlay)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.shorty.playQueue(
                    playlist.getSortedSongIds(context.symphony),
                    shuffle = true,
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.PlayNext)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(
                    playlist.getSortedSongIds(context.symphony),
                    context.symphony.radio.queue.currentSongIndex + 1
                )
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistPlay, null)
            },
            text = {
                Text(context.symphony.t.AddToQueue)
            },
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(playlist.getSongIds(context.symphony))
            }
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null)
            },
            text = {
                Text(context.symphony.t.AddToPlaylist)
            },
            onClick = {
                onDismissRequest()
                showAddToPlaylistDialog = true
            }
        )
        if (playlist.isNotLocal) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null)
                },
                text = {
                    Text(context.symphony.t.ManageSongs)
                },
                onClick = {
                    onDismissRequest()
                    showSongsPicker = true
                }
            )
        }
        DropdownMenuItem(
            leadingIcon = {
                Icon(Icons.Filled.Info, null)
            },
            text = {
                Text(context.symphony.t.Details)
            },
            onClick = {
                onDismissRequest()
                showInfoDialog = true
            }
        )
        if (playlist.isNotLocal) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Save, null)
                },
                text = {
                    Text(context.symphony.t.Export)
                },
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
            DropdownMenuItem(
                leadingIcon = {
                    Icon(Icons.Filled.Edit, null)
                },
                text = {
                    Text(context.symphony.t.Rename)
                },
                onClick = {
                    onDismissRequest()
                    showRenameDialog = true
                }
            )
        }
        if (!context.symphony.groove.playlist.isBuiltInPlaylist(playlist)) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        Icons.Filled.DeleteForever,
                        null,
                        tint = ThemeColors.Red,
                    )
                },
                text = {
                    Text(context.symphony.t.Delete)
                },
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
                Text(context.symphony.t.AreYouSureThatYouWantToDeleteThisPlaylist)
            },
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
