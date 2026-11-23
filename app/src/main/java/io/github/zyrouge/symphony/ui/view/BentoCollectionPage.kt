package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.Settings
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoActionTile
import io.github.zyrouge.symphony.ui.components.BentoGrid
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTilePlacement
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.bentoBottomInset
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoGridClass
import io.github.zyrouge.symphony.ui.view.detail.BentoDetailHeader
import io.github.zyrouge.symphony.ui.view.detail.DetailEmptyState
import io.github.zyrouge.symphony.ui.view.detail.DetailInfoTile
import io.github.zyrouge.symphony.ui.view.detail.DetailSideSlot
import io.github.zyrouge.symphony.ui.view.detail.DetailSongRow
import io.github.zyrouge.symphony.ui.view.detail.DetailTileHeading
import io.github.zyrouge.symphony.ui.view.detail.DetailTileIds
import io.github.zyrouge.symphony.ui.view.detail.DetailTracksTile
import io.github.zyrouge.symphony.ui.view.detail.detailListRowSpan
import io.github.zyrouge.symphony.ui.view.detail.detailMaxTracksRowSpan
import io.github.zyrouge.symphony.ui.view.detail.detailPlacements
import kotlin.math.roundToInt

/**
 * A secondary tile of a detail page (albums, genres, similar artists,
 * subfolders): its id, the rows it needs on a grid class, whether it sits
 * above the songs on phones, and its content.
 */
class DetailSideTile(
    val id: String,
    val leading: Boolean = false,
    val rowSpan: (BentoGridClass) -> Int,
    val content: @Composable (BentoTileGeometry) -> Unit,
)

/**
 * A detail page for a collection of songs (artist, album artist, genre,
 * playlist, folder) in the album frame (DESIGN.md 5.4, the prototype's
 * detail layout): cover, info, Play / Shuffle / Queue action tiles (Length
 * on wider grids), the songs and the side tiles.
 *
 * Phones (c4) stack everything in one lazy list, so the songs compose as
 * they scroll into view. Wider grids keep the bento layout; there the songs
 * tile is at most a viewport tall and scrolls its lazy rows inside.
 *
 * [onEditSongs] adds an Edit songs tile in the Queue slot and moves Queue to
 * the Length slot. Sorting follows [sortByEntry] and [sortReverseEntry].
 */
