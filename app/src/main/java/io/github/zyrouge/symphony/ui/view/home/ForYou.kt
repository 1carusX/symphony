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
            val contentAtEnd = io.github.zyrouge.symphony.ui.view.LocalHomeContentAtEnd.current
            androidx.compose.runtime.LaunchedEffect(scrollState) {
                androidx.compose.runtime.snapshotFlow {
                    scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 4
                }.collect { contentAtEnd.value = it }
            }
            val contentAtTop = io.github.zyrouge.symphony.ui.view.LocalHomeContentAtTop.current
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
                            sub = "Theme and colors",
                            onClick = { context.navController.navigate(SettingsViewRoute()) },
                        )

                        "mix" -> mixPlaylist?.let { playlist ->
                            BentoShortcutTile(
                                geometry = geometry,
                                tone = BentoTileTone.TONAL,
                                icon = Icons.AutoMirrored.Rounded.QueueMusic,
                                label = "Late night",
                                sub = "Playlist · " +
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
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Switches the home page to [section]; a shortcut to a section the user has
 * removed from their tabs enables it first so the navigation lands somewhere
 * visible.
 */
private fun selectHomeSection(context: ViewContext, section: HomePage) {
    val tabs = context.symphony.settings.homeTabs.value
    if (section !in tabs) {
        context.symphony.settings.homeTabs.setValue(tabs + section)
    }
    context.symphony.settings.lastHomeTab.setValue(section)
}

/**
 * The Now playing tile (DESIGN.md 5.1): square art, an eyebrow with an
 * equalizer glyph, title, artist, seek bar and prev/play/next; on xl or tall
 * tiles the art fills the tile under a dark gradient with white text and the
 * eyebrow becomes a dark pill. Tapping the tile opens the player.
 */
@Composable
private fun ForYouNowPlayingTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    onOpenPlayer: () -> Unit,
    onNowPlayingTileVisibilityChanged: (Boolean) -> Unit,
) {
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    val playbackPosition by context.symphony.radio.observatory.playbackPosition.collectAsState()
    val song by remember(queue, queueIndex) {
        derivedStateOf {
            queue.getOrNull(queueIndex)?.let { context.symphony.groove.song.get(it) }
        }
    }
    val fillsTile = geometry.sizeClass == BentoTileSizeClass.XL || geometry.height >= 260f
    val tall = geometry.height >= 200f
    val windowInfo = LocalWindowInfo.current

    DisposableEffect(Unit) {
        onDispose { onNowPlayingTileVisibilityChanged(false) }
    }

    BentoTile(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            val visibleHeight = (
                bounds.bottom.coerceAtMost(windowInfo.containerSize.height.toFloat()) -
                    bounds.top.coerceAtLeast(0f)
                ).coerceIn(0f, bounds.height)
            onNowPlayingTileVisibilityChanged(visibleHeight / bounds.height >= 0.35f)
        },
        tone = BentoTileTone.RAISED,
        sizeClass = geometry.sizeClass,
        onClick = onOpenPlayer,
        contentPadding = when {
            fillsTile -> 0.dp
            else -> null
        },
    ) {
        if (fillsTile) {
            ForYouNowPlayingBackdropContent(
                context = context,
                song = song,
                isPlaying = isPlaying,
                playbackPosition = playbackPosition,
                appFontFamily = appFontFamily,
            )
        } else {
            val padding = bentoTilePaddingDp(geometry.sizeClass)
            val artSize = min(
                geometry.height - 2f * padding.value,
                0.45f * (geometry.width - 2f * padding.value),
            )
            Row(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    song?.createArtworkImageRequest(context.symphony)?.build()
                        ?: Assets.createPlaceholderImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(artSize.dp)
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(18.dp)),
                )
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BentoEqualizerGlyph(
                            color = LocalBentoTileContentColors.current.sub,
                            playing = isPlaying,
                        )
                        Text(
                            context.symphony.t.NowPlaying,
                            style = BentoTypography.label,
                            color = LocalBentoTileContentColors.current.sub,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        song?.title ?: context.symphony.t.NothingIsBeingPlayedRightNow,
                        style = BentoTypography.title.copy(fontFamily = appFontFamily),
                        color = LocalBentoTileContentColors.current.fg,
                        maxLines = if (tall) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    song?.artists?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            it.joinToString(),
                            style = BentoTypography.body,
                            color = LocalBentoTileContentColors.current.sub,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    BentoSeekWave(
                        context = context,
                        position = playbackPosition,
                        playing = isPlaying,
                        height = 24.dp,
                    )
                    ForYouTransportControls(
                        context = context,
                        isPlaying = isPlaying,
                        playButtonSize = 44.dp,
                        playIconSize = 20,
                    )
                }
            }
        }
    }
}

/**
 * Now playing content for tiles whose art fills the tile: artwork under a
 * dark gradient, white text, a dark eyebrow pill in the top-left corner.
 */
