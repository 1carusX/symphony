package io.github.zyrouge.symphony.ui.view.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileSizeClass
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.darkened
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.components.BentoEqualizerGlyph
import io.github.zyrouge.symphony.ui.components.BentoTextWithMeta
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.model.UiSong

/**
 * Platform-neutral tiles of the detail pages (DESIGN.md 5.4): header, info,
 * genre and icon covers, empty state, song list containers, chips and
 * subfolder rows. Artwork and song menus stay with the platform.
 */

/** Sub-page header (DESIGN.md 2.2): a back button, a 22 sp title and optional actions. */
@Composable
fun BentoDetailHeader(
    title: String,
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = BentoSizes.HEADER_MIN_HEIGHT_SHORT.dp)
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, null, tint = colors.fg)
        }
        Text(
            title,
            style = BentoTypography.pageTitleShort.copy(fontFamily = appFontFamily),
            color = colors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
    }
}

/** A tile heading: a 13 sp label and an optional count on the right, both `sub`. */
@Composable
fun DetailTileHeading(label: String, end: String?, modifier: Modifier = Modifier) {
    val tileColors = LocalBentoTileContentColors.current
    val style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            style = style,
            color = tileColors.sub,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        end?.let {
            Text(it, style = style, color = tileColors.sub, maxLines = 1)
        }
    }
}

/**
 * The info tile: the kind label, the title (28 sp, 22 in m and narrow tiles)
 * and the counts line. Tiles under 100 dp drop the label, which the header
 * already shows.
 */
@Composable
fun DetailInfoTile(label: String, title: String, meta: String, geometry: BentoTileGeometry) {
    val tileColors = LocalBentoTileContentColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val compact = geometry.height < 170f
    val narrow = geometry.width < 240f
    val titleStyle = when {
        geometry.sizeClass == BentoTileSizeClass.M || narrow -> BentoTypography.title
        else -> BentoTypography.display
    }.copy(fontFamily = appFontFamily)

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (geometry.height >= 100f) {
                Text(
                    label,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                    color = tileColors.sub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                title,
                style = titleStyle,
                color = tileColors.fg,
                maxLines = when {
                    compact -> 1
                    else -> 2
                },
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                meta,
                style = BentoTypography.secondary,
                color = tileColors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


/** The genre swatch colors; mirrors BentoGenreSwatch so the cover matches the Genres tile. */
private val detailGenreSwatch = listOf(
    0xFFEF4444,
    0xFFF97316,
    0xFFF59E0B,
    0xFF16A34A,
    0xFF06B6B4,
    0xFF8B5CF6,
    0xFFD946EF,
    0xFFF43F5E,
    0xFF6366F1,
    0xFFA855F7,
).map { Color(it) }

/** The genre cover tile: the genre's gradient swatch with its name in large type. */
@Composable
fun DetailGenreCoverTile(name: String, geometry: BentoTileGeometry) {
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val size = detailGenreSwatch.size
    val base = detailGenreSwatch[((name.hashCode() % size) + size) % size]
    val fontSize = (geometry.width * 0.18f).coerceIn(18f, 64f)

    BentoTile(
        tone = BentoTileTone.PLAIN,
        sizeClass = geometry.sizeClass,
        contentPadding = 0.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(base, base.darkened())))
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart,
        ) {
            Text(
                name,
                style = TextStyle(
                    fontFamily = appFontFamily,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.03).em,
                    lineHeight = 0.95.em,
                ),
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}


/** A cover tile showing a large icon on `tonal` (folders, favorites, empty playlists). */
@Composable
fun DetailIconCoverTile(icon: ImageVector, geometry: BentoTileGeometry) {
    val colors = LocalBentoColors.current
    val iconSize = minOf(geometry.width, geometry.height) * 0.38f

    BentoTile(tone = BentoTileTone.TONAL, sizeClass = geometry.sizeClass) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = colors.onTonal, modifier = Modifier.size(iconSize.dp))
        }
    }
}

/** The empty state of a list: an icon above a message. */
@Composable
fun DetailEmptyState(icon: ImageVector, message: String, modifier: Modifier = Modifier) {
    val tileColors = LocalBentoTileContentColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, null, tint = tileColors.sub, modifier = Modifier.size(28.dp))
        Text(
            message,
            style = BentoTypography.rowTitle,
            color = tileColors.fg,
            textAlign = TextAlign.Center,
        )
    }
}


