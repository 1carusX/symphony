package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.radio.RadioPlayer
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoColors
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils
import kotlin.math.max
import kotlin.math.sin

/** Two pi as a Float, for the seek wave phase. */
private const val TAU = (2.0 * Math.PI).toFloat()

/**
 * One entry of a bento placement table (DESIGN.md, Appendix A): a tile's grid
 * position. Columns and rows are counted from 1; spans count cells.
 */
@Immutable
data class BentoTilePlacement(
    val tileId: String,
    val column: Int,
    val row: Int,
    val columnSpan: Int,
    val rowSpan: Int,
)

/**
 * A tile's size class (DESIGN.md 4.1), derived from its pixel size and
 * controlling padding, type and inner layouts. Checked in this order:
 * height >= 300 -> xl, height >= 170 -> l, width >= 200 -> m, width < 92 -> xs,
 * otherwise -> s.
 */
enum class BentoTileSizeClass {
    XL,
    L,
    M,
    S,
    XS,
}

fun resolveBentoTileSizeClass(widthDp: Float, heightDp: Float) = when {
    heightDp >= 300f -> BentoTileSizeClass.XL
    heightDp >= 170f -> BentoTileSizeClass.L
    widthDp >= 200f -> BentoTileSizeClass.M
    widthDp < 92f -> BentoTileSizeClass.XS
    else -> BentoTileSizeClass.S
}

/**
 * Resolved geometry of one placed tile, in dp relative to the grid's content
 * box (inside the page gutter).
 */
@Immutable
data class BentoTileGeometry(
    val offsetX: Float,
    val offsetY: Float,
    val width: Float,
    val height: Float,
    val sizeClass: BentoTileSizeClass,
)

/**
 * Resolved geometry of a whole grid: each placement with its geometry, keyed
 * by tile id, plus the grid's total height.
 */
@Immutable
data class BentoGridMeasure(
    val tiles: Map<String, Pair<BentoTilePlacement, BentoTileGeometry>>,
    val totalHeight: Float,
)

/**
 * Resolves tile offsets, sizes and size classes for a placement table on a
 * grid class. Column widths are equal and fill [innerWidthDp]; a tile's pixel
 * size is `span * cell + (span - 1) * gap` per axis.
 */
fun measureBentoGrid(
    placements: List<BentoTilePlacement>,
    gridClass: BentoGridClass,
    innerWidthDp: Float,
): BentoGridMeasure {
    val columns = gridClass.columns.toFloat()
    val rowHeight = gridClass.rowHeightDp.toFloat()
    val gap = gridClass.gapDp.toFloat()
    val columnWidth = (innerWidthDp - (columns - 1f) * gap) / columns
    val tiles = placements.associate { placement ->
        val width = placement.columnSpan * columnWidth +
            (placement.columnSpan - 1f) * gap
        val height = placement.rowSpan * rowHeight + (placement.rowSpan - 1f) * gap
        val geometry = BentoTileGeometry(
            offsetX = (placement.column - 1f) * (columnWidth + gap),
            offsetY = (placement.row - 1f) * (rowHeight + gap),
            width = width,
            height = height,
            sizeClass = resolveBentoTileSizeClass(width, height),
        )
        placement.tileId to (placement to geometry)
    }
    val maxRow = placements.maxOfOrNull { it.row + it.rowSpan - 1 } ?: 0
    val totalHeight = maxRow * rowHeight + (maxRow - 1f).coerceAtLeast(0f) * gap
    return BentoGridMeasure(tiles = tiles, totalHeight = totalHeight)
}

/**
 * Tile tones (DESIGN.md 4.1): the fill and text colors of a tile.
 */
enum class BentoTileTone {
    /** `surface` fill, `fg` and `sub` text. */
    PLAIN,

    /** `raised` fill, `fg` and `sub` text. */
    RAISED,

    /** `accent` fill, `on-accent` and `on-accent-sub` text. */
    ACCENT,

    /** `tonal` fill, `on-tonal` and `tonal-sub` text. */
    TONAL,
}

/**
 * Text colors of the enclosing [BentoTile], so nested content matches its tone.
 * Provided by [io.github.zyrouge.symphony.ui.theme.bento.BentoTheme] with the
 * plain tone's colors and re-provided by every tile with its own tone.
 */
@Immutable
data class BentoTileContentColors(
    val fg: Color,
    val sub: Color,
)

