package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.components.BentoStatContent
import io.github.zyrouge.symphony.ui.components.bentoStatValueStyle
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.model.UiSong
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoActionTile
import io.github.zyrouge.symphony.ui.components.BentoGrid
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTilePlacement
import io.github.zyrouge.symphony.ui.components.BentoTileTone
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
    label: String,
    title: String,
    songs: List<UiSong>,
    t: Strings,
    onBack: () -> Unit,
    emptyMessage: String,
    cover: @Composable (BentoTileGeometry) -> Unit,
    playingSongId: String?,
    isPlaying: Boolean,
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
    onQueue: () -> Unit,
    onSongClick: (UiSong) -> Unit,
    modifier: Modifier = Modifier,
    metaPrefix: String? = null,
    trailingMenu: (@Composable () -> Unit)? = null,
    sideTiles: List<DetailSideTile> = emptyList(),
    onEditSongs: (() -> Unit)? = null,
    rowSecondary: (UiSong) -> String = ::defaultDetailRowSecondary,
    rowDurationText: (UiSong) -> String,
    rowArtwork: @Composable (UiSong, Modifier) -> Unit,
    rowMenu: (@Composable (UiSong, Boolean, () -> Unit) -> Unit)? = null,
    bottomInset: Dp = 0.dp,
    info: (@Composable (BentoTileGeometry) -> Unit)? = null,
    rowLeading: (@Composable (UiSong, Boolean, Boolean) -> Unit)? = null,
    tracksLabel: String = t.Songs,
) {
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val queueIds = remember(songs) { songs.map { it.id } }
    val totalMs = remember(songs) { songs.sumOf { it.durationMillis } }
    val songCountLabel = t.XSongs(songs.size.toString())
    val meta = listOfNotNull(metaPrefix, songCountLabel, formatBentoDurationMinutes(totalMs, t))
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
            song = song,
            secondary = rowSecondary(song),
            playing = song.id == playingSongId,
            isPlaying = isPlaying,
            durationText = rowDurationText(song),
            moreContentDescription = t.More,
            artwork = { modifier -> rowArtwork(song, modifier) },
            menu = rowMenu?.let { slot ->
                { expanded, dismiss -> slot(song, expanded, dismiss) }
            },
            leading = rowLeading?.let { slot ->
                { slot(song, song.id == playingSongId, isPlaying) }
            },
            onClick = { onSongClick(song) },
        )
    }

    val tile: @Composable (String, BentoTileGeometry) -> Unit = { tileId, geometry ->
        when (tileId) {
            DetailTileIds.COVER -> cover(geometry)

            DetailTileIds.INFO -> when {
                info != null -> info.invoke(geometry)
                else -> DetailInfoTile(
                    label = label,
                    title = title,
                    meta = meta,
                    geometry = geometry,
                )
            }

            DetailTileIds.PLAY -> BentoActionTile(
                fillHeight = true,
                icon = Icons.Filled.PlayArrow,
                label = t.Play,
                sub = formatBentoDurationMinutes(totalMs, t),
                tone = BentoTileTone.ACCENT,
                onClick = onPlayAll,
            )

            DetailTileIds.SHUFFLE -> BentoActionTile(
                fillHeight = true,
                icon = Icons.Filled.Shuffle,
                label = t.ShufflePlay,
                sub = null,
                tone = BentoTileTone.TONAL,
                narrow = geometry.width < 200f,
                onClick = onShuffle,
            )

            DetailTileIds.QUEUE -> BentoActionTile(
                fillHeight = true,
                icon = Icons.Filled.Queue,
                label = t.Queue,
                sub = t.AddX(songCountLabel),
                narrow = geometry.width < 200f,
                onClick = onQueue,
            )

            DetailTileIds.EDIT -> BentoActionTile(
                fillHeight = true,
                icon = Icons.Filled.Edit,
                label = t.ManageSongs,
                sub = null,
                narrow = geometry.width < 200f,
                onClick = { onEditSongs?.invoke() },
            )

            DetailTileIds.LENGTH -> DetailLengthStatTile(
                t = t,
                totalMs = totalMs,
                songCount = songs.size,
                geometry = geometry,
                appFontFamily = appFontFamily,
            )

            DetailTileIds.TRACKS -> DetailTracksTile(
                label = tracksLabel,
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
                    songsLabel = tracksLabel,
                    songCountLabel = songCountLabel,
                    songCount = songs.size,
                    emptyMessage = emptyMessage,
                    songRow = songRow,
                    bottomInset = bottomInset,
                )

                else -> DetailGridLayout(
                    gridClass = gridClass,
                    viewportHeightDp = maxHeight.value,
                    actionA = actionA,
                    actionB = actionB,
                    sideTiles = sideTiles,
                    songCount = songs.size,
                    tile = tile,
                    bottomInset = bottomInset,
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
    bottomInset: Dp,
) {
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
            squareTileId = DetailTileIds.COVER,
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
    bottomInset: Dp,
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
        contentPadding = PaddingValues(bottom = bottomInset),
    ) {
        item(key = "hero") {
            BentoGrid(
                gridClass = gridClass,
                placements = hero,
                modifier = Modifier.fillMaxWidth(),
                squareTileId = DetailTileIds.COVER,
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

/** The default secondary line of a song row: artists and album. */
fun defaultDetailRowSecondary(song: UiSong) = listOfNotNull(
    song.artists.joinToString().takeIf { it.isNotEmpty() },
    song.album,
).joinToString(" · ")

/** The Length stat tile (DESIGN.md 5.4, wider grids only). */
@Composable
private fun DetailLengthStatTile(
    t: Strings,
    totalMs: Long,
    songCount: Int,
    geometry: BentoTileGeometry,
    appFontFamily: androidx.compose.ui.text.font.FontFamily?,
) {
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        BentoStatContent(
            label = t.Length,
            value = formatBentoDurationMinutes(totalMs, t),
            valueStyle = bentoStatValueStyle(geometry.sizeClass, appFontFamily),
            sub = t.XSongs(songCount.toString()),
            geometry = geometry,
        )
    }
}
