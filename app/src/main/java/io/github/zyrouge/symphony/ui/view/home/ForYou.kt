package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.RadioPlayer
import io.github.zyrouge.symphony.ui.components.BentoCallToActionPill
import io.github.zyrouge.symphony.ui.components.BentoCoverGrid
import io.github.zyrouge.symphony.ui.components.BentoEqualizerGlyph
import io.github.zyrouge.symphony.ui.components.BentoGrid
import io.github.zyrouge.symphony.ui.components.BentoSeekWave
import io.github.zyrouge.symphony.ui.components.BentoShortcutTile
import io.github.zyrouge.symphony.ui.components.BentoStatContent
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileHeader
import io.github.zyrouge.symphony.ui.components.BentoTilePlacement
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.BentoTileSizeClass
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.bentoBottomInset
import io.github.zyrouge.symphony.ui.components.bentoStatValueStyle
import io.github.zyrouge.symphony.ui.components.bentoTilePaddingDp
import io.github.zyrouge.symphony.ui.components.fitBentoCoverGrid
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.rememberGrooveFolderCount
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoGridClass
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
import java.util.Locale
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * For you placement tables (DESIGN.md, Appendix A, "forYou"): each entry is
 * [tileId, column, row, columnSpan, rowSpan], counted from 1.
 */