val LocalBentoTileContentColors = compositionLocalOf<BentoTileContentColors> {
    error("BentoTileContentColors is not provided; wrap content in BentoTheme")
}

/**
 * Tile padding from the size class (DESIGN.md 3.3): 16, or 12 in s, 8 in xs.
 */
fun bentoTilePaddingDp(sizeClass: BentoTileSizeClass) = when (sizeClass) {
    BentoTileSizeClass.XS -> BentoSpacing.TILE_PADDING_EXTRA_SMALL.dp
    BentoTileSizeClass.S -> BentoSpacing.TILE_PADDING_SMALL.dp
    else -> BentoSpacing.TILE_PADDING.dp
}

/**
 * The bento grid engine (DESIGN.md 2.3): places tiles per the grid class and
 * the page's placement table inside the page gutter (16 on compact windows,
 * 24 elsewhere). The grid is as tall as its bottom-most tile; the caller hosts
 * it in a scrollable column.
 */
@Composable
fun BentoGrid(
    gridClass: BentoGridClass,
    placements: List<BentoTilePlacement>,
    modifier: Modifier = Modifier,
    tile: @Composable (tileId: String, placement: BentoTilePlacement, geometry: BentoTileGeometry) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val windowInfo = LocalWindowInfo.current
        val gutter = when {
            windowInfo.containerDpSize.width < 600.dp -> BentoSpacing.GUTTER_COMPACT.dp
            else -> BentoSpacing.GUTTER_OTHER.dp
        }
        val innerWidth = maxWidth - gutter * 2
        val measure = remember(placements, gridClass, innerWidth) {
            measureBentoGrid(placements, gridClass, innerWidth.value)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((measure.totalHeight + 4f).dp)
                .padding(start = gutter, end = gutter, top = 4.dp),
        ) {
            measure.tiles.forEach { (tileId, placed) ->
                val (placement, geometry) = placed
                key(tileId) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    geometry.offsetX.dp.roundToPx(),
                                    geometry.offsetY.dp.roundToPx(),
                                )
                            }
                            .size(geometry.width.dp, geometry.height.dp),
                    ) {
                        tile(tileId, placement, geometry)
                    }
                }
            }
        }
    }
}

/**
 * A bento tile (DESIGN.md 4.1): a rounded fill in one of the four tones, with
 * tone-aware text colors exposed through [LocalBentoTileContentColors], size
 * class padding, a 7 % hover/press overlay with the radius growing to 30, and
 * the 1 dp `line` ring in the Contrast scheme. Tiles are flat; only floating
 * surfaces carry elevation.
 */