/**
 * The songs tile on wide grids: a heading and the rows in a lazy list, so a
 * long list composes only the rows in view and scrolls inside the tile.
 */
@Composable
fun DetailTracksTile(
    label: String,
    countLabel: String,
    geometry: BentoTileGeometry,
    rowCount: Int,
    emptyIcon: ImageVector,
    emptyMessage: String,
    row: @Composable (index: Int) -> Unit,
) {
    BentoTile(
        tone = BentoTileTone.PLAIN,
        sizeClass = geometry.sizeClass,
        contentPadding = 10.dp,
    ) {
        DetailTileHeading(
            label = label,
            end = countLabel,
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 6.dp),
        )
        when (rowCount) {
            0 -> DetailEmptyState(emptyIcon, emptyMessage)
            else -> LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                items(rowCount) { index -> row(index) }
            }
        }
    }
}


class DetailChip(val label: String, val onClick: () -> Unit)

/**
 * A tile of chips (an artist's genres). Short tiles put the label beside a
 * single row of chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailChipsTile(label: String, chips: List<DetailChip>, geometry: BentoTileGeometry) {
    val tileColors = LocalBentoTileContentColors.current
    val chipRow: @Composable (Modifier, Int) -> Unit = { modifier, maxLines ->
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxLines = maxLines,
        ) {
            chips.forEach { DetailChipButton(it) }
        }
    }

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        when {
            geometry.height < 120f -> Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    label,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                    color = tileColors.sub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(72.dp),
                )
                chipRow(Modifier.weight(1f), 1)
            }

            else -> {
                DetailTileHeading(label = label, end = null)
                chipRow(
                    Modifier.fillMaxWidth(),
                    ((geometry.height - 62f + 8f) / 44f).toInt().coerceAtLeast(1),
                )
            }
        }
    }
}

@Composable
private fun DetailChipButton(chip: DetailChip) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val shape = RoundedCornerShape(BentoRadii.CHIP.dp)
    Box(
        modifier = Modifier
            .height(BentoSizes.GENRE_CHIP.dp)
            .clip(shape)
            .then(
                when (colorScheme) {
                    BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, shape)
                    else -> Modifier
                },
            )
            .background(colors.raised)
            .clickable(onClick = chip.onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            chip.label,
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = colors.fg,
            maxLines = 1,
        )
    }
}


/** One row of [DetailFoldersTile]. */
class DetailFolderEntry(val name: String, val sub: String, val onClick: () -> Unit)

