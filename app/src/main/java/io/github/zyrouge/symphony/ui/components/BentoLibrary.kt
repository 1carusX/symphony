package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.view.LocalHomeContentAtEnd
import io.github.zyrouge.symphony.ui.view.LocalHomeContentAtTop
import io.github.zyrouge.symphony.utils.DurationUtils
import io.github.zyrouge.symphony.utils.withCase
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Shared building blocks of the bento library pages (DESIGN.md 4.4 rows,
 * 5.2 Songs, 5.3 grouped tiles, 5.5 search filters) plus the pure layout
 * logic those pages need: letter grouping, masonry distribution, group
 * ordering and the album placement helpers.
 */

// ---------------------------------------------------------------------------
// Pure logic
// ---------------------------------------------------------------------------

/**
 * The number of rows a grouped tile shows (DESIGN.md 5.3): up to six, with a
 * "Show all" row replacing the sixth song when the group is longer.
 */
const val BENTO_GROUP_MAX_ROWS = 6

@Composable
fun rememberBentoPlayingSong(context: ViewContext): BentoPlayingSong {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    return remember(queue, queueIndex, isPlaying) {
        BentoPlayingSong(songId = queue.getOrNull(queueIndex), isPlaying = isPlaying)
    }
}

// ---------------------------------------------------------------------------
// Tone surface
// ---------------------------------------------------------------------------

/**
 * A tile-shaped surface in one of the bento tones (DESIGN.md 4.1): tone fill,
 * the 7 % hover/press overlay, the radius growing to 30 on hover, and the
 * 1 dp line ring in the Contrast scheme. Unlike [BentoTile] the padding and
 * inner layout belong to the caller.
 */
