package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * One destination in the home navigation: an icon, a label and an optional
 * count shown by the wide rail (Songs, Albums, ...).
 */
data class HomeNavItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val count: Int? = null,
)

/** One section pill in the compact-window tabs row. */
data class HomeTab(
    val key: String,
    val label: String,
)

/** Section pills for compact windows (DESIGN.md §2.2). */
@Composable
fun HomeTabsRow(
    items: List<HomeTab>,
    selectedKey: String,
    onSelect: (key: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val tabStyle = BentoTypography.body.copy(
        fontWeight = FontWeight.SemiBold,
        fontFamily = appFontFamily,
    )
    val scrollState = rememberScrollState()
    var viewportWidth by remember { mutableFloatStateOf(0f) }
    var tabCenters by remember { mutableStateOf(emptyMap<String, Float>()) }
    var initialScroll by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg)
            .onGloballyPositioned { viewportWidth = it.size.width.toFloat() }
            .padding(top = 4.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = BentoSpacing.GUTTER_COMPACT.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEach { item ->
                val selected = item.key == selectedKey
                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            val center = it.positionInParent().x + it.size.width / 2f
                            tabCenters = tabCenters + (item.key to center)
                        }
                        .height(BentoSizes.TAB.dp)
                        .clip(RoundedCornerShape((BentoSizes.TAB / 2).dp))
                        .background(
                            when {
                                selected -> colors.accent
                                else -> colors.surface
                            }
                        )
                        .clickable { onSelect(item.key) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        item.label,
                        style = tabStyle,
                        color = when {
                            selected -> colors.onAccent
                            else -> colors.sub
                        },
                        maxLines = 1,
                    )
                }
            }
        }
    }

    LaunchedEffect(selectedKey, tabCenters) {
        val center = tabCenters[selectedKey] ?: return@LaunchedEffect
        if (viewportWidth <= 0f) return@LaunchedEffect
        val target = (center - viewportWidth / 2f)
            .coerceIn(0f, scrollState.maxValue.toFloat())
            .roundToInt()
        if (initialScroll) {
            scrollState.scrollTo(target)
            initialScroll = false
        } else {
            scrollState.animateScrollTo(target)
        }
    }
}

/**
 * Page title with the phone Search and Settings actions (DESIGN.md §2.2).
 */
@Composable
fun HomePageHeader(
    title: String,
    windowClass: BentoWindowClass,
    showActions: Boolean,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    searchLabel: String,
    settingsLabel: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val (titleStyle, paddings, minHeight) = when (windowClass) {
        BentoWindowClass.COMPACT -> Triple(
            BentoTypography.pageTitleCompact.copy(fontFamily = appFontFamily),
            PaddingValues(
                start = BentoSpacing.GUTTER_COMPACT.dp,
                end = 8.dp,
                top = 14.dp,
                bottom = 6.dp,
            ),
            BentoSizes.HEADER_MIN_HEIGHT_PHONE.dp,
        )

        BentoWindowClass.SHORT_HEIGHT -> Triple(
            BentoTypography.pageTitleShort.copy(fontFamily = appFontFamily),
            PaddingValues(
                start = BentoSpacing.GUTTER_OTHER.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 6.dp,
            ),
            BentoSizes.HEADER_MIN_HEIGHT_SHORT.dp,
        )

        else -> Triple(
            BentoTypography.pageTitle.copy(fontFamily = appFontFamily),
            PaddingValues(
                start = BentoSpacing.GUTTER_OTHER.dp,
                end = BentoSpacing.GUTTER_OTHER.dp,
                top = 22.dp,
                bottom = 14.dp,
            ),
            BentoSizes.HEADER_MIN_HEIGHT.dp,
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .padding(paddings),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Crossfade(
            label = "home-title",
            targetState = title,
            modifier = Modifier.weight(1f),
        ) { value ->
            Text(
                value,
                style = titleStyle,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (showActions) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onSearch) {
                    Icon(
                        Icons.Rounded.Search,
                        searchLabel,
                        tint = colors.fg,
                    )
                }
                IconButton(onClick = onSettings) {
                    Icon(
                        Icons.Rounded.Tune,
                        settingsLabel,
                        tint = colors.fg,
                    )
                }
            }
        }
        trailing?.invoke()
    }
}