@Composable
fun BentoCollectionPage(
    context: ViewContext,
    label: String,
    title: String,
    songIds: List<String>,
    onBack: () -> Unit,
    emptyMessage: String,
    cover: @Composable (BentoTileGeometry) -> Unit,
    modifier: Modifier = Modifier,
    metaPrefix: String? = null,
    trailingMenu: (@Composable () -> Unit)? = null,
    sideTiles: List<DetailSideTile> = emptyList(),
    onEditSongs: (() -> Unit)? = null,
    rowSecondary: (Song) -> String = ::defaultDetailRowSecondary,
    rowMenuExtra: (@Composable ColumnScope.(Song, () -> Unit) -> Unit)? = null,
    sortByEntry: Settings.EnumEntry<SongRepository.SortBy> = context.symphony.settings.lastUsedSongsSortBy,
    sortReverseEntry: Settings.BooleanEntry = context.symphony.settings.lastUsedSongsSortReverse,
) {
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val grooveVersion = LocalGrooveVersion.current
    val sortBy by sortByEntry.flow.collectAsState()
    val sortReverse by sortReverseEntry.flow.collectAsState()
    val songs by remember(songIds, sortBy, sortReverse, grooveVersion) {
        derivedStateOf {
            context.symphony.groove.song.get(
                context.symphony.groove.song.sort(songIds, sortBy, sortReverse),
            )
        }
    }
    val queueIds = remember(songs) { songs.map { it.id } }
    val totalMs = remember(songs) { songs.sumOf { it.duration } }
    val playingSong = rememberBentoPlayingSong(context)
    val favorites by context.symphony.groove.playlist.favorites.collectAsState()
    val favoriteIds = remember(favorites) { favorites.toSet() }
    val songCountLabel = context.symphony.t.XSongs(songs.size.toString())
    val meta = listOfNotNull(metaPrefix, songCountLabel, formatBentoDurationMinutes(totalMs))
        .joinToString(" · ")
    val actionA = when (onEditSongs) {
        null -> DetailTileIds.QUEUE
        else -> DetailTileIds.EDIT
    }
    val actionB = when (onEditSongs) {
        null -> DetailTileIds.LENGTH
        else -> DetailTileIds.QUEUE
    }

    val songRow: @Composable (Int) -> Unit = { index ->
        val song = songs[index]
        DetailSongRow(
            context = context,
            song = song,
            secondary = rowSecondary(song),
            playing = song.id == playingSong.songId,
            isPlaying = playingSong.isPlaying,
            isFavorite = song.id in favoriteIds,
            menuExtra = rowMenuExtra,
            onClick = {
                context.symphony.radio.shorty.playQueue(
                    queueIds,
                    options = Radio.PlayOptions(index = index),
                    source = title,
                )
            },
        )
    }

    val tile: @Composable (String, BentoTileGeometry) -> Unit = { tileId, geometry ->
        when (tileId) {
            DetailTileIds.COVER -> cover(geometry)

            DetailTileIds.INFO -> DetailInfoTile(
                label = label,
                title = title,
                meta = meta,
                geometry = geometry,
            )

            DetailTileIds.PLAY -> BentoActionTile(
                icon = Icons.Filled.PlayArrow,
                label = context.symphony.t.Play,
                sub = formatBentoDurationMinutes(totalMs),
                tone = BentoTileTone.ACCENT,
                onClick = {
                    if (queueIds.isNotEmpty()) {
                        context.symphony.radio.shorty.playQueue(queueIds, source = title)
                    }
                },
            )

            DetailTileIds.SHUFFLE -> BentoActionTile(
                icon = Icons.Filled.Shuffle,
                label = context.symphony.t.ShufflePlay,
                sub = null,
                tone = BentoTileTone.TONAL,
                narrow = geometry.width < 200f,
                onClick = {
                    if (queueIds.isNotEmpty()) {
                        context.symphony.radio.shorty.playQueue(queueIds, shuffle = true, source = title)
                    }
                },
            )

            DetailTileIds.QUEUE -> BentoActionTile(
                icon = Icons.Filled.Queue,
                label = context.symphony.t.Queue,
                sub = "Add $songCountLabel",
                narrow = geometry.width < 200f,
                onClick = { context.symphony.radio.queue.add(queueIds) },
            )

            DetailTileIds.EDIT -> BentoActionTile(
                icon = Icons.Filled.Edit,
                label = context.symphony.t.ManageSongs,
                sub = null,
                narrow = geometry.width < 200f,
                onClick = { onEditSongs?.invoke() },
            )

            DetailTileIds.LENGTH -> AlbumLengthStatTile(
                context = context,
                geometry = geometry,
                appFontFamily = appFontFamily,
                songs = songs,
            )

            DetailTileIds.TRACKS -> DetailTracksTile(
                label = context.symphony.t.Songs,
                countLabel = songCountLabel,
                geometry = geometry,
                rowCount = songs.size,
                emptyIcon = Icons.Filled.MusicNote,
                emptyMessage = emptyMessage,
                row = songRow,
            )

            else -> sideTiles.firstOrNull { it.id == tileId }?.content?.invoke(geometry)
        }
    }

    BentoDetailFrame(
        label = label,
        onBack = onBack,
        modifier = modifier,
        trailingMenu = trailingMenu,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val gridClass = rememberBentoGridClass(maxWidth.value.roundToInt())
            when (gridClass) {
                BentoGridClass.C4 -> DetailStackedLayout(
                    gridClass = gridClass,
                    actionA = actionA,
                    sideTiles = sideTiles,
                    tile = tile,
                    songsLabel = context.symphony.t.Songs,
                    songCountLabel = songCountLabel,
                    songCount = songs.size,
                    emptyMessage = emptyMessage,
                    songRow = songRow,
                )

                else -> DetailGridLayout(
                    gridClass = gridClass,
                    viewportHeightDp = maxHeight.value,
                    actionA = actionA,
                    actionB = actionB,
                    sideTiles = sideTiles,
                    songCount = songs.size,
                    tile = tile,
                )
            }
        }
    }
}

/** A detail page whose collection does not exist (any more): the header and a message. */
@Composable
fun BentoCollectionUnknownPage(
    label: String,
    message: String,
    icon: ImageVector,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BentoDetailFrame(label = label, onBack = onBack, modifier = modifier) {
        DetailEmptyState(icon = icon, message = message, modifier = Modifier.padding(top = 40.dp))
    }
}

@Composable
private fun BentoDetailFrame(
    label: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingMenu: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BentoDetailHeader(title = label, onBack = onBack, trailing = trailingMenu)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                content()
            }
        }
    }
}

/**
 * Wide grids: the bento layout in a scrolling column. The songs tile spans
 * the rows its list needs, capped at the viewport above the capsule.
 */