/** The subfolders tile of a folder page: folder rows in a lazy list. */
@Composable
fun DetailFoldersTile(
    label: String,
    countLabel: String,
    folders: List<DetailFolderEntry>,
    geometry: BentoTileGeometry,
) {
    val colors = LocalBentoColors.current
    val tileColors = LocalBentoTileContentColors.current

    BentoTile(
        tone = BentoTileTone.PLAIN,
        sizeClass = geometry.sizeClass,
        contentPadding = 10.dp,
    ) {
        DetailTileHeading(
            label = label,
            end = countLabel,
            modifier = Modifier.padding(start = 6.dp, end = 6.dp, top = 6.dp),
        )
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            items(folders) { folder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BentoSizes.TILE_LIST_ROW.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = folder.onClick)
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(BentoRadii.TILE_LIST_ART_36.dp))
                            .background(colors.tonal),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Folder,
                            null,
                            tint = colors.onTonal,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            folder.name,
                            style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                            color = tileColors.fg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            folder.sub,
                            style = BentoTypography.label,
                            color = tileColors.sub,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Icon(
                        Icons.Rounded.ChevronRight,
                        null,
                        tint = tileColors.sub,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

/**
 * A song row inside a list tile (the prototype's `.ql` row): 48 dp, 36 dp
 * art with an equalizer while playing, title, a secondary line, the
 * duration and a More button that opens the song menu. A long press opens
 * the menu too.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailSongRow(
    song: UiSong,
    secondary: String,
    playing: Boolean,
    isPlaying: Boolean,
    durationText: String,
    moreContentDescription: String,
    artwork: @Composable (Modifier) -> Unit,
    menu: (@Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val tileColors = LocalBentoTileContentColors.current
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = BentoSizes.TILE_LIST_ROW.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
            .padding(start = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (leading != null) {
            leading()
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(BentoRadii.TILE_LIST_ART_36.dp)),
                contentAlignment = Alignment.Center,
            ) {
                artwork(Modifier.fillMaxSize())
                if (playing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BentoEqualizerGlyph(color = Color.White, playing = isPlaying)
                    }
                }
            }
        }
        // At a large scale the duration gives way to the title.
        BentoTextWithMeta(
            modifier = Modifier.weight(1f),
            gap = 10.dp,
            em = 14.sp,
            meta = {
                Text(
                    durationText,
                    style = bentoTabularStyle(BentoTypography.label),
                    color = tileColors.sub,
                    maxLines = 1,
                )
            },
        ) {
            Column {
                Text(
                    song.title,
                    style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                    color = when {
                        playing -> colors.accent
                        else -> tileColors.fg
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondary.isNotEmpty()) {
                    Text(
                        secondary,
                        style = BentoTypography.label,
                        color = tileColors.sub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (menu != null) {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        moreContentDescription,
                        tint = tileColors.sub,
                        modifier = Modifier.size(20.dp),
                    )
                }
                menu.invoke(showMenu, { showMenu = false })
            }
        }
    }
}


/** One avatar of [DetailPeopleTile]. */
class DetailPerson(
    val name: String,
    val artwork: @Composable (Modifier) -> Unit,
    val onClick: () -> Unit,
)

/**
 * A row of round avatars (similar artists, the artists of a genre): as many
 * as fit. Tiles under 140 dp put the label beside the avatars and drop the
 * names.
 */
@Composable
fun DetailPeopleTile(label: String, people: List<DetailPerson>, geometry: BentoTileGeometry) {
    val tileColors = LocalBentoTileContentColors.current

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        when {
            geometry.height < 140f -> {
                val avatarSize = minOf(52f, geometry.height - 32f)
                val count = ((geometry.width - 32f - 84f + 8f) / (avatarSize + 8f)).toInt()
                    .coerceIn(1, people.size.coerceAtLeast(1))
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        label,
                        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                        color = tileColors.sub,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(72.dp),
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        people.take(count).forEach { DetailAvatar(it, avatarSize, showName = false) }
                    }
                }
            }

            else -> {
                val avatarSize = if (geometry.height < 170f) 48f else 56f
                val count = ((geometry.width - 32f + 12f) / (avatarSize + 28f)).toInt()
                    .coerceIn(1, people.size.coerceAtLeast(1))
                DetailTileHeading(label = label, end = null)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    people.take(count).forEach { DetailAvatar(it, avatarSize, showName = true) }
                }
            }
        }
    }
}

@Composable
private fun DetailAvatar(person: DetailPerson, sizeDp: Float, showName: Boolean) {
    val tileColors = LocalBentoTileContentColors.current
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = person.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        person.artwork(
            Modifier
                .size(sizeDp.dp)
                .clip(CircleShape),
        )
        if (showName) {
            Text(
                person.name,
                style = BentoTypography.label.copy(fontWeight = FontWeight.SemiBold),
                color = tileColors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.width((sizeDp + 20f).dp),
            )
        }
    }
}

/** A cover tile filled with platform artwork (the artist avatar). */
@Composable
fun DetailImageCoverTile(
    geometry: BentoTileGeometry,
    artwork: @Composable (Modifier) -> Unit,
) {
    BentoTile(
        tone = BentoTileTone.RAISED,
        sizeClass = geometry.sizeClass,
        contentPadding = 0.dp,
    ) {
        artwork(Modifier.fillMaxSize())
    }
}

/** The playlist cover tile: a 2 x 2 mosaic of the first songs' artwork. */
@Composable
fun DetailMosaicCoverTile(
    geometry: BentoTileGeometry,
    artworks: List<@Composable (Modifier) -> Unit>,
) {
    BentoTile(
        tone = BentoTileTone.PLAIN,
        sizeClass = geometry.sizeClass,
        contentPadding = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            artworks.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    row.forEach { artwork ->
                        artwork(
                            Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}
