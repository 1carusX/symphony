package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.AlbumDropdownMenu
import io.github.zyrouge.symphony.ui.components.BentoActionTile
import io.github.zyrouge.symphony.ui.components.BentoCoverGrid
import io.github.zyrouge.symphony.ui.components.BentoEqualizerGlyph
import io.github.zyrouge.symphony.ui.components.BentoGrid
import io.github.zyrouge.symphony.ui.components.BentoStatContent
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileHeader
import io.github.zyrouge.symphony.ui.components.BentoTilePlacement
import io.github.zyrouge.symphony.ui.components.BentoTileSizeClass
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.bentoAlbumTracksRowSpan
import io.github.zyrouge.symphony.ui.components.bentoStatValueStyle
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.components.bentoTilePaddingDp
import io.github.zyrouge.symphony.ui.components.fitBentoCoverGrid
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoGridClass
import io.github.zyrouge.symphony.utils.DurationUtils
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class AlbumViewRoute(val albumId: String)

/**
 * The Album detail page (DESIGN.md 5.4): a bento grid with the cover, the
 * album info, Play / Shuffle / Queue action tiles, the tracks list and
 * "More like this", per the Appendix A "album" placement tables. The header
 * is a back button with a 22 sp title; the kit has no overflow menu, so the
 * existing album options stay in the header.
 */
@Composable
fun AlbumView(context: ViewContext, route: AlbumViewRoute) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val allAlbumIds by context.symphony.groove.album.all.collectAsState()
    val album by remember(allAlbumIds) {
        derivedStateOf { context.symphony.groove.album.get(route.albumId) }
    }
    val isViable by remember(allAlbumIds) {
        derivedStateOf { allAlbumIds.contains(route.albumId) }
    }
    var showOptionsMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Sub-page header (DESIGN.md 2.2): a back button and a 22 sp
            // title. The overflow menu keeps the existing album options.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = BentoSizes.HEADER_MIN_HEIGHT_SHORT.dp)
                    .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(
                    content = {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            null,
                            tint = colors.fg,
                        )
                    },
                    onClick = { context.navController.popBackStack() },
                )
                Text(
                    context.symphony.t.Album,
                    style = BentoTypography.pageTitleShort.copy(fontFamily = appFontFamily),
                    color = colors.fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Box {
                    IconButton(
                        content = {
                            Icon(
                                Icons.Rounded.MoreVert,
                                context.symphony.t.Details,
                                tint = colors.fg,
                            )
                        },
                        onClick = { showOptionsMenu = !showOptionsMenu },
                    )
                    album?.let {
                        AlbumDropdownMenu(
                            context,
                            it,
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false },
                        )
                    }
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isViable -> album?.let { value ->
                        AlbumGrid(
                            context = context,
                            album = value,
                            appFontFamily = appFontFamily,
                        )
                    }

                    else -> UnknownAlbum(context, route.albumId)
                }
            }
        }
    }
}

/**
 * Album placement tables (DESIGN.md, Appendix A, "album"). The Tracks tile
 * spans `rs` rows: `ceil((32 + 30 + trackCount x 48 + gap) / (rowHeight +
 * gap))`.
 */
private fun albumPlacements(gridClass: BentoGridClass, tracksRowSpan: Int) = when (gridClass) {
    BentoGridClass.C4 -> listOf(
        BentoTilePlacement("acover", 1, 1, 2, 2),
        BentoTilePlacement("ainfo", 3, 1, 2, 2),
        BentoTilePlacement("aplay", 1, 3, 2, 1),
        BentoTilePlacement("ashuf", 3, 3, 1, 1),
        BentoTilePlacement("aqueue", 4, 3, 1, 1),
        BentoTilePlacement("tracks", 1, 4, 4, tracksRowSpan),
        BentoTilePlacement("more", 1, 4 + tracksRowSpan, 4, 2),
    )

    BentoGridClass.C6 -> listOf(
        BentoTilePlacement("acover", 1, 1, 2, 2),
        BentoTilePlacement("ainfo", 3, 1, 4, 1),
        BentoTilePlacement("aplay", 3, 2, 2, 1),
        BentoTilePlacement("ashuf", 5, 2, 1, 1),
        BentoTilePlacement("aqueue", 6, 2, 1, 1),
        BentoTilePlacement("tracks", 1, 3, 4, tracksRowSpan),
        BentoTilePlacement("more", 5, 3, 2, maxOf(2, tracksRowSpan)),
    )

    BentoGridClass.C8, BentoGridClass.SHORT -> listOf(
        BentoTilePlacement("acover", 1, 1, 3, 3),
        BentoTilePlacement("ainfo", 4, 1, 5, 1),
        BentoTilePlacement("aplay", 4, 2, 2, 1),
        BentoTilePlacement("ashuf", 6, 2, 1, 1),
        BentoTilePlacement("aqueue", 7, 2, 1, 1),
        BentoTilePlacement("astat", 8, 2, 1, 1),
        BentoTilePlacement("tracks", 4, 3, 5, tracksRowSpan),
        BentoTilePlacement("more", 1, 4, 3, 2),
    )

    BentoGridClass.C12 -> listOf(
        BentoTilePlacement("acover", 1, 1, 4, 3),
        BentoTilePlacement("ainfo", 5, 1, 8, 1),
        BentoTilePlacement("aplay", 5, 2, 2, 1),
        BentoTilePlacement("ashuf", 7, 2, 2, 1),
        BentoTilePlacement("aqueue", 9, 2, 2, 1),
        BentoTilePlacement("astat", 11, 2, 2, 1),
        BentoTilePlacement("tracks", 5, 3, 8, tracksRowSpan),
        BentoTilePlacement("more", 1, 4, 4, 2),
    )
}