@Composable
fun BentoTile(
    sizeClass: BentoTileSizeClass,
    modifier: Modifier = Modifier,
    tone: BentoTileTone = BentoTileTone.PLAIN,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val (fill, fg, sub) = when (tone) {
        BentoTileTone.PLAIN -> Triple(colors.surface, colors.fg, colors.sub)
        BentoTileTone.RAISED -> Triple(colors.raised, colors.fg, colors.sub)
        BentoTileTone.ACCENT -> Triple(colors.accent, colors.onAccent, colors.onAccentSub)
        BentoTileTone.TONAL -> Triple(colors.tonal, colors.onTonal, colors.tonalSub)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val radius by animateDpAsState(
        targetValue = when {
            hovered -> BentoRadii.TILE_HOVER.dp
            else -> bentoTileRadius(sizeClass)
        },
        label = "bento-tile-radius",
    )
    val shape = RoundedCornerShape(radius)
    val overlay = when {
        hovered || pressed -> fg.copy(alpha = 0.07f)
        else -> Color.Transparent
    }
    val outline = when (colorScheme) {
        BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, shape)
        else -> Modifier
    }

    Column(
        modifier = modifier
            .fillMaxSize()
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
            .then(outline)
            .background(fill)
            .drawBehind { drawRect(overlay) }
            .padding(contentPadding ?: bentoTilePaddingDp(sizeClass)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CompositionLocalProvider(LocalBentoTileContentColors provides BentoTileContentColors(fg, sub)) {
            content()
        }
    }
}

/**
 * Text colors a tile of [tone] uses for its content. Tiles read this instead of
 * [LocalBentoTileContentColors] when they compute colors before composing their
 * own [BentoTile], so the tone stays correct at any nesting depth.
 */
fun bentoToneContentColors(tone: BentoTileTone, colors: BentoColors) = when (tone) {
    BentoTileTone.PLAIN, BentoTileTone.RAISED ->
        BentoTileContentColors(colors.fg, colors.sub)

    BentoTileTone.ACCENT -> BentoTileContentColors(colors.onAccent, colors.onAccentSub)
    BentoTileTone.TONAL -> BentoTileContentColors(colors.onTonal, colors.tonalSub)
}

private fun bentoTileRadius(sizeClass: BentoTileSizeClass) = when (sizeClass) {
    BentoTileSizeClass.S, BentoTileSizeClass.XS -> BentoRadii.TILE_SMALL.dp
    else -> BentoRadii.TILE.dp
}

/**
 * True when a sub-label should render at this tile size (DESIGN.md 4.1): it
 * hides in xs tiles and in s tiles under 100 dp high.
 */
fun showsTileSubLabel(sizeClass: BentoTileSizeClass, tileHeightDp: Float) = when (sizeClass) {
    BentoTileSizeClass.XS -> false
    BentoTileSizeClass.S -> tileHeightDp >= 100f
    else -> true
}

/**
 * The cover grid (columns x rows, cell size) that fits a tile (DESIGN.md
 * 4.1): at least three covers, as large as fits, counting at most six
 * towards the covered area so a few big covers beat many small ones.
 * Mirrors the prototype's `fit(aw, ah, N, gap, minS, capH, capMin)`; a
 * caption of [captionHeightDp] below a cell is allowed from
 * [captionMinSizeDp] cell sizes up.
 */
fun fitBentoCoverGrid(
    tileWidthDp: Float,
    tileHeightDp: Float,
    itemCount: Int,
    gapDp: Float = 10f,
    minSizeDp: Float = 40f,
    captionHeightDp: Float = 22f,
    captionMinSizeDp: Float = 100f,
): BentoCoverGridFit {
    var best: BentoCoverGridFit? = null
    var bestScore = -1f
    for (rows in 1..4) {
        for (cols in 1..12) {
            val count = cols * rows
            if (count > itemCount) break
            if (count < minOf(3, itemCount)) continue
            val availableWidth = tileWidthDp - (cols - 1f) * gapDp
            val availableHeight = tileHeightDp - (rows - 1f) * gapDp
            var coverSize = minOf(availableWidth / cols, availableHeight / rows)
            var captioned = false
            if (minOf(availableWidth / cols, availableHeight / rows - captionHeightDp) >= captionMinSizeDp) {
                coverSize = minOf(availableWidth / cols, availableHeight / rows - captionHeightDp)
                captioned = true
            }
            if (coverSize < minSizeDp) continue
            val score = minOf(count, 6) * coverSize * coverSize
            if (score > bestScore) {
                bestScore = score
                best = BentoCoverGridFit(
                    columns = cols,
                    rows = rows,
                    coverSize = coverSize,
                    captioned = captioned,
                )
            }
        }
    }
    return best ?: BentoCoverGridFit(
        columns = 1,
        rows = 1,
        coverSize = max(24f, minOf(tileWidthDp, tileHeightDp)),
        captioned = false,
    )
}

/**
 * The cover layout picked by [fitBentoCoverGrid].
 */
@Immutable
data class BentoCoverGridFit(
    val columns: Int,
    val rows: Int,
    val coverSize: Float,
    val captioned: Boolean,
)

/**
 * A shortcut tile (DESIGN.md 4.1, "Shortcut"): an icon well and a label with
 * an optional sub-label; the Genres shortcut carries a row of chips when the
 * tile is m-sized and large enough.
 */
@Composable
fun BentoShortcutTile(
    geometry: BentoTileGeometry,
    icon: ImageVector,
    label: String,
    sub: String? = null,
    tone: BentoTileTone = BentoTileTone.PLAIN,
    chips: List<String> = emptyList(),
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val tileColors = bentoToneContentColors(tone, LocalBentoColors.current)
    val iconWell = tileColors.fg.copy(alpha = 0.13f)
    val showSub = sub != null && showsTileSubLabel(geometry.sizeClass, geometry.height)
    val chipped = chips.isNotEmpty() &&
        geometry.sizeClass == BentoTileSizeClass.M &&
        geometry.width >= 300f &&
        geometry.height >= 104f

    BentoTile(
        tone = tone,
        sizeClass = geometry.sizeClass,
        onClick = when {
            enabled -> onClick
            else -> null
        },
    ) {
        when {
            // xs: a centered icon with a 12 sp label.
            geometry.sizeClass == BentoTileSizeClass.XS -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    icon,
                    label,
                    tint = tileColors.fg,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = BentoTypography.label,
                    color = tileColors.fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // m: an icon well beside the label; with chips, the well and the
            // label sit on top of a one-row chip strip.
            geometry.sizeClass == BentoTileSizeClass.M -> Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = when {
                    chipped -> Alignment.Top
                    else -> Alignment.CenterVertically
                },
            ) {
                if (chipped) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            BentoIconWell(
                                icon = icon,
                                label = label,
                                wellSize = 36.dp,
                                wellRadius = 12,
                                iconSize = 24,
                                well = iconWell,
                                fg = tileColors.fg,
                            )
                            BentoShortcutTexts(
                                label = label,
                                sub = if (showSub) sub else null,
                                large = false,
                                appFontFamily = appFontFamily,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            chips.forEach { chip ->
                                BentoTileChip(text = chip)
                            }
                        }
                    }
                } else {
                    BentoIconWell(
                        icon = icon,
                        label = label,
                        wellSize = 48.dp,
                        wellRadius = 14,
                        iconSize = 24,
                        well = iconWell,
                        fg = tileColors.fg,
                    )
                    Spacer(Modifier.width(14.dp))
                    BentoShortcutTexts(
                        label = label,
                        sub = if (showSub) sub else null,
                        large = false,
                        appFontFamily = appFontFamily,
                    )
                }
            }

            // s and l/xl: an icon well above the label.
            else -> Column(modifier = Modifier.fillMaxSize()) {
                BentoIconWell(
                    icon = icon,
                    label = label,
                    wellSize = when (geometry.sizeClass) {
                        BentoTileSizeClass.S -> 36.dp
                        else -> 56.dp
                    },
                    wellRadius = when (geometry.sizeClass) {
                        BentoTileSizeClass.S -> 12
                        else -> 16
                    },
                    iconSize = 24,
                    well = iconWell,
                    fg = tileColors.fg,
                )
                Spacer(Modifier.weight(1f))
                BentoShortcutTexts(
                    label = label,
                    sub = if (showSub) sub else null,
                    large = geometry.sizeClass == BentoTileSizeClass.L ||
                        geometry.sizeClass == BentoTileSizeClass.XL,
                    appFontFamily = appFontFamily,
                )
            }
        }
    }
}

