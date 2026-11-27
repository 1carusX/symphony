package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.UiAlbum
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoGridClass

/** A playlist shortcut in the For you "mix" tile. */
data class ForYouMix(
    val title: String,
    val sub: String,
)

/** Everything the For you page renders; the platform prepares it from its library. */
data class ForYouPageData(
    val songCount: Int,
    val albumCount: Int,
    val genreNames: List<String>,
    val playlistCount: Int,
    val folderCount: Int,
    val totalDurationMs: Long,
    val recentlyAddedAlbums: List<UiAlbum>,
    val suggestedAlbums: List<UiAlbum>,
    val suggestedArtists: List<String>,
    val upNext: List<ForYouUpNextEntry>,
    val mix: ForYouMix?,
    val songsEnabled: Boolean,
)

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

/**
 * The For you dashboard (DESIGN.md Appendix A): scrolls the tile grid for the
 * current grid class. The platform prepares [data], supplies the artwork,
 * navigation and the Now playing tile through slots.
 */
@Composable
fun ForYouPage(
    data: ForYouPageData,
    strings: Strings,
    appFontFamily: FontFamily?,
    nowPlaying: @Composable (BentoTileGeometry) -> Unit,
    artworkAlbum: @Composable (UiAlbum, Modifier) -> Unit,
    artworkArtist: @Composable (String, Modifier) -> Unit,
    artworkSong: @Composable (UiSong, Modifier) -> Unit,
    onShuffle: () -> Unit,
    onRescan: () -> Unit,
    onOpenGenres: () -> Unit,
    onOpenPlaylists: () -> Unit,
    onOpenFolders: () -> Unit,
    onOpenAlbums: () -> Unit,
    onOpenArtists: () -> Unit,
    onOpenSongs: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAlbum: (UiAlbum) -> Unit,
    onOpenArtist: (String) -> Unit,
    onOpenMix: () -> Unit,
    onJumpQueue: (Int) -> Unit,
    onContentAtEndChanged: (Boolean) -> Unit = {},
    onContentAtTopChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val gridClass = rememberBentoGridClass(maxWidth.value.roundToInt())
        val scrollState = rememberScrollState()
        LaunchedEffect(scrollState) {
            snapshotFlow {
                scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 4
            }.collect(onContentAtEndChanged)
        }
        LaunchedEffect(scrollState) {
            snapshotFlow { scrollState.value <= 4 }
                .collect(onContentAtTopChanged)
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
                "np" -> nowPlaying(geometry)

                "shuffle" -> BentoShortcutTile(
                    geometry = geometry,
                    tone = BentoTileTone.ACCENT,
                    icon = Icons.Rounded.Shuffle,
                    label = strings.ShufflePlay,
                    sub = strings.XSongs(data.songCount.toString()),
                    enabled = data.songsEnabled,
                    onClick = onShuffle,
                )

                "stat" -> ForYouLibraryStatTile(
                    geometry = geometry,
                    appFontFamily = appFontFamily,
                    totalDurationMs = data.totalDurationMs,
                    songCount = data.songCount,
                    albumCount = data.albumCount,
                    strings = strings,
                    onRescan = onRescan,
                )

                "recent" -> ForYouAlbumCollectionTile(
                    geometry = geometry,
                    label = strings.RecentlyAddedSongs,
                    seeAllLabel = strings.All,
                    albums = data.recentlyAddedAlbums,
                    artwork = artworkAlbum,
                    onAlbumClick = onOpenAlbum,
                    onSeeAll = onOpenAlbums,
                )

                "suggested" -> ForYouAlbumCollectionTile(
                    geometry = geometry,
                    label = strings.SuggestedAlbums,
                    seeAllLabel = strings.All,
                    albums = data.suggestedAlbums,
                    artwork = artworkAlbum,
                    onAlbumClick = onOpenAlbum,
                    onSeeAll = onOpenAlbums,
                )

                "genres" -> BentoShortcutTile(
                    geometry = geometry,
                    icon = Icons.Rounded.Sell,
                    label = strings.Genres,
                    sub = strings.XGenres(data.genreNames.size.toString()),
                    chips = data.genreNames,
                    onClick = onOpenGenres,
                )

                "playlists" -> BentoShortcutTile(
                    geometry = geometry,
                    icon = Icons.AutoMirrored.Rounded.QueueMusic,
                    label = strings.Playlists,
                    sub = strings.XPlaylists(data.playlistCount.toString()),
                    onClick = onOpenPlaylists,
                )

                "folders" -> BentoShortcutTile(
                    geometry = geometry,
                    icon = Icons.Rounded.Folder,
                    label = strings.Folders,
                    sub = strings.XFolders(data.folderCount.toString()),
                    onClick = onOpenFolders,
                )

                "albums" -> BentoShortcutTile(
                    geometry = geometry,
                    icon = Icons.Rounded.Album,
                    label = strings.Albums,
                    sub = strings.XAlbums(data.albumCount.toString()),
                    onClick = onOpenAlbums,
                )

                "songs" -> BentoShortcutTile(
                    geometry = geometry,
                    icon = Icons.Rounded.MusicNote,
                    label = strings.Songs,
                    sub = strings.XSongs(data.songCount.toString()),
                    onClick = onOpenSongs,
                )

                "settings" -> BentoShortcutTile(
                    geometry = geometry,
                    icon = Icons.Rounded.Tune,
                    label = strings.Settings,
                    sub = strings.ThemeAndColors,
                    onClick = onOpenSettings,
                )

                "mix" -> data.mix?.let { mix ->
                    BentoShortcutTile(
                        geometry = geometry,
                        tone = BentoTileTone.TONAL,
                        icon = Icons.AutoMirrored.Rounded.QueueMusic,
                        label = mix.title,
                        sub = mix.sub,
                        onClick = onOpenMix,
                    )
                }

                "artists" -> ForYouArtistsTile(
                    geometry = geometry,
                    label = strings.Artists,
                    seeAllLabel = strings.All,
                    artistNames = data.suggestedArtists,
                    artwork = artworkArtist,
                    onArtistClick = onOpenArtist,
                    onSeeAll = onOpenArtists,
                )

                "upnext" -> ForYouUpNextTile(
                    geometry = geometry,
                    label = strings.UpNext,
                    actionLabel = when {
                        data.upNext.isEmpty() -> strings.EndOfQueue
                        else -> strings.XSongs(data.upNext.size.toString())
                    },
                    entries = data.upNext,
                    artwork = artworkSong,
                    onJump = onJumpQueue,
                )
            }
        }
            Spacer(Modifier.height(16.dp))
        }
    }
}
