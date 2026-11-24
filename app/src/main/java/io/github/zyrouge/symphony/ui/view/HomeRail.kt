package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.rememberGrooveFolderCount
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/**
 * Navigation rail with labels (DESIGN.md §2.1, medium to large windows):
 * 96 dp wide, a 56 dp tonal Search button on top, one icon-over-label item
 * per section and Settings pinned to the bottom.
 */
@Composable
internal fun HomeRailColumnLabeled(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(BentoSizes.RAIL_WIDTH.dp)
            .fillMaxHeight()
            .navigationBarsPadding()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HomeRailSearchFab(context, currentTab)
        Spacer(Modifier.height(14.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            sections.forEach { section ->
                HomeRailItemLabeled(
                    icon = section.icon,
                    label = section.label(context),
                    selected = section == currentTab,
                    onClick = { onSelectSection(section) },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        HomeRailItemLabeled(
            icon = Icons.Rounded.Tune,
            label = context.symphony.t.Settings,
            selected = false,
            onClick = { context.navController.navigate(SettingsViewRoute()) },
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
 * Icon-only navigation rail for short-height windows (DESIGN.md §2.1):
 * 80 dp wide, a 48 dp Search button, 52 × 36 icon pills with a long-press
 * tooltip, Settings pinned to the bottom.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeRailColumnShort(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    var tooltipLabel by remember { mutableStateOf<String?>(null) }
    var tooltipCenterYInRoot by remember { mutableFloatStateOf(0f) }
    var railYInRoot by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
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
            HomeRailSearchFab(context, currentTab, short = true)
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                sections.forEach { section ->
                    HomeRailItemShort(
                        icon = section.icon,
                        label = section.label(context),
                        selected = section == currentTab,
                        onClick = { onSelectSection(section) },
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
                label = context.symphony.t.Settings,
                selected = false,
                onClick = { context.navController.navigate(SettingsViewRoute()) },
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
 * Wide navigation rail for extra-large windows (DESIGN.md §2.2): 240 dp
 * wide, the app name, a Search button with label, 52 dp rows with a count
 * on the right and Settings pinned to the bottom.
 */
@Composable
internal fun HomeRailColumnWide(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily

    Column(
        modifier = Modifier
            .width(BentoSizes.RAIL_WIDE.dp)
            .fillMaxHeight()
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, top = 20.dp, bottom = 16.dp),
    ) {
        Text(
            "Symphony",
            style = BentoTypography.title.copy(fontFamily = appFontFamily),
            color = colors.fg,
            modifier = Modifier.padding(start = 16.dp, top = 6.dp, bottom = 18.dp),
        )
        HomeRailWideSearchButton(context, currentTab)
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            sections.forEach { section ->
                HomeRailItemWide(
                    icon = section.icon,
                    label = section.label(context),
                    count = rememberSectionCount(context, section),
                    selected = section == currentTab,
                    onClick = { onSelectSection(section) },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        HomeRailItemWide(
            icon = Icons.Rounded.Tune,
            label = context.symphony.t.Settings,
            count = null,
            selected = false,
            onClick = { context.navController.navigate(SettingsViewRoute()) },
        )
    }
}

@Composable
private fun HomeRailWideSearchButton(context: ViewContext, currentTab: HomePage) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.RAIL_FAB.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(colors.tonal)
            .clickable {
                context.navController.navigate(SearchViewRoute(currentTab.kind?.name))
            }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Rounded.Search,
            context.symphony.t.SearchYourMusic,
            tint = colors.onTonal,
        )
        Text(
            context.symphony.t.SearchYourMusic,
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
    context: ViewContext,
    currentTab: HomePage,
    short: Boolean = false,
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
            ) {
                context.navController.navigate(SearchViewRoute(currentTab.kind?.name))
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Search,
            context.symphony.t.SearchYourMusic,
            tint = colors.onTonal,
        )
    }
}

/**
 * Section count for the wide rail rows. The design kit covers Songs,
 * Albums, Artists, Genres, Playlists and Folders; ForYou has no count.
 */
@Composable
private fun rememberSectionCount(context: ViewContext, section: HomePage): Int? {
    return when (section) {
        HomePage.Songs -> context.symphony.groove.song.count.collectAsState().value
        HomePage.Albums -> context.symphony.groove.album.count.collectAsState().value
        HomePage.Artists -> context.symphony.groove.artist.count.collectAsState().value
        HomePage.AlbumArtists -> context.symphony.groove.albumArtist.count.collectAsState().value
        HomePage.Genres -> context.symphony.groove.genre.count.collectAsState().value
        HomePage.Playlists -> context.symphony.groove.playlist.count.collectAsState().value
        HomePage.Folders -> rememberGrooveFolderCount(context)
        else -> null
    }
}