@Composable
private fun BentoShortcutTexts(
    label: String,
    sub: String?,
    large: Boolean,
    appFontFamily: FontFamily?,
) {
    val tileColors = LocalBentoTileContentColors.current
    Column {
        Text(
            label,
            style = when {
                large -> BentoTypography.title.copy(fontFamily = appFontFamily)
                else -> BentoTypography.rowTitle.copy(fontFamily = appFontFamily)
            },
            color = tileColors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        sub?.let {
            Text(
                it,
                style = BentoTypography.secondary,
                color = tileColors.sub,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * An icon well: the tile text color at 13 %, with the glyph inside.
 */
@Composable
private fun BentoIconWell(
    icon: ImageVector,
    label: String,
    wellSize: Dp,
    wellRadius: Int,
    iconSize: Int,
    well: Color,
    fg: Color,
) {
    Box(
        modifier = Modifier
            .size(wellSize)
            .clip(RoundedCornerShape(wellRadius.dp))
            .background(well),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            label,
            tint = fg,
            modifier = Modifier.size(iconSize.dp),
        )
    }
}

/**
 * A genre chip inside a tile: 28 dp, the text color at 17 %.
 */
@Composable
private fun BentoTileChip(text: String) {
    val tileColors = LocalBentoTileContentColors.current
    Box(
        modifier = Modifier
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(tileColors.fg.copy(alpha = 0.17f))
            .padding(horizontal = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = BentoTypography.label,
            color = tileColors.fg,
            maxLines = 1,
        )
    }
}

/**
 * The shared stat tile body (DESIGN.md 4.1, "Stat"): label on top, value,
 * sub-label at the bottom.
 */
@Composable
fun BentoStatContent(
    label: String,
    value: String,
    valueStyle: TextStyle,
    sub: String,
    geometry: BentoTileGeometry,
) {
    val tileColors = LocalBentoTileContentColors.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
            color = tileColors.sub,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            value,
            style = valueStyle,
            color = tileColors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showsTileSubLabel(geometry.sizeClass, geometry.height)) {
            Text(
                sub,
                style = BentoTypography.secondary,
                color = tileColors.sub,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Stat value type by size class (DESIGN.md 3.2): 40 in l/xl, 22 in s/xs,
 * 28 otherwise.
 */
fun bentoStatValueStyle(sizeClass: BentoTileSizeClass, fontFamily: FontFamily?) = when (sizeClass) {
    BentoTileSizeClass.XL, BentoTileSizeClass.L -> BentoTypography.statLarge
    BentoTileSizeClass.S, BentoTileSizeClass.XS -> BentoTypography.title.copy(fontFamily = fontFamily)
    else -> BentoTypography.display.copy(fontFamily = fontFamily)
}

/**
 * A call-to-action pill (DESIGN.md 4.1): accent, 36 dp high, radius 18, with
 * an icon and a label, or icon-only below 320 dp wide. The design pins it
 * 12 dp from the tile's top-right corner; callers pass the tile padding so
 * the pill lands there from inside the padded content.
 */
@Composable
fun BoxScope.BentoCallToActionPill(
    label: String,
    wide: Boolean,
    icon: ImageVector,
    tilePadding: Dp,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val inset = 12.dp - tilePadding
    Row(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = inset, y = inset)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.accent)
            .clickable(onClick = onClick)
            .then(
                when {
                    wide -> Modifier.padding(start = 10.dp, end = 14.dp)
                    else -> Modifier.width(36.dp)
                },
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        Icon(
            icon,
            when {
                wide -> null
                else -> label
            },
            tint = colors.onAccent,
            modifier = Modifier.size(18.dp),
        )
        if (wide) {
            Text(
                label,
                style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onAccent,
                maxLines = 1,
            )
        }
    }
}

/**
 * The 22 dp tile header: a 13 sp label on the left and an optional action on
 * the right.
 */
@Composable
fun BentoTileHeader(
    label: String,
    actionLabel: String,
    showAction: Boolean,
    onAction: (() -> Unit)?,
) {
    val tileColors = LocalBentoTileContentColors.current
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
            color = tileColors.sub,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (showAction) {
            val actionModifier = when (onAction) {
                null -> Modifier
                else -> Modifier.clickable(onClick = onAction)
            }
            Text(
                actionLabel,
                style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                color = colors.accent,
                maxLines = 1,
                modifier = actionModifier,
            )
        }
    }
}

/**
 * A grid of square covers (or avatars): rows of fixed-size cells spread with
 * [Arrangement.SpaceBetween], vertically centered, as many rows as [fit]
 * allows. A cell renders nothing beyond [itemCount] cells.
 */
@Composable
fun BoxScope.BentoCoverGrid(
    itemCount: Int,
    fit: BentoCoverGridFit,
    gap: Dp = 10.dp,
    cell: @Composable (index: Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.Center),
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        (0 until fit.rows).forEach { rowIndex ->
            val rowItemCount = minOf(fit.columns, itemCount - rowIndex * fit.columns)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                (0 until rowItemCount).forEach { column ->
                    cell(rowIndex * fit.columns + column)
                }
                repeat(fit.columns - rowItemCount) {
                    Spacer(Modifier.size(fit.coverSize.dp))
                }
            }
        }
    }
}

/**
 * The tile seek bar (DESIGN.md 4.3): an accent wave on a `track` line with a
 * 4 x 22 thumb; the wave animates while playing and flattens into a 4 dp
 * line while paused. Tap and drag seek the current song.
 */
@Composable
fun BentoSeekWave(
    context: ViewContext,
    position: RadioPlayer.PlaybackPosition,
    playing: Boolean,
    height: Dp,
    accent: Color = LocalBentoColors.current.accent,
    track: Color = LocalBentoColors.current.track,
) {
    val hasPlayer = context.symphony.radio.hasPlayer
    // Playback position only arrives every 100 ms (RadioPlayer timer). Interpolate
    // it so the wave slides at frame rate instead of stepping ten times a second.
    val ratioState = animateFloatAsState(
        targetValue = position.ratio,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "bento-seek-ratio",
    )
    val wavePhase = rememberWavePhase(playing)
    val onSeekAtRatio: (Float) -> Unit = { ratio ->
        if (hasPlayer) {
            context.symphony.radio.seek((position.total * ratio.coerceIn(0f, 1f)).toLong())
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .semantics {
                stateDescription = DurationUtils.formatMs(position.played)
                progressBarRangeInfo = ProgressBarRangeInfo(position.ratio, 0f..1f)
            }
            .pointerInput(position.total) {
                detectTapGestures { offset ->
                    onSeekAtRatio(offset.x / size.width.toFloat())
                }
            }
            .pointerInput(position.total) {
                detectHorizontalDragGestures { change, _ ->
                    change.consume()
                    onSeekAtRatio(change.position.x / size.width.toFloat())
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val ratio = ratioState.value
            val centerY = size.height / 2f
            val lineHeight = 5.dp.toPx()
            val halfLine = lineHeight / 2f
            val thumbWidth = 5.dp.toPx()
            val thumbHeight = 20.dp.toPx()
            val gap = thumbWidth + 2.dp.toPx()
            val playedWidth = size.width * ratio
            val restWidth = (size.width * (1f - ratio) - gap)
                .coerceAtLeast(0f)
            drawRoundRect(
                color = track,
                topLeft = Offset(size.width - restWidth, centerY - halfLine),
                size = Size(restWidth, lineHeight),
                cornerRadius = CornerRadius(halfLine),
            )
            val playedWidthWithoutThumb = (playedWidth - gap).coerceAtLeast(0f)
            when {
                playing -> {
                    // A stroked sine: one line of constant weight that undulates.
                    // The stroke stays thinner than twice the amplitude so crests and
                    // troughs stay separate instead of fusing into beads.
                    val period = 22.dp.toPx()
                    val amplitude = 2.5.dp.toPx()
                    val phase = wavePhase.value * period
                    val step = 1.dp.toPx()
                    val strokeWidth = 2.dp.toPx()
                    val path = Path()
                    var x = 0f
                    var y = centerY + sin((x + phase) / period * TAU) * amplitude
                    path.moveTo(x, y)
                    while (x < playedWidthWithoutThumb) {
                        x = minOf(x + step, playedWidthWithoutThumb)
                        y = centerY + sin((x + phase) / period * TAU) * amplitude
                        path.lineTo(x, y)
                    }
                    drawPath(
                        path = path,
                        color = accent,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                    )
                }

                else -> drawRoundRect(
                    color = accent,
                    topLeft = Offset(0f, centerY - halfLine),
                    size = Size(playedWidthWithoutThumb, lineHeight),
                    cornerRadius = CornerRadius(halfLine),
                )
            }
            drawRoundRect(
                color = accent,
                topLeft = Offset(
                    playedWidth - thumbWidth / 2f,
                    centerY - thumbHeight / 2f,
                ),
                size = Size(thumbWidth, thumbHeight),
                cornerRadius = CornerRadius(thumbWidth / 2f),
            )
        }
    }
}

private sealed interface BentoWavePhase {
    @Immutable
    data class Animated(val phase: State<Float>) : BentoWavePhase

    @Immutable
    data class Resting(val phase: Float) : BentoWavePhase
}

private val BentoWavePhase.value: Float
    get() = when (this) {
        is BentoWavePhase.Animated -> phase.value
        is BentoWavePhase.Resting -> phase
    }

@Composable
private fun rememberWavePhase(playing: Boolean): BentoWavePhase = when {
    playing -> {
        val transition = rememberInfiniteTransition(label = "bento-seek-wave")
        val phase = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "bento-seek-wave-phase",
        )
        BentoWavePhase.Animated(phase)
    }

    else -> BentoWavePhase.Resting(0f)
}

/**
 * An equalizer glyph (DESIGN.md 5.1): three bars that bounce while playing
 * and rest at fixed heights otherwise.
 */
@Composable
fun BentoEqualizerGlyph(color: Color, playing: Boolean) {
    val phase = when {
        playing -> {
            val transition = rememberInfiniteTransition(label = "bento-eq")
            val animated by transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "bento-eq-phase",
            )
            animated
        }

        else -> 0f
    }
    Canvas(modifier = Modifier.size(13.dp, 14.dp)) {
        val barWidth = 3.dp.toPx()
        val gap = 2.dp.toPx()
        val restHeights = listOf(0.45f, 0.85f, 0.6f)
        val delays = listOf(0.35f, 0f, 0.6f)
        restHeights.forEachIndexed { i, restHeight ->
            val heightFraction = when {
                playing -> {
                    val t = ((phase + delays[i]) % 1f) * 2f
                    val triangle = when {
                        t < 1f -> t
                        else -> 2f - t
                    }
                    0.2f + 0.8f * triangle
                }

                else -> restHeight
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(i * (barWidth + gap), size.height * (1f - heightFraction)),
                size = Size(barWidth, size.height * heightFraction),
                cornerRadius = CornerRadius(1.dp.toPx()),
            )
        }
    }
}