@Composable
fun BentoToneSurface(
    modifier: Modifier = Modifier,
    tone: BentoTileTone = BentoTileTone.PLAIN,
    radiusDp: Int = BentoRadii.TILE,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val (fill, fg) = when (tone) {
        BentoTileTone.PLAIN -> colors.surface to colors.fg
        BentoTileTone.RAISED -> colors.raised to colors.fg
        BentoTileTone.ACCENT -> colors.accent to colors.onAccent
        BentoTileTone.TONAL -> colors.tonal to colors.onTonal
    }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(
        when {
            hovered -> BentoRadii.TILE_HOVER.dp
            else -> radiusDp.dp
        },
    )
    Box(
        modifier = modifier
            .let { m ->
                when (onClick) {
                    null -> m
                    else -> m
                        .hoverable(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick,
                        )
                }
            }
            .clip(shape)
            .then(
                when (colorScheme) {
                    BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, shape)
                    else -> Modifier
                },
            )
            .background(fill)
            .then(
                when {
                    hovered || pressed -> Modifier.background(fg.copy(alpha = 0.07f))
                    else -> Modifier
                },
            ),
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Action tiles (DESIGN.md 5.2)
// ---------------------------------------------------------------------------

/**
 * An action tile (DESIGN.md 5.2): 72 dp high, an icon well beside the label
 * and sub-label. Below 520 dp inner width the non-main tiles stack the icon
 * over the label and drop their sub-label.
 */
@Composable
fun BentoActionTile(
    icon: ImageVector,
    label: String,
    sub: String?,
    tone: BentoTileTone = BentoTileTone.PLAIN,
    main: Boolean = false,
    narrow: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (fg, subColor) = when (tone) {
        BentoTileTone.PLAIN -> colors.fg to colors.sub
        BentoTileTone.RAISED -> colors.fg to colors.sub
        BentoTileTone.ACCENT -> colors.onAccent to colors.onAccentSub
        BentoTileTone.TONAL -> colors.onTonal to colors.tonalSub
    }
    val stacked = narrow && !main
    BentoToneSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.ACTION_TILE.dp),
        tone = tone,
        onClick = onClick,
    ) {
        if (stacked) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(icon, label, tint = fg, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(BentoRadii.ICON_WELL_40.dp))
                        .background(fg.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, label, tint = fg, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(
                        label,
                        style = BentoTypography.rowTitle,
                        color = fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (sub != null) {
                        Text(
                            sub,
                            style = BentoTypography.secondary,
                            color = subColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Song rows (DESIGN.md 4.4)
// ---------------------------------------------------------------------------

/**
 * The grouped-page columns (DESIGN.md 5.3): 1 column below 560 dp inner
 * width, 2 at 560-999, 3 at 1000 and up. With at most 2 x columns groups
 * the tiles form an even grid; with more they flow down masonry columns.
 * Each entry renders through [entry] with its resolved tile width.
 */
@Composable
fun <T> BentoGroupedTileColumns(
    entries: List<T>,
    entryHeightDp: (T) -> Float,
    modifier: Modifier = Modifier,
    entry: @Composable (T, tileWidthDp: Float) -> Unit,
) {
    val windowInfo = LocalWindowInfo.current
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compactWindow = windowInfo.containerDpSize.width < 600.dp
        val gutter = when {
            compactWindow -> BentoSpacing.GUTTER_COMPACT.dp
            else -> BentoSpacing.GUTTER_OTHER.dp
        }
        val gap = when {
            compactWindow -> BentoSpacing.GAP_COMPACT.dp
            else -> BentoSpacing.GAP_OTHER.dp
        }
        val innerWidth = maxWidth - gutter * 2
        val maxColumns = when {
            innerWidth >= 1000.dp -> 3
            innerWidth >= 560.dp -> 2
            else -> 1
        }
        val columns = resolveBentoGroupColumnCount(entries.size, maxColumns)
        val evenGrid = isBentoGroupEvenGrid(entries.size, maxColumns)
        val tileWidth = (innerWidth - gap * (columns - 1)) / columns

        val scrollState = rememberScrollState()
        val contentAtEnd = LocalHomeContentAtEnd.current
        val contentAtTop = LocalHomeContentAtTop.current
        androidx.compose.runtime.LaunchedEffect(scrollState) {
            androidx.compose.runtime.snapshotFlow {
                scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 4
            }.collect { contentAtEnd.value = it }
        }
        androidx.compose.runtime.LaunchedEffect(scrollState) {
            androidx.compose.runtime.snapshotFlow { scrollState.value <= 4 }
                .collect { contentAtTop.value = it }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = gutter, end = gutter, bottom = 8.dp),
        ) {
            when {
                evenGrid -> entries.chunked(columns).forEach { rowEntries ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = gap),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        rowEntries.forEach { entryValue ->
                            Box(modifier = Modifier.weight(1f)) {
                                entry(entryValue, tileWidth.value)
                            }
                        }
                        repeat(columns - rowEntries.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                else -> {
                    val columnsEntries = distributeBentoMasonry(entries, columns, entryHeightDp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        columnsEntries.forEach { columnEntries ->
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(gap),
                            ) {
                                columnEntries.forEach { entryValue ->
                                    Box {
                                        entry(entryValue, tileWidth.value)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Search filters and chips (DESIGN.md 5.5)
// ---------------------------------------------------------------------------

/**
 * A smart filter tile (DESIGN.md 5.5): 64 dp (56 in the side column), an
 * icon well, the filter name and its song count; the active filter turns
 * accent and also orders the results.
 */
@Composable
fun BentoSmartFilterTile(
    icon: ImageVector,
    label: String,
    count: String,
    active: Boolean,
    sideColumn: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (fg, subColor, well) = when {
        active -> Triple(colors.onAccent, colors.onAccentSub, colors.onAccent)
        else -> Triple(colors.fg, colors.sub, colors.fg)
    }
    BentoToneSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                when {
                    sideColumn -> BentoSizes.SMART_FILTER_SIDE_COLUMN.dp
                    else -> BentoSizes.SMART_FILTER.dp
                },
            ),
        tone = when {
            active -> BentoTileTone.ACCENT
            else -> BentoTileTone.PLAIN
        },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(BentoRadii.ICON_WELL_40.dp))
                    .background(well.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, label, tint = fg, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    label,
                    style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    count,
                    style = BentoTypography.secondary,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * A genre chip (DESIGN.md 5.5): 36 dp, radius 18, `surface` with the
 * selected chip in `tonal`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoGenreChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .height(BentoSizes.GENRE_CHIP.dp)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(BentoRadii.CHIP.dp))
            .background(
                when {
                    selected -> colors.tonal
                    hovered -> colors.raised
                    else -> colors.surface
                },
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = when {
                selected -> colors.onTonal
                else -> colors.fg
            },
            maxLines = 1,
        )
    }
}

/**
 * An artist or album hit above the song results (DESIGN.md 5.5): 60 dp,
 * `raised`, with a 40 dp avatar or artwork.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoSearchHitChip(
    request: ImageRequest,
    title: String,
    subtitle: String,
    round: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .height(60.dp)
            .widthIn(max = 240.dp)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(
                when {
                    hovered -> colors.high
                    else -> colors.raised
                },
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AsyncImage(
            request,
            title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(
                    when {
                        round -> CircleShape
                        else -> RoundedCornerShape(10.dp)
                    },
                ),
        )
        Column {
            Text(
                title,
                style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                style = BentoTypography.label,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * A mini Play/Shuffle button of the results header (DESIGN.md 4.3): 36 dp,
 * radius 18, accent or tonal.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoMiniButton(
    icon: ImageVector,
    label: String,
    tone: BentoTileTone,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (bg, fg) = when (tone) {
        BentoTileTone.ACCENT -> colors.accent to colors.onAccent
        BentoTileTone.TONAL -> colors.tonal to colors.onTonal
        else -> colors.surface to colors.fg
    }
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(BentoRadii.CHIP.dp))
            .background(bg)
            .combinedClickable(onClick = onClick)
            .padding(start = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, label, tint = fg, modifier = Modifier.size(18.dp))
        Text(
            label,
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Sort menu (DESIGN.md 4.5)
// ---------------------------------------------------------------------------

/** One sort option of [BentoSortMenu]. */
@Immutable
data class BentoSortOption<T>(val value: T, val label: String)

/**
 * The sort menu (DESIGN.md 4.5): "Sort songs by", one 48 dp row per option
 * with a check mark on the current one, then "Reverse order" with an On/Off
 * value pill. Rendered in a Material dropdown with the bento surface, shape
 * and item styling.
 */
@Composable
fun <T> BentoSortMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    options: List<BentoSortOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    reverse: Boolean,
    reverseLabel: String,
    onReverseChange: (Boolean) -> Unit,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = colors.high,
        shape = RoundedCornerShape(BentoRadii.MENU.dp),
        shadowElevation = 16.dp,
        border = when (colorScheme) {
            BentoColorScheme.CONTRAST -> BorderStroke(1.dp, colors.line)
            else -> null
        },
    ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Column {
                Text(
                    title,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.Bold),
                    color = colors.muted,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 8.dp),
                )
                options.forEach { option ->
                    MenuRow(
                        leading = {
                            if (option.value == selected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    option.label,
                                    tint = colors.sub,
                                    modifier = Modifier.size(22.dp),
                                )
                            } else {
                                Spacer(Modifier.size(22.dp))
                            }
                        },
                        label = option.label,
                        value = null,
                        onClick = {
                            onDismissRequest()
                            onSelect(option.value)
                        },
                    )
                }
                MenuRow(
                    leading = {
                        Icon(
                            Icons.AutoMirrored.Rounded.Sort,
                            reverseLabel,
                            tint = colors.sub,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    label = reverseLabel,
                    value = if (reverse) "On" else "Off",
                    onClick = {
                        onDismissRequest()
                        onReverseChange(!reverse)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MenuRow(
    leading: @Composable () -> Unit,
    label: String,
    value: String?,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.MENU_ITEM.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        leading()
        Text(
            label,
            style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Medium),
            color = colors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        value?.let {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.tonal)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    it,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onTonal,
                    maxLines = 1,
                )
            }
        }
    }
}