@Composable
private fun AlbumGrid(
    context: ViewContext,
    album: Album,
    appFontFamily: FontFamily?,
) {
    val playingSong = rememberBentoPlayingSong(context)
    val trackSongIds by remember(album.id) {
        derivedStateOf {
            context.symphony.groove.song.sort(
                album.getSongIds(context.symphony),
                SongRepository.SortBy.TRACK_NUMBER,
                false,
            )
        }
    }
    val songs by remember(trackSongIds) {
        derivedStateOf {
            trackSongIds.mapNotNull { context.symphony.groove.song.get(it) }
        }
    }
    val moreAlbums by remember(album.id) {
        derivedStateOf {
            val artistAlbumIds = album.artists.flatMap { artistName ->
                context.symphony.groove.artist.getAlbumIds(artistName)
            }
            artistAlbumIds
                .filterNot { it == album.id }
                .mapNotNull { context.symphony.groove.album.get(it) }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val gridClass = rememberBentoGridClass(maxWidth.value.roundToInt())
        val tracksRowSpan = remember(songs.size, gridClass) {
            bentoAlbumTracksRowSpan(songs.size, gridClass)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            BentoGrid(
                gridClass = gridClass,
                placements = albumPlacements(gridClass, tracksRowSpan),
                modifier = Modifier.fillMaxWidth(),
            ) { tileId, _, geometry ->
                when (tileId) {
                    "acover" -> AlbumCoverTile(context = context, album = album, geometry = geometry)

                    "ainfo" -> AlbumInfoTile(
                        context = context,
                        album = album,
                        geometry = geometry,
                        appFontFamily = appFontFamily,
                    )

                    "aplay" -> BentoActionTile(
                        icon = Icons.Filled.PlayArrow,
                        label = context.symphony.t.Play,
                        sub = formatBentoDurationMinutes(songs.sumOf { it.duration }),
                        tone = BentoTileTone.ACCENT,
                        onClick = {
                            context.symphony.radio.shorty.playQueue(trackSongIds)
                        },
                    )

                    "ashuf" -> BentoActionTile(
                        icon = Icons.Filled.Shuffle,
                        label = context.symphony.t.ShufflePlay,
                        sub = null,
                        tone = BentoTileTone.TONAL,
                        narrow = true,
                        onClick = {
                            context.symphony.radio.shorty.playQueue(
                                trackSongIds,
                                shuffle = true,
                            )
                        },
                    )

                    "aqueue" -> BentoActionTile(
                        icon = Icons.Filled.Queue,
                        label = context.symphony.t.Queue,
                        sub = "Add " + context.symphony.t.XSongs(songs.size.toString()),
                        onClick = {
                            context.symphony.radio.queue.add(trackSongIds)
                        },
                    )

                    "astat" -> AlbumLengthStatTile(
                        context = context,
                        geometry = geometry,
                        appFontFamily = appFontFamily,
                        songs = songs,
                    )

                    "tracks" -> AlbumTracksTile(
                        context = context,
                        songs = songs,
                        trackSongIds = trackSongIds,
                        geometry = geometry,
                        playingSongId = playingSong.songId,
                        playing = playingSong.isPlaying,
                    )

                    "more" -> AlbumMoreLikeThisTile(
                        context = context,
                        geometry = geometry,
                        label = context.symphony.t.MoreLikeThis,
                        albums = moreAlbums,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