@Composable
private fun ForYouNowPlayingBackdropContent(
    context: ViewContext,
    song: Song?,
    isPlaying: Boolean,
    playbackPosition: RadioPlayer.PlaybackPosition,
    appFontFamily: FontFamily?,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            song?.createArtworkImageRequest(context.symphony)?.build()
                ?: Assets.createPlaceholderImageRequest(context.symphony).build(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black.copy(alpha = 0f),
                            0.15f to Color.Black.copy(alpha = 0f),
                            0.45f to Color.Black.copy(alpha = 0.3f),
                            1f to Color.Black.copy(alpha = 0.8f),
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .padding(start = 14.dp, top = 14.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BentoEqualizerGlyph(color = Color.White, playing = isPlaying)
            Text(
                context.symphony.t.NowPlaying,
                style = BentoTypography.label,
                color = Color.White,
                maxLines = 1,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                song?.title ?: context.symphony.t.NothingIsBeingPlayedRightNow,
                style = BentoTypography.display.copy(fontFamily = appFontFamily),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            song?.artists?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    it.joinToString(),
                    style = BentoTypography.rowTitle,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BentoSeekWave(
                context = context,
                position = playbackPosition,
                playing = isPlaying,
                height = 24.dp,
                accent = Color.White,
                track = Color.White.copy(alpha = 0.32f),
            )
            ForYouTransportControls(
                context = context,
                isPlaying = isPlaying,
                playButtonSize = 56.dp,
                playIconSize = 24,
                accent = Color.White,
                onAccent = Color.Black,
                overlay = Color.White,
            )
        }
    }
}

/**
 * Prev / play / next controls of the Now playing tile (DESIGN.md 4.3): 40 dp
 * circles around a play button that is a rounded square while paused and a
 * circle while playing.
 */
@Composable
private fun ForYouTransportControls(
    context: ViewContext,
    isPlaying: Boolean,
    playButtonSize: Dp,
    playIconSize: Int,
    accent: Color = LocalBentoColors.current.accent,
    onAccent: Color = LocalBentoColors.current.onAccent,
    overlay: Color = LocalBentoTileContentColors.current.fg,
) {
    val radius by animateDpAsState(
        targetValue = playButtonSize * when {
            isPlaying -> 0.5f
            else -> BentoRadii.PLAY_BUTTON_PAUSED_RATIO
        },
        label = "bento-np-play-radius",
    )
    Row(
        modifier = Modifier.offset(x = (-8).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        BentoCircleTransportButton(
            icon = Icons.Filled.SkipPrevious,
            contentDescription = context.symphony.t.Previous,
            overlay = overlay,
            onClick = { context.symphony.radio.shorty.previous() },
        )
        Box(
            modifier = Modifier
                .size(playButtonSize)
                .clip(RoundedCornerShape(radius))
                .background(accent)
                .clickable { context.symphony.radio.shorty.playPause() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                when {
                    isPlaying -> Icons.Filled.Pause
                    else -> Icons.Filled.PlayArrow
                },
                when {
                    isPlaying -> context.symphony.t.Pause
                    else -> context.symphony.t.Play
                },
                tint = onAccent,
                modifier = Modifier.size(playIconSize.dp),
            )
        }
        BentoCircleTransportButton(
            icon = Icons.Filled.SkipNext,
            contentDescription = context.symphony.t.Next,
            overlay = overlay,
            onClick = { context.symphony.radio.shorty.skip() },
        )
    }
}

@Composable
private fun BentoCircleTransportButton(
    icon: ImageVector,
    contentDescription: String,
    overlay: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(overlay.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription,
            modifier = Modifier.size(24.dp),
            tint = overlay,
        )
    }
}

/**
 * The "Your library" stat tile (DESIGN.md 4.1, 5.1): label, total hours,
 * songs and albums, with a Rescan pill in the top-right corner (the shell's
 * temporary Rescan action retires here).
 */
@Composable
private fun ForYouLibraryStatTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    appFontFamily: FontFamily?,
    totalDurationMs: Long,
    songCount: Int,
    albumCount: Int,
) {
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Box(modifier = Modifier.fillMaxSize()) {
            BentoStatContent(
                label = "Your library",
                value = String.format(Locale.ROOT, "%.1f h", totalDurationMs / 3_600_000f),
                valueStyle = bentoStatValueStyle(geometry.sizeClass, appFontFamily),
                sub = "${context.symphony.t.XSongs(songCount.toString())} · " +
                    context.symphony.t.XAlbums(albumCount.toString()),
                geometry = geometry,
            )
            BentoCallToActionPill(
                label = context.symphony.t.Rescan,
                wide = geometry.width >= 320f,
                icon = Icons.Rounded.Refresh,
                tilePadding = bentoTilePaddingDp(geometry.sizeClass),
                onClick = rescanLibrary(context),
            )
        }
    }
}

private fun rescanLibrary(context: ViewContext): () -> Unit = {
    context.symphony.radio.stop()
    context.symphony.groove.fetch(
        Groove.FetchOptions(resetInMemoryCache = true),
    )
}

/**
 * A collection tile (DESIGN.md 4.1, "Collection"): a header with "See all"
 * and a grid of square album covers that opens the album page.
 */
