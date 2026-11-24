package io.github.zyrouge.symphony.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.helpers.ViewContext

/**
 * Adds [songIds] to playlists: a "New playlist" row, then one checkbox per
 * in-app playlist, ticked when it already holds every song. The ticks are
 * applied on Done; a playlist never gets a song twice.
 */
@Composable
fun AddToPlaylistDialog(
    context: ViewContext,
    songIds: List<String>,
    onDismissRequest: () -> Unit,
) {
    val playlistRepository = context.symphony.groove.playlist
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    val allPlaylistsIds by playlistRepository.all.collectAsState()
    val playlistsUpdateId by playlistRepository.updateId.collectAsState()
    val addedSongIds = remember(songIds) { songIds.distinct() }
    val playlists = remember(allPlaylistsIds, playlistsUpdateId) {
        allPlaylistsIds
            .mapNotNull { playlistRepository.get(it) }
            .filter { it.isNotLocal }
    }
    // Whether each playlist holds every song right now.
    val holdsAll = remember(playlists, addedSongIds) {
        playlists.associate { playlist ->
            playlist.id to (addedSongIds.isNotEmpty() &&
                playlist.getSongIds(context.symphony).toHashSet().containsAll(addedSongIds))
        }
    }
    // The ticks the user changed, by playlist id.
    val picked = remember { mutableStateMapOf<String, Boolean>() }

    fun applyPicked() {
        playlists.forEach { playlist ->
            val add = picked[playlist.id] ?: return@forEach
            if (add == holdsAll[playlist.id]) return@forEach
            playlistRepository.update(
                playlist.id,
                togglePlaylistSongs(playlist.getSongIds(context.symphony), addedSongIds, add),
            )
        }
    }

    if (!showNewPlaylistDialog) {
        val songs = addedSongIds.mapNotNull { context.symphony.groove.song.get(it) }
        BentoSheetDialog(
            title = context.symphony.t.AddToPlaylist,
            subtitle = when {
                songs.size == 1 -> listOf(songs[0].title)
                    .plus(songs[0].artists)
                    .joinToString(" · ")

                else -> context.symphony.t.XSongs(addedSongIds.size.toString())
            },
            onDismissRequest = onDismissRequest,
            actions = {
                BentoSheetButton(
                    label = context.symphony.t.Done,
                    primary = true,
                    onClick = {
                        applyPicked()
                        onDismissRequest()
                    },
                )
            },
        ) {
            BentoSheetActionRow(
                title = context.symphony.t.NewPlaylist,
                leading = { BentoSheetIconWell(Icons.Rounded.Add) },
                onClick = { showNewPlaylistDialog = true },
            )
            playlists.forEach { playlist ->
                BentoSheetCheckRow(
                    title = playlist.title,
                    subtitle = context.symphony.t.XSongs(playlist.numberOfTracks.toString()),
                    checked = picked[playlist.id] ?: holdsAll[playlist.id] ?: false,
                    onCheckedChange = { picked[playlist.id] = it },
                    leading = {
                        BentoSheetArt(
                            playlist.createArtworkImageRequest(context.symphony).build(),
                        )
                    },
                )
            }
        }
    }

    if (showNewPlaylistDialog) {
        NewPlaylistDialog(
            context = context,
            initialSongIds = addedSongIds,
            onDone = { playlist ->
                showNewPlaylistDialog = false
                playlistRepository.add(playlist)
            },
            onDismissRequest = {
                showNewPlaylistDialog = false
            }
        )
    }
}

/**
 * [current] with [songIds] appended (skipping songs it already holds) when
 * [add] is true, or with every occurrence of them removed otherwise.
 */
internal fun togglePlaylistSongs(
    current: List<String>,
    songIds: List<String>,
    add: Boolean,
): List<String> = when {
    add -> {
        val held = current.toHashSet()
        current + songIds.filter { held.add(it) }
    }

    else -> {
        val removed = songIds.toHashSet()
        current.filter { it !in removed }
    }
}