@Composable
private fun DetailGridLayout(
    gridClass: BentoGridClass,
    viewportHeightDp: Float,
    actionA: String,
    actionB: String,
    sideTiles: List<DetailSideTile>,
    songCount: Int,
    tile: @Composable (String, BentoTileGeometry) -> Unit,
) {
    val bottomInset = bentoBottomInset(16.dp)
    val tracksRowSpan = minOf(
        detailListRowSpan(gridClass, songCount),
        detailMaxTracksRowSpan(gridClass, viewportHeightDp - bottomInset.value),
    )
    val placements = detailPlacements(
        gridClass = gridClass,
        tracksRowSpan = tracksRowSpan,
        side = sideTiles.map { DetailSideSlot(it.id, it.rowSpan(gridClass), it.leading) },
        actionA = actionA,
        actionB = actionB,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        BentoGrid(
            gridClass = gridClass,
            placements = placements,
            modifier = Modifier.fillMaxWidth(),
        ) { tileId, _, geometry ->
            tile(tileId, geometry)
        }
        Spacer(Modifier.height(bottomInset))
    }
}

/**
 * Phones (c4): one lazy list. The hero grid (cover, info, actions) and the
 * leading side tiles come first, then the songs as lazy rows drawn as one
 * tile, then the other side tiles.
 */
@Composable
private fun DetailStackedLayout(
    gridClass: BentoGridClass,
    actionA: String,
    sideTiles: List<DetailSideTile>,
    tile: @Composable (String, BentoTileGeometry) -> Unit,
    songsLabel: String,
    songCountLabel: String,
    songCount: Int,
    emptyMessage: String,
    songRow: @Composable (Int) -> Unit,
) {
    val colors = LocalBentoColors.current
    val gap = gridClass.gapDp.dp
    val gutter = when {
        LocalWindowInfo.current.containerDpSize.width < 600.dp -> BentoSpacing.GUTTER_COMPACT.dp
        else -> BentoSpacing.GUTTER_OTHER.dp
    }
    val radius = BentoRadii.TILE.dp
    val topShape = RoundedCornerShape(topStart = radius, topEnd = radius)
    val bottomShape = RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
    val hero = remember(gridClass, actionA) {
        detailPlacements(gridClass, 1, emptyList(), actionA, null)
            .filterNot { it.tileId == DetailTileIds.TRACKS }
    }
    val sideTile: @Composable (DetailSideTile) -> Unit = { side ->
        // BentoGrid adds 4 dp above its tiles; the spacer makes up the gap.
        Spacer(Modifier.height(gap - 4.dp))
        BentoGrid(
            gridClass = gridClass,
            placements = listOf(
                BentoTilePlacement(side.id, 1, 1, gridClass.columns, side.rowSpan(gridClass)),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) { _, _, geometry ->
            side.content(geometry)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = bentoBottomInset(16.dp)),
    ) {
        item(key = "hero") {
            BentoGrid(
                gridClass = gridClass,
                placements = hero,
                modifier = Modifier.fillMaxWidth(),
            ) { tileId, _, geometry ->
                tile(tileId, geometry)
            }
        }
        sideTiles.filter { it.leading }.forEach { side ->
            item(key = "side-${side.id}") { Column { sideTile(side) } }
        }
        item(key = "songs-heading") {
            DetailTileHeading(
                label = songsLabel,
                end = songCountLabel,
                modifier = Modifier
                    .padding(start = gutter, end = gutter, top = gap)
                    .background(colors.surface, topShape)
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            )
        }
        when (songCount) {
            0 -> item(key = "songs-empty") {
                DetailEmptyState(
                    icon = Icons.Filled.MusicNote,
                    message = emptyMessage,
                    modifier = Modifier
                        .padding(horizontal = gutter)
                        .background(colors.surface, bottomShape),
                )
            }

            else -> items(songCount) { index ->
                val last = index == songCount - 1
                Box(
                    modifier = Modifier
                        .padding(horizontal = gutter)
                        .fillMaxWidth()
                        .background(
                            colors.surface,
                            when {
                                last -> bottomShape
                                else -> RectangleShape
                            },
                        )
                        .padding(
                            start = 10.dp,
                            end = 10.dp,
                            bottom = when {
                                last -> 10.dp
                                else -> 0.dp
                            },
                        ),
                ) {
                    songRow(index)
                }
            }
        }
        sideTiles.filterNot { it.leading }.forEach { side ->
            item(key = "side-${side.id}") { Column { sideTile(side) } }
        }
    }
}

private fun defaultDetailRowSecondary(song: Song) = listOfNotNull(
    song.artists.joinToString().takeIf { it.isNotEmpty() },
    song.album,
).joinToString(" · ")