@Composable
private fun ForYouAlbumCollectionTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    label: String,
    albums: List<Album>,
) {
    val tileColors = LocalBentoTileContentColors.current
    val padding = bentoTilePaddingDp(geometry.sizeClass)
    val fit = fitBentoCoverGrid(
        tileWidthDp = geometry.width - 2f * padding.value,
        tileHeightDp = geometry.height - 2f * padding.value - 30f,
        itemCount = albums.size,
    )
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            BentoTileHeader(
                label = label,
                actionLabel = context.symphony.t.All,
                showAction = geometry.width >= 220f,
                onAction = { selectHomeSection(context, HomePage.Albums) },
            )
            Box(modifier = Modifier.weight(1f)) {
                if (albums.isNotEmpty() && fit.rows > 0) {
                    BentoCoverGrid(
                        itemCount = albums.size,
                        fit = fit,
                    ) { index ->
                        val album = albums.getOrNull(index) ?: return@BentoCoverGrid
                        Column {
                            AsyncImage(
                                album.createArtworkImageRequest(context.symphony).build(),
                                null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(fit.coverSize.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        context.navController.navigate(AlbumViewRoute(album.id))
                                    },
                            )
                            if (fit.captioned) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    album.name,
                                    style = BentoTypography.secondary.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = tileColors.fg,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(fit.coverSize.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The Artists tile (DESIGN.md 4.1, "People"): a single row of round avatars
 * when short, an avatar grid with names when tall.
 */
@Composable
private fun ForYouArtistsTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    artistNames: List<String>,
) {
    val tileColors = LocalBentoTileContentColors.current
    val padding = bentoTilePaddingDp(geometry.sizeClass)

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        if (geometry.height < 170f) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.width(72.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        context.symphony.t.Artists,
                        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                        color = tileColors.sub,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        context.symphony.t.All,
                        style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                        color = LocalBentoColors.current.accent,
                        maxLines = 1,
                        modifier = Modifier.clickable {
                            selectHomeSection(context, HomePage.Artists)
                        },
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    artistNames.forEach { artistName ->
                        AsyncImage(
                            context.symphony.groove.artist
                                .createArtworkImageRequest(artistName)
                                .build(),
                            artistName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable {
                                    context.navController.navigate(ArtistViewRoute(artistName))
                                },
                        )
                    }
                }
            }
        } else {
            val fit = fitBentoCoverGrid(
                tileWidthDp = geometry.width - 2f * padding.value,
                tileHeightDp = geometry.height - 2f * padding.value - 30f,
                itemCount = artistNames.size,
                gapDp = 12f,
                minSizeDp = 44f,
                captionHeightDp = 24f,
                captionMinSizeDp = 44f,
            )
            Column(modifier = Modifier.fillMaxSize()) {
                BentoTileHeader(
                    label = context.symphony.t.Artists,
                    actionLabel = context.symphony.t.All,
                    showAction = true,
                    onAction = { selectHomeSection(context, HomePage.Artists) },
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (artistNames.isNotEmpty() && fit.rows > 0) {
                        BentoCoverGrid(
                            itemCount = artistNames.size,
                            fit = fit,
                            gap = 12.dp,
                        ) { index ->
                            val artistName = artistNames.getOrNull(index)
                                ?: return@BentoCoverGrid
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width((fit.coverSize + 16f).dp),
                            ) {
                                AsyncImage(
                                    context.symphony.groove.artist
                                        .createArtworkImageRequest(artistName)
                                        .build(),
                                    artistName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(fit.coverSize.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            context.navController.navigate(
                                                ArtistViewRoute(artistName),
                                            )
                                        },
                                )
                                if (fit.captioned) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        artistName,
                                        style = BentoTypography.label,
                                        color = tileColors.fg,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The Up next tile (DESIGN.md 4.1, "List"): 48 dp rows with art, title,
 * artist and duration; tapping a row jumps the queue there.
 */
@Composable
private fun ForYouUpNextTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val upNext by remember(queue, queueIndex) {
        derivedStateOf {
            queue.drop(queueIndex + 1).mapIndexed { index, songId ->
                queueIndex + 1 + index to songId
            }
        }
    }
    val maxRows = maxOf(1, floor((geometry.height - 62f + 2f) / 48f).toInt())
    val rows = upNext.take(maxRows)

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            BentoTileHeader(
                label = "Up next",
                actionLabel = when {
                    upNext.isEmpty() -> "End of queue"
                    else -> context.symphony.t.XSongs(upNext.size.toString())
                },
                showAction = true,
                onAction = null,
            )
            rows.forEach { (queuePosition, songId) ->
                val song = context.symphony.groove.song.get(songId) ?: return@forEach
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { context.symphony.radio.jumpTo(queuePosition) }
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AsyncImage(
                        song.createArtworkImageRequest(context.symphony).build(),
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            song.title,
                            style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                            color = LocalBentoTileContentColors.current.fg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        song.artists.takeIf { it.isNotEmpty() }?.let {
                            Text(
                                it.joinToString(),
                                style = BentoTypography.label,
                                color = LocalBentoTileContentColors.current.sub,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        DurationUtils.formatMs(song.duration),
                        style = BentoTypography.label,
                        color = LocalBentoTileContentColors.current.sub,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