/**
 * Labeled rail for medium to large windows (DESIGN.md §2.1): 96 dp wide, a
 * Search button on top and Settings pinned to the bottom.
 */
@Composable
fun HomeRailLabeled(
    items: List<HomeNavItem>,
    selectedKey: String,
    onSelect: (key: String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    searchLabel: String,
    settingsLabel: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(BentoSizes.RAIL_WIDTH.dp)
            .fillMaxHeight()
            .navigationBarsPadding()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HomeRailSearchFab(searchLabel = searchLabel, short = false, onClick = onSearch)
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items.forEach { item ->
                HomeRailItemLabeled(
                    icon = item.icon,
                    label = item.label,
                    selected = item.key == selectedKey,
                    onClick = { onSelect(item.key) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        HomeRailItemLabeled(
            icon = Icons.Rounded.Tune,
            label = settingsLabel,
            selected = false,
            onClick = onSettings,
        )
    }
}

@Composable
private fun HomeRailItemLabeled(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier = Modifier
            .width(BentoSizes.RAIL_ITEM.dp)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(
                when {
                    selected -> colors.tonal
                    hovered -> colors.fg.copy(alpha = 0.07f)
                    else -> Color.Transparent
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .padding(top = 8.dp, bottom = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.height(28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(22.dp),
                tint = when {
                    selected -> colors.onTonal
                    else -> colors.sub
                },
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = BentoTypography.label.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = appFontFamily,
            ),
            color = when {
                selected -> colors.onTonal
                else -> colors.sub
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Icon-only rail for short-height windows (DESIGN.md §2.1): 80 dp wide,
 * with a long-press tooltip for each item.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeRailShort(
    items: List<HomeNavItem>,
    selectedKey: String,
    onSelect: (key: String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    searchLabel: String,
    settingsLabel: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var tooltipLabel by remember { mutableStateOf<String?>(null) }
    var tooltipCenterYInRoot by remember { mutableFloatStateOf(0f) }
    var railYInRoot by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .width(BentoSizes.RAIL_SHORT.dp)
            .fillMaxHeight()
            .onGloballyPositioned { railYInRoot = it.boundsInRoot().top },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HomeRailSearchFab(searchLabel = searchLabel, short = true, onClick = onSearch)
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items.forEach { item ->
                    HomeRailItemShort(
                        icon = item.icon,
                        label = item.label,
                        selected = item.key == selectedKey,
                        onClick = { onSelect(item.key) },
                        onShowTooltip = { label, centerYInRoot ->
                            tooltipLabel = label
                            tooltipCenterYInRoot = centerYInRoot
                        },
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            HomeRailItemShort(
                icon = Icons.Rounded.Tune,
                label = settingsLabel,
                selected = false,
                onClick = onSettings,
                onShowTooltip = { label, centerYInRoot ->
                    tooltipLabel = label
                    tooltipCenterYInRoot = centerYInRoot
                },
            )
        }

        tooltipLabel?.let { label ->
            Box(
                modifier = Modifier
                    .absoluteOffset {
                        val gap = 8.dp.roundToPx()
                        val railWidth = BentoSizes.RAIL_SHORT.dp.roundToPx()
                        val x = when (isRtl) {
                            true -> -(railWidth + gap)
                            false -> railWidth + gap
                        }
                        IntOffset(
                            x,
                            (tooltipCenterYInRoot - railYInRoot - 14.dp.roundToPx()).roundToInt(),
                        )
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.tonal)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    label,
                    style = BentoTypography.label.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = appFontFamily,
                    ),
                    color = colors.onTonal,
                )
            }
        }
    }

    LaunchedEffect(tooltipLabel) {
        if (tooltipLabel != null) {
            delay(1200)
            tooltipLabel = null
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeRailItemShort(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onShowTooltip: (label: String, centerYInRoot: Float) -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    var centerYInRoot by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { centerYInRoot = it.boundsInRoot().center.y }
            .padding(vertical = 2.dp)
            .size(52.dp, 36.dp)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(18.dp))
            .background(
                when {
                    selected -> colors.tonal
                    hovered -> colors.fg.copy(alpha = 0.09f)
                    else -> Color.Transparent
                }
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = { onShowTooltip(label, centerYInRoot) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            label,
            modifier = Modifier.size(22.dp),
            tint = when {
                selected -> colors.onTonal
                else -> colors.sub
            },
        )
    }
}

/**
 * Wide rail for extra-large windows (DESIGN.md §2.2): 240 dp, the app name,
 * a labeled Search button, 52 dp rows with counts and Settings at the end.
 */
@Composable
fun HomeRailWide(
    items: List<HomeNavItem>,
    selectedKey: String,
    onSelect: (key: String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    searchLabel: String,
    settingsLabel: String,
    appName: String,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily

    Column(
        modifier = modifier
            .width(BentoSizes.RAIL_WIDE.dp)
            .fillMaxHeight()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 16.dp),
    ) {
        Text(
            appName,
            style = BentoTypography.title.copy(fontFamily = appFontFamily),
            color = colors.fg,
            modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 18.dp),
        )
        HomeRailWideSearchButton(searchLabel = searchLabel, onClick = onSearch)
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { item ->
                HomeRailItemWide(
                    icon = item.icon,
                    label = item.label,
                    count = item.count,
                    selected = item.key == selectedKey,
                    onClick = { onSelect(item.key) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        HomeRailItemWide(
            icon = Icons.Rounded.Tune,
            label = settingsLabel,
            count = null,
            selected = false,
            onClick = onSettings,
        )
    }
}

@Composable
private fun HomeRailWideSearchButton(
    searchLabel: String,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.RAIL_FAB.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(colors.tonal)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Rounded.Search,
            searchLabel,
            tint = colors.onTonal,
        )
        Text(
            searchLabel,
            style = BentoTypography.rowTitle.copy(fontFamily = appFontFamily),
            color = colors.onTonal,
        )
    }
}

@Composable
private fun HomeRailItemWide(
    icon: ImageVector,
    label: String,
    count: Int?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(26.dp))
            .background(
                when {
                    selected -> colors.tonal
                    hovered -> colors.fg.copy(alpha = 0.07f)
                    else -> Color.Transparent
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(22.dp),
            tint = when {
                selected -> colors.onTonal
                else -> colors.sub
            },
        )
        Text(
            label,
            style = BentoTypography.body.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontFamily = appFontFamily,
            ),
            color = when {
                selected -> colors.onTonal
                else -> colors.fg
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        count?.let {
            Text(
                it.toString(),
                style = BentoTypography.label.copy(
                    fontWeight = FontWeight.Medium,
                    fontFamily = appFontFamily,
                ),
                color = when {
                    selected -> colors.onTonal
                    else -> colors.muted
                },
            )
        }
    }
}

@Composable
private fun HomeRailSearchFab(
    searchLabel: String,
    short: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val size = when {
        short -> 48.dp
        else -> BentoSizes.RAIL_FAB.dp
    }
    val cornerRadius by animateDpAsState(
        targetValue = when {
            hovered -> 28.dp
            else -> BentoRadii.ROW.dp
        },
        label = "home-rail-search-fab-radius",
    )

    Box(
        modifier = Modifier
            .size(size)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.tonal)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Search,
            searchLabel,
            tint = colors.onTonal,
        )
    }
}
