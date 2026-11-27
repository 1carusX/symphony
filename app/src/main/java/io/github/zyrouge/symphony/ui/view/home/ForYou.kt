package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.ui.adapters.toUiAlbum
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.ForYouMix
import io.github.zyrouge.symphony.ui.components.ForYouPage
import io.github.zyrouge.symphony.ui.components.ForYouPageData
import io.github.zyrouge.symphony.ui.components.ForYouUpNextEntry
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.libraryRescanAction
import io.github.zyrouge.symphony.ui.helpers.rememberGrooveFolderCount
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import io.github.zyrouge.symphony.ui.view.HomePage
import io.github.zyrouge.symphony.ui.view.LocalHomeContentAtEnd
import io.github.zyrouge.symphony.ui.view.LocalHomeContentAtTop
import io.github.zyrouge.symphony.ui.view.PlaylistViewRoute
import io.github.zyrouge.symphony.ui.view.SettingsViewRoute
import io.github.zyrouge.symphony.utils.DurationUtils
import io.github.zyrouge.symphony.utils.randomSubList
import io.github.zyrouge.symphony.utils.runIfOrDefault

@Composable
fun ForYouView(
    context: ViewContext,
    onOpenPlayer: () -> Unit,
    onNowPlayingTileVisibilityChanged: (Boolean) -> Unit,
) {
    val songsIsUpdating by context.symphony.groove.song.isUpdating.collectAsState()
    val albumsIsUpdating by context.symphony.groove.album.isUpdating.collectAsState()
    val artistsIsUpdating by context.symphony.groove.artist.isUpdating.collectAsState()
    val songIds by context.symphony.groove.song.all.collectAsState()
    val albumIds by context.symphony.groove.album.all.collectAsState()
    val artistNames by context.symphony.groove.artist.all.collectAsState()
    val genreNames by context.symphony.groove.genre.all.collectAsState()
    val playlistIds by context.symphony.groove.playlist.all.collectAsState()
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val folderCount = rememberGrooveFolderCount(context)

    when {
        songIds.isEmpty() && songsIsUpdating -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        songIds.isEmpty() -> IconTextBody(
            icon = { modifier ->
                Icon(
                    Icons.Filled.MusicNote,
                    null,
                    modifier = modifier,
                )
            },
            content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
        )

        else -> {
            val t = context.symphony.t
            val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
            val recentlyAddedSongs by remember(songsIsUpdating, songIds) {
                derivedStateOf {
                    runIfOrDefault(!songsIsUpdating, listOf()) {
                        context.symphony.groove.song.sort(
                            songIds.toList(),
                            SongRepository.SortBy.DATE_MODIFIED,
                            true,
                        )
                    }
                }
            }
            val recentlyAddedAlbums by remember(recentlyAddedSongs) {
                derivedStateOf {
                    recentlyAddedSongs
                        .mapNotNull { songId ->
                            context.symphony.groove.song.get(songId)
                                ?.let { context.symphony.groove.album.getIdFromSong(it) }
                        }
                        .distinct()
                        .take(9)
                        .mapNotNull { context.symphony.groove.album.get(it) }
                }
            }
            val suggestedAlbums by remember(albumsIsUpdating, albumIds) {
                derivedStateOf {
                    runIfOrDefault(!albumsIsUpdating, listOf()) {
                        albumIds.randomSubList(6)
                            .mapNotNull { context.symphony.groove.album.get(it) }
                    }
                }
            }
            val suggestedArtists by remember(artistsIsUpdating, artistNames) {
                derivedStateOf {
                    runIfOrDefault(!artistsIsUpdating, listOf()) {
                        artistNames.randomSubList(6)
                    }
                }
            }
            val totalDurationMs by remember(songIds) {
                derivedStateOf {
                    songIds.sumOf { context.symphony.groove.song.get(it)?.duration ?: 0L }
                }
            }
            val mixPlaylist by remember(playlistIds) {
                derivedStateOf {
                    playlistIds.firstOrNull()?.let { context.symphony.groove.playlist.get(it) }
                }
            }
            val upNext by remember(queue, queueIndex) {
                derivedStateOf {
                    queue.drop(queueIndex + 1).mapIndexed { index, songId ->
                        queueIndex + 1 + index to songId
                    }
                }
            }
            val upNextEntries = upNext.mapNotNull { (position, songId) ->
                context.symphony.groove.song.get(songId)?.let { song ->
                    ForYouUpNextEntry(
                        position = position,
                        song = song.toUiSong(),
                        durationText = DurationUtils.formatMs(song.duration),
                    )
                }
            }

            val contentAtEnd = LocalHomeContentAtEnd.current
            val contentAtTop = LocalHomeContentAtTop.current
            ForYouPage(
                data = ForYouPageData(
                    songCount = songIds.size,
                    albumCount = albumIds.size,
                    genreNames = genreNames,
                    playlistCount = playlistIds.size,
                    folderCount = folderCount,
                    totalDurationMs = totalDurationMs,
                    recentlyAddedAlbums = recentlyAddedAlbums.map { it.toUiAlbum() },
                    suggestedAlbums = suggestedAlbums.map { it.toUiAlbum() },
                    suggestedArtists = suggestedArtists,
                    upNext = upNextEntries,
                    mix = mixPlaylist?.let { playlist ->
                        ForYouMix(
                            title = playlist.title,
                            sub = t.Playlist + " · " + t.XSongs(playlist.numberOfTracks.toString()),
                        )
                    },
                    songsEnabled = !songsIsUpdating,
                ),
                strings = t,
                appFontFamily = appFontFamily,
                nowPlaying = { geometry ->
                    ForYouNowPlayingTile(
                        context = context,
                        geometry = geometry,
                        onOpenPlayer = onOpenPlayer,
                        onNowPlayingTileVisibilityChanged = onNowPlayingTileVisibilityChanged,
                    )
                },
                artworkAlbum = { album, modifier ->
                    AsyncImage(
                        context.symphony.groove.album.createArtworkImageRequest(album.id).build(),
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = modifier,
                    )
                },
                artworkArtist = { artistName, modifier ->
                    AsyncImage(
                        context.symphony.groove.artist.createArtworkImageRequest(artistName).build(),
                        artistName,
                        contentScale = ContentScale.Crop,
                        modifier = modifier,
                    )
                },
                artworkSong = { song, modifier ->
                    AsyncImage(
                        context.symphony.groove.song.createArtworkImageRequest(song.id).build(),
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = modifier,
                    )
                },
                onShuffle = {
                    context.symphony.radio.shorty.playQueue(
                        songIds.toList(),
                        shuffle = true,
                        source = t.AllSongs,
                    )
                },
                onRescan = libraryRescanAction(context),
                onOpenGenres = { selectHomeSection(context, HomePage.Genres) },
                onOpenPlaylists = { selectHomeSection(context, HomePage.Playlists) },
                onOpenFolders = { selectHomeSection(context, HomePage.Folders) },
                onOpenAlbums = { selectHomeSection(context, HomePage.Albums) },
                onOpenArtists = { selectHomeSection(context, HomePage.Artists) },
                onOpenSongs = { selectHomeSection(context, HomePage.Songs) },
                onOpenSettings = { context.navController.navigate(SettingsViewRoute()) },
                onOpenAlbum = { album -> context.navController.navigate(AlbumViewRoute(album.id)) },
                onOpenArtist = { artistName ->
                    context.navController.navigate(ArtistViewRoute(artistName))
                },
                onOpenMix = {
                    mixPlaylist?.let { context.navController.navigate(PlaylistViewRoute(it.id)) }
                },
                onJumpQueue = { position -> context.symphony.radio.jumpTo(position) },
                onContentAtEndChanged = { contentAtEnd.value = it },
                onContentAtTopChanged = { contentAtTop.value = it },
            )
        }
    }
}

/**
 * Switches the home page to [section]; a shortcut to a section the user has
 * removed from their tabs enables it first so the navigation lands somewhere
 * visible.
 */
internal fun selectHomeSection(context: ViewContext, section: HomePage) {
    val tabs = context.symphony.settings.homeTabs.value
    if (section !in tabs) {
        context.symphony.settings.homeTabs.setValue(tabs + section)
    }
    context.symphony.settings.lastHomeTab.setValue(section)
}
