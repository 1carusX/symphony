package io.github.zyrouge.symphony.ui.view.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.MediaExposer
import io.github.zyrouge.symphony.services.groove.Playlist
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoGroupPlaylistCover
import io.github.zyrouge.symphony.ui.components.BentoGroupTile
import io.github.zyrouge.symphony.ui.components.BentoGroupedTileColumns
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.NewPlaylistDialog
import io.github.zyrouge.symphony.ui.components.bentoGroupTileHeightDp
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.PlaylistViewRoute
import io.github.zyrouge.symphony.utils.ActivityUtils
import io.github.zyrouge.symphony.utils.Logger

/**
 * The Playlists page (DESIGN.md 5.3): one grouped tile per playlist, in
 * stored order, with a 2 x 2 album-art mosaic cover and the playlist's songs
 * as rows showing the artist. The kit does not design the New/Import
 * playlist controls, so the existing control row stays above the tiles.
 */
@Composable
fun PlaylistsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.playlist.isUpdating.collectAsState()
    val playlistIds by context.symphony.groove.playlist.all.collectAsState()
    val playingSong = rememberBentoPlayingSong(context)
    var showPlaylistCreator by remember { mutableStateOf(false) }

    val openPlaylistLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        uris.forEach { x ->
            try {
                ActivityUtils.makePersistableReadableUri(context.symphony.applicationContext, x)
                val playlist = Playlist.parse(context.symphony, null, x)
                context.symphony.groove.playlist.add(playlist)
            } catch (err: Exception) {
                Logger.error("PlaylistView", "import failed (activity result)", err)
                Toast.makeText(
                    context.symphony.applicationContext,
                    context.symphony.t.InvalidM3UFile,
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
    }

    // Renames and edits replace playlists in place, so the version is a key too.
    val grooveVersion = LocalGrooveVersion.current
    val playlists by remember(playlistIds, grooveVersion) {
        derivedStateOf {
            playlistIds.mapNotNull { context.symphony.groove.playlist.get(it) }
        }
    }

    LoaderScaffold(context, isLoading = isUpdating) {
        when {
            playlists.isEmpty() -> IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.AutoMirrored.Rounded.QueueMusic,
                        null,
                        modifier = modifier,
                    )
                },
                content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
            )

            else -> Column(modifier = Modifier.fillMaxSize()) {
                PlaylistControlBar(
                    context,
                    showPlaylistCreator = {
                        showPlaylistCreator = true
                    },
                    showPlaylistPicker = {
                        openPlaylistLauncher.launch(arrayOf(MediaExposer.MIMETYPE_M3U))
                    },
                )
                BentoGroupedTileColumns(
                    entries = playlists,
                    entryHeightDp = { bentoGroupTileHeightDp(it.numberOfTracks) },
                    modifier = Modifier.weight(1f),
                ) { playlist, tileWidth ->
                    PlaylistGroupTile(
                        context = context,
                        playlist = playlist,
                        playingSongId = playingSong.songId,
                        playing = playingSong.isPlaying,
                        narrow = tileWidth < 340f,
                    )
                }
            }
        }
    }

    if (showPlaylistCreator) {
        NewPlaylistDialog(
            context,
            onDone = { playlist ->
                showPlaylistCreator = false
                context.symphony.groove.playlist.add(playlist)
            },
            onDismissRequest = {
                showPlaylistCreator = false
            }
        )
    }
}

@Composable
private fun PlaylistGroupTile(
    context: ViewContext,
    playlist: Playlist,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
) {
    // A playlist keeps its own order (custom by default), not the Songs tab's sort.
    val sortBy by context.symphony.settings.lastUsedPlaylistSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedPlaylistSongsSortReverse.flow.collectAsState()
    val grooveVersion = LocalGrooveVersion.current
    val songIds by remember(playlist, sortBy, sortReverse, grooveVersion) {
        derivedStateOf {
            context.symphony.groove.song.sort(
                playlist.getSongIds(context.symphony),
                sortBy,
                sortReverse,
            )
        }
    }
    val songs by remember(songIds) {
        derivedStateOf {
            songIds.mapNotNull { context.symphony.groove.song.get(it) }
        }
    }

    BentoGroupTile(
        context = context,
        title = playlist.title,
        subtitle = context.symphony.t.XSongs(playlist.numberOfTracks.toString()) +
            " · " +
            formatBentoDurationMinutes(songs.sumOf { it.duration }),
        songs = songs,
        cover = {
            BentoGroupPlaylistCover(
                context,
                playlist.getSongIds(context.symphony),
            )
        },
        onOpen = { context.navController.navigate(PlaylistViewRoute(playlist.id)) },
        onPlay = {
            context.symphony.radio.shorty.playQueue(songIds)
        },
        onPlayAt = { index ->
            context.symphony.radio.shorty.playQueue(
                songIds,
                Radio.PlayOptions(index = index),
            )
        },
        rowSecondaryOf = { it.artists.joinToString() },
        playingSongId = playingSongId,
        playing = playing,
        narrow = narrow,
        showAllLabel = "Show all " +
            context.symphony.t.XSongs(playlist.numberOfTracks.toString()),
        onShowAll = { context.navController.navigate(PlaylistViewRoute(playlist.id)) },
    )
}

@Composable
private fun PlaylistControlBar(
    context: ViewContext,
    showPlaylistCreator: () -> Unit,
    showPlaylistPicker: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
    ) {
        ActionPill(
            context = context,
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.Add,
            label = context.symphony.t.NewPlaylist,
            onClick = showPlaylistCreator,
        )
        ActionPill(
            context = context,
            modifier = Modifier.weight(1f),
            icon = Icons.Filled.ImportExport,
            label = context.symphony.t.ImportPlaylist,
            onClick = showPlaylistPicker,
        )
    }
}

@Composable
private fun ActionPill(
    context: ViewContext,
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(BentoRadii.TILE_SMALL.dp))
            .background(colors.raised)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = colors.accent, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = BentoTypography.rowTitle, color = colors.fg)
    }
}
