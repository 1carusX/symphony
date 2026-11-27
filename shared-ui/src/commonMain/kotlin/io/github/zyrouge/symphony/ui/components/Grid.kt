package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** Two pi as a Float, for the seek wave phase. */

/**
 * The bento grid engine (DESIGN.md 2.3): places tiles per the grid class and
 * the page's placement table inside the page gutter (16 on compact windows,
 * 24 elsewhere). The grid is as tall as its bottom-most tile; the caller hosts
 * it in a scrollable column. Rows grow with the font scale, and a grid too
 * narrow for its text ([bentoTooNarrowForText]) reflows onto two columns.
 */
@Composable
fun BentoGrid(
    gridClass: BentoGridClass,
    placements: List<BentoTilePlacement>,
    modifier: Modifier = Modifier,
    squareTileId: String? = null,
    tile: @Composable (tileId: String, placement: BentoTilePlacement, geometry: BentoTileGeometry) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val windowInfo = LocalWindowInfo.current
        val gutter = when {
            windowInfo.containerDpSize.width < 600.dp -> BentoSpacing.GUTTER_COMPACT.dp
            else -> BentoSpacing.GUTTER_OTHER.dp
        }
        val innerWidth = maxWidth - gutter * 2
        val fontScale = LocalDensity.current.fontScale
        val rowScale = fontScale.coerceAtLeast(1f)
        val reflow = gridClass.columns > 2 && bentoTooNarrowForText(innerWidth.value, fontScale)
        val measure = remember(placements, gridClass, innerWidth, squareTileId, rowScale, reflow) {
            measureBentoGrid(placements, gridClass, innerWidth.value, squareTileId, rowScale, reflow)
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
    // Room kept free at the end of the bottom line, for a pill in the bottom-right corner.
    bottomEndInset: Dp = 0.dp,
) {
    val tileColors = LocalBentoTileContentColors.current
    // A wide one-row tile (4x1 on phones) has room for the label and value
    // only; drop the sub line there instead of clipping it.
    val subFits = with(LocalDensity.current) {
        val available = geometry.height.dp - bentoTilePaddingDp(geometry.sizeClass) * 2
        val needed = BentoTypography.secondary.lineHeight.toDp() * 2 + valueStyle.lineHeight.toDp()
        needed <= available
    }
    val showSub = showsTileSubLabel(geometry.sizeClass, geometry.height) && subFits
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
        // A value wider than the tile (a long duration in a narrow tile) shrinks to
        // fit, down to 60 % of its size, rather than cutting off.
        BasicText(
            value,
            style = valueStyle.copy(color = tileColors.fg),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(end = if (showSub) 0.dp else bottomEndInset),
            autoSize = TextAutoSize.StepBased(
                minFontSize = valueStyle.fontSize * 0.6f,
                maxFontSize = valueStyle.fontSize,
                stepSize = 1.sp,
            ),
        )
        if (showSub) {
            BasicText(
                sub,
                style = BentoTypography.secondary.copy(color = tileColors.sub),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = bottomEndInset),
                // Beside a corner pill the lines are short; they may shrink a little to fit.
                autoSize = if (bottomEndInset > 0.dp) {
                    TextAutoSize.StepBased(
                        minFontSize = BentoTypography.secondary.fontSize * 0.85f,
                        maxFontSize = BentoTypography.secondary.fontSize,
                        stepSize = 0.5.sp,
                    )
                } else {
                    null
                },
            )
        }
    }
}

/**
 * A call-to-action pill (DESIGN.md 4.1): accent, 36 dp high, radius 18, with
 * an icon and a label, or icon-only below 320 dp wide. The design pins it
 * 12 dp from the tile's top-right corner ([bottom]: bottom-right, for narrow
 * tiles whose value needs the top line); callers pass the tile padding so
 * the pill lands there from inside the padded content.
 */
@Composable
fun BoxScope.BentoCallToActionPill(
    label: String,
    wide: Boolean,
    icon: ImageVector,
    tilePadding: Dp,
    onClick: () -> Unit,
    bottom: Boolean = false,
) {
    val colors = LocalBentoColors.current
    val inset = 12.dp - tilePadding
    Row(
        modifier = Modifier
            .align(if (bottom) Alignment.BottomEnd else Alignment.TopEnd)
            .offset(x = inset, y = if (bottom) -inset else inset)
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