private fun forYouPlacements(gridClass: BentoGridClass) = when (gridClass) {
    BentoGridClass.C4 -> listOf(
        BentoTilePlacement("np", 1, 1, 4, 2),
        BentoTilePlacement("shuffle", 1, 3, 2, 1),
        BentoTilePlacement("stat", 3, 3, 2, 1),
        BentoTilePlacement("recent", 1, 4, 4, 2),
        BentoTilePlacement("suggested", 1, 6, 2, 2),
        BentoTilePlacement("genres", 3, 6, 2, 1),
        BentoTilePlacement("playlists", 3, 7, 1, 1),
        BentoTilePlacement("folders", 4, 7, 1, 1),
        BentoTilePlacement("artists", 1, 8, 4, 1),
        BentoTilePlacement("upnext", 1, 9, 4, 3),
        BentoTilePlacement("albums", 1, 12, 2, 1),
        BentoTilePlacement("songs", 3, 12, 2, 1),
    )

    BentoGridClass.SHORT -> listOf(
        BentoTilePlacement("np", 1, 1, 4, 3),
        BentoTilePlacement("shuffle", 5, 1, 2, 1),
        BentoTilePlacement("stat", 5, 2, 2, 1),
        BentoTilePlacement("genres", 5, 3, 2, 1),
        BentoTilePlacement("recent", 7, 1, 2, 3),
        BentoTilePlacement("suggested", 1, 4, 3, 2),
        BentoTilePlacement("upnext", 4, 4, 3, 2),
        BentoTilePlacement("playlists", 7, 4, 1, 1),
        BentoTilePlacement("folders", 8, 4, 1, 1),
        BentoTilePlacement("artists", 7, 5, 2, 1),
        BentoTilePlacement("albums", 1, 6, 3, 1),
        BentoTilePlacement("songs", 4, 6, 3, 1),
        BentoTilePlacement("mix", 7, 6, 2, 1),
    )

    BentoGridClass.C6 -> listOf(
        BentoTilePlacement("np", 1, 1, 4, 2),
        BentoTilePlacement("shuffle", 5, 1, 2, 1),
        BentoTilePlacement("stat", 5, 2, 2, 1),
        BentoTilePlacement("recent", 1, 3, 6, 2),
        BentoTilePlacement("suggested", 1, 5, 3, 2),
        BentoTilePlacement("genres", 4, 5, 3, 1),
        BentoTilePlacement("playlists", 4, 6, 1, 1),
        BentoTilePlacement("folders", 5, 6, 1, 1),
        BentoTilePlacement("albums", 6, 6, 1, 1),
        BentoTilePlacement("upnext", 1, 7, 3, 2),
        BentoTilePlacement("artists", 4, 7, 3, 1),
        BentoTilePlacement("mix", 4, 8, 2, 1),
        BentoTilePlacement("songs", 6, 8, 1, 1),
    )

    BentoGridClass.C8 -> listOf(
        BentoTilePlacement("np", 1, 1, 4, 2),
        BentoTilePlacement("recent", 5, 1, 4, 2),
        BentoTilePlacement("suggested", 1, 3, 2, 2),
        BentoTilePlacement("shuffle", 3, 3, 1, 1),
        BentoTilePlacement("stat", 4, 3, 1, 1),
        BentoTilePlacement("genres", 3, 4, 2, 1),
        BentoTilePlacement("upnext", 5, 3, 2, 2),
        BentoTilePlacement("playlists", 7, 3, 1, 1),
        BentoTilePlacement("folders", 8, 3, 1, 1),
        BentoTilePlacement("artists", 7, 4, 2, 1),
        BentoTilePlacement("mix", 1, 5, 2, 1),
        BentoTilePlacement("albums", 3, 5, 2, 1),
        BentoTilePlacement("songs", 5, 5, 2, 1),
        BentoTilePlacement("settings", 7, 5, 2, 1),
    )

    BentoGridClass.C12 -> listOf(
        BentoTilePlacement("np", 1, 1, 5, 3),
        BentoTilePlacement("recent", 6, 1, 7, 2),
        BentoTilePlacement("shuffle", 6, 3, 2, 1),
        BentoTilePlacement("stat", 8, 3, 2, 1),
        BentoTilePlacement("genres", 10, 3, 3, 1),
        BentoTilePlacement("suggested", 1, 4, 3, 2),
        BentoTilePlacement("upnext", 4, 4, 4, 3),
        BentoTilePlacement("playlists", 8, 4, 2, 1),
        BentoTilePlacement("folders", 10, 4, 3, 1),
        BentoTilePlacement("artists", 8, 5, 5, 1),
        BentoTilePlacement("mix", 1, 6, 3, 1),
        BentoTilePlacement("albums", 8, 6, 2, 1),
        BentoTilePlacement("songs", 10, 6, 3, 1),
    )
}

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

        else -> BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
            val gridClass = rememberBentoGridClass(maxWidth.value.roundToInt())

            // Recently added songs (the same sort the previous For you page
            // used), reduced to their albums for the collection tile.
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

            val scrollState = rememberScrollState()
            val contentAtEnd = LocalHomeContentAtEnd.current
            androidx.compose.runtime.LaunchedEffect(scrollState) {
                androidx.compose.runtime.snapshotFlow {
                    scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 4
                }.collect { contentAtEnd.value = it }
            }
            val contentAtTop = LocalHomeContentAtTop.current
            androidx.compose.runtime.LaunchedEffect(scrollState) {
                androidx.compose.runtime.snapshotFlow { scrollState.value <= 4 }
                    .collect { contentAtTop.value = it }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                BentoGrid(
                    gridClass = gridClass,
                    placements = forYouPlacements(gridClass),
                    modifier = Modifier.fillMaxWidth(),
                ) { tileId, _, geometry ->
                    when (tileId) {
                        "np" -> ForYouNowPlayingTile(
                            context = context,
                            geometry = geometry,
                            onOpenPlayer = onOpenPlayer,
                            onNowPlayingTileVisibilityChanged = onNowPlayingTileVisibilityChanged,
                        )

                        "shuffle" -> BentoShortcutTile(
                            geometry = geometry,
                            tone = BentoTileTone.ACCENT,
                            icon = Icons.Rounded.Shuffle,
                            label = context.symphony.t.ShufflePlay,
                            sub = context.symphony.t.XSongs(songIds.size.toString()),
                            enabled = !songsIsUpdating,
                            onClick = {
                                context.symphony.radio.shorty.playQueue(
                                    songIds.toList(),
                                    shuffle = true,
                                    source = context.symphony.t.AllSongs,
                                )
                            },
                        )

                        "stat" -> ForYouLibraryStatTile(
                            context = context,
                            geometry = geometry,
                            appFontFamily = appFontFamily,
                            totalDurationMs = totalDurationMs,
                            songCount = songIds.size,
                            albumCount = albumIds.size,
                        )

                        "recent" -> ForYouAlbumCollectionTile(
                            context = context,
                            geometry = geometry,
                            label = context.symphony.t.RecentlyAddedSongs,
                            albums = recentlyAddedAlbums,
                        )

                        "suggested" -> ForYouAlbumCollectionTile(
                            context = context,
                            geometry = geometry,
                            label = context.symphony.t.SuggestedAlbums,
                            albums = suggestedAlbums,
                        )

                        "genres" -> BentoShortcutTile(
                            geometry = geometry,
                            icon = Icons.Rounded.Sell,
                            label = context.symphony.t.Genres,
                            sub = context.symphony.t.XGenres(genreNames.size.toString()),
                            chips = genreNames,
                            onClick = { selectHomeSection(context, HomePage.Genres) },
                        )

                        "playlists" -> BentoShortcutTile(
                            geometry = geometry,
                            icon = Icons.AutoMirrored.Rounded.QueueMusic,
                            label = context.symphony.t.Playlists,
                            sub = context.symphony.t.XPlaylists(playlistIds.size.toString()),
                            onClick = { selectHomeSection(context, HomePage.Playlists) },
                        )

                        "folders" -> BentoShortcutTile(
                            geometry = geometry,
                            icon = Icons.Rounded.Folder,
                            label = context.symphony.t.Folders,
                            sub = context.symphony.t.XFolders(folderCount.toString()),
                            onClick = { selectHomeSection(context, HomePage.Folders) },
                        )

                        "albums" -> BentoShortcutTile(
                            geometry = geometry,
                            icon = Icons.Rounded.Album,
                            label = context.symphony.t.Albums,
                            sub = context.symphony.t.XAlbums(albumIds.size.toString()),
                            onClick = { selectHomeSection(context, HomePage.Albums) },
                        )

                        "songs" -> BentoShortcutTile(
                            geometry = geometry,
                            icon = Icons.Rounded.MusicNote,
                            label = context.symphony.t.Songs,
                            sub = context.symphony.t.XSongs(songIds.size.toString()),
                            onClick = { selectHomeSection(context, HomePage.Songs) },
                        )

                        "settings" -> BentoShortcutTile(
                            geometry = geometry,
                            icon = Icons.Rounded.Tune,
                            label = context.symphony.t.Settings,
                            sub = context.symphony.t.ThemeAndColors,
                            onClick = { context.navController.navigate(SettingsViewRoute()) },
                        )

                        "mix" -> mixPlaylist?.let { playlist ->
                            BentoShortcutTile(
                                geometry = geometry,
                                tone = BentoTileTone.TONAL,
                                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                                label = playlist.title,
                                sub = context.symphony.t.Playlist + " · " +
                                    context.symphony.t.XSongs(playlist.numberOfTracks.toString()),
                                onClick = {
                                    context.navController.navigate(PlaylistViewRoute(playlist.id))
                                },
                            )
                        }

                        "artists" -> ForYouArtistsTile(
                            context = context,
                            geometry = geometry,
                            artistNames = suggestedArtists,
                        )

                        "upnext" -> ForYouUpNextTile(
                            context = context,
                            geometry = geometry,
                        )
                    }
                }
                Spacer(Modifier.height(bentoBottomInset(16.dp)))
            }
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
