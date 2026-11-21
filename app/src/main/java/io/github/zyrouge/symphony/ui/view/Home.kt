package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.SupervisorAccount
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
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
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.ui.components.IntroductoryDialog
import io.github.zyrouge.symphony.ui.helpers.ScaleTransition
import io.github.zyrouge.symphony.ui.helpers.SlideTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.rememberGrooveFolderCount
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.view.home.AlbumArtistsView
import io.github.zyrouge.symphony.ui.view.home.AlbumsView
import io.github.zyrouge.symphony.ui.view.home.ArtistsView
import io.github.zyrouge.symphony.ui.view.home.BrowserView
import io.github.zyrouge.symphony.ui.view.home.FoldersView
import io.github.zyrouge.symphony.ui.view.home.ForYouView
import io.github.zyrouge.symphony.ui.view.home.GenresView
import io.github.zyrouge.symphony.ui.view.home.PlaylistsView
import io.github.zyrouge.symphony.ui.view.home.SongsView
import io.github.zyrouge.symphony.ui.view.home.TreeView
import io.github.zyrouge.symphony.ui.view.player.BentoPlayerHost
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/**
 * Home page sections. The order follows the bento design spec (DESIGN.md
 * §2.2: For you, Songs, Albums, Artists, Genres, Playlists, Folders); the
 * sections the design kit does not cover (AlbumArtists, Browser, Tree) are
 * appended and keep their previous icons.
 */
enum class HomePage(
    val kind: Groove.Kind? = null,
    val label: (context: ViewContext) -> String,
    val icon: ImageVector,
) {
    ForYou(
        label = { it.symphony.t.ForYou },
        icon = Icons.Rounded.AutoAwesome,
    ),
    Songs(
        kind = Groove.Kind.SONG,
        label = { it.symphony.t.Songs },
        icon = Icons.Rounded.MusicNote,
    ),
    Albums(
        kind = Groove.Kind.ALBUM,
        label = { it.symphony.t.Albums },
        icon = Icons.Rounded.Album,
    ),
    Artists(
        kind = Groove.Kind.ARTIST,
        label = { it.symphony.t.Artists },
        icon = Icons.Rounded.Person,
    ),
    Genres(
        kind = Groove.Kind.GENRE,
        label = { it.symphony.t.Genres },
        icon = Icons.Rounded.Sell,
    ),
    Playlists(
        kind = Groove.Kind.PLAYLIST,
        label = { it.symphony.t.Playlists },
        icon = Icons.AutoMirrored.Rounded.QueueMusic,
    ),
    Folders(
        label = { it.symphony.t.Folders },
        icon = Icons.Rounded.Folder,
    ),
    AlbumArtists(
        kind = Groove.Kind.ALBUM_ARTIST,
        label = { it.symphony.t.AlbumArtists },
        icon = Icons.Rounded.SupervisorAccount,
    ),
    Browser(
        label = { it.symphony.t.Browser },
        icon = Icons.Rounded.FolderOpen,
    ),
    Tree(
        label = { it.symphony.t.Tree },
        icon = Icons.Rounded.AccountTree,
    ),
}

@Serializable
object HomeViewRoute

/**
 * Navigation form for a window class (DESIGN.md §2.1): pill tabs on compact
 * windows, a 96 dp labeled rail on medium to large, a 240 dp wide rail on
 * extra-large and an 80 dp icon-only rail on short-height windows.
 */
private enum class HomeNavigation {
    TABS,
    RAIL,
    RAIL_WIDE,
    RAIL_SHORT,
}

private fun BentoWindowClass.toHomeNavigation() = when (this) {
    BentoWindowClass.COMPACT -> HomeNavigation.TABS
    BentoWindowClass.MEDIUM, BentoWindowClass.EXPANDED, BentoWindowClass.LARGE -> HomeNavigation.RAIL
    BentoWindowClass.EXTRA_LARGE -> HomeNavigation.RAIL_WIDE
    BentoWindowClass.SHORT_HEIGHT -> HomeNavigation.RAIL_SHORT
}

@Composable
fun HomeView(context: ViewContext) {
    val windowClass = rememberBentoWindowClass()
    val navigation = windowClass.toHomeNavigation()
    val colors = LocalBentoColors.current
    val readIntroductoryMessage by context.symphony.settings.readIntroductoryMessage.flow.collectAsState()
    val tabs by context.symphony.settings.homeTabs.flow.collectAsState()
    val currentTab by context.symphony.settings.lastHomeTab.flow.collectAsState()
    val sections = remember(tabs) {
        HomePage.entries.filter { it in tabs }
    }
    val onSelectSection: (HomePage) -> Unit = { section ->
        if (section != currentTab) {
            context.symphony.settings.lastHomeTab.setValue(section)
        }
    }
    var playerOpen by remember { mutableStateOf(false) }
    var forYouNowPlayingVisible by remember { mutableStateOf(false) }
    var capsuleSpaceVisible by remember { mutableStateOf(false) }
    val openPlayer: () -> Unit = {
        if (context.symphony.radio.queue.currentSongId != null) {
            playerOpen = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding(),
    ) {
        when (navigation) {
            HomeNavigation.TABS -> Column(Modifier.fillMaxSize()) {
                HomeHeader(
                    context = context,
                    navigation = navigation,
                    currentTab = currentTab,
                )
                HomeTabsRow(
                    context = context,
                    sections = sections,
                    currentTab = currentTab,
                    onSelectSection = onSelectSection,
                )
                HomeContent(
                    context = context,
                    currentTab = currentTab,
                    onOpenPlayer = openPlayer,
                    onNowPlayingTileVisibilityChanged = { visible ->
                        forYouNowPlayingVisible = visible
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            HomeNavigation.RAIL, HomeNavigation.RAIL_SHORT, HomeNavigation.RAIL_WIDE ->
                Row(Modifier.fillMaxSize()) {
                    when (navigation) {
                        HomeNavigation.RAIL -> HomeRailColumnLabeled(
                            context = context,
                            sections = sections,
                            currentTab = currentTab,
                            onSelectSection = onSelectSection,
                        )

                        HomeNavigation.RAIL_SHORT -> HomeRailColumnShort(
                            context = context,
                            sections = sections,
                            currentTab = currentTab,
                            onSelectSection = onSelectSection,
                        )

                        HomeNavigation.RAIL_WIDE -> HomeRailColumnWide(
                            context = context,
                            sections = sections,
                            currentTab = currentTab,
                            onSelectSection = onSelectSection,
                        )

                        HomeNavigation.TABS -> {}
                    }
                    val capsuleInset by androidx.compose.animation.core.animateDpAsState(
                        targetValue = if (capsuleSpaceVisible) {
                            (BentoSizes.CAPSULE_HEIGHT + 12).dp
                        } else {
                            0.dp
                        },
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 0.8f,
                            stiffness = 380f,
                        ),
                        label = "capsule-space",
                    )
                    HomeMainColumn(
                        context = context,
                        navigation = navigation,
                        currentTab = currentTab,
                        onOpenPlayer = openPlayer,
                        onNowPlayingTileVisibilityChanged = { visible ->
                            forYouNowPlayingVisible = visible
                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .bentoGestureBarPadding()
                            .padding(bottom = capsuleInset.coerceAtLeast(0.dp))
,
                    )
            }
        }

        BentoPlayerHost(
            context = context,
            windowClass = windowClass,
            open = playerOpen,
            capsuleSuppressed = currentTab == HomePage.ForYou && forYouNowPlayingVisible,
            onCapsuleVisibleChange = { capsuleSpaceVisible = it },
            onOpenChange = { open -> playerOpen = open },
        )
    }

    if (!readIntroductoryMessage) {
        IntroductoryDialog(
            context,
            onDismissRequest = {
                context.symphony.settings.readIntroductoryMessage.setValue(true)
            },
        )
    }
}

@Composable
private fun HomeMainColumn(
    context: ViewContext,
    navigation: HomeNavigation,
    currentTab: HomePage,
    onOpenPlayer: () -> Unit,
    onNowPlayingTileVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val contentAtEnd = remember { mutableStateOf(false) }
    val contentAtTop = remember { mutableStateOf(false) }
    CompositionLocalProvider(
        LocalHomeContentAtEnd provides contentAtEnd,
        LocalHomeContentAtTop provides contentAtTop,
    ) {
        Column(modifier = modifier) {
            HomeHeader(
                context = context,
                navigation = navigation,
                currentTab = currentTab,
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                HomeContent(
                    context = context,
                    currentTab = currentTab,
                    onOpenPlayer = onOpenPlayer,
                    onNowPlayingTileVisibilityChanged = onNowPlayingTileVisibilityChanged,
                    modifier = Modifier.fillMaxSize(),
                )
                val fadeAlpha by animateFloatAsState(
                    targetValue = if (contentAtEnd.value) 0f else 1f,
                    animationSpec = tween(180),
                    label = "home-content-fade",
                )
                val topFadeAlpha by animateFloatAsState(
                    targetValue = if (contentAtTop.value) 0f else 1f,
                    animationSpec = tween(180),
                    label = "home-content-top-fade",
                )
                if (topFadeAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .fillMaxWidth()
                            .height(24.dp)
                            .graphicsLayer { alpha = topFadeAlpha }
                            .background(
                                Brush.verticalGradient(
                                    listOf(colors.bg, colors.bg.copy(alpha = 0f)),
                                ),
                            ),
                    )
                }
                if (fadeAlpha > 0.01f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(28.dp)
                            .graphicsLayer { alpha = fadeAlpha }
                            .background(
                                Brush.verticalGradient(
                                    listOf(colors.bg.copy(alpha = 0f), colors.bg),
                                ),
                            ),
                    )
                }
            }
        }
    }
}

/** Set by a list whose last item is fully visible so the bottom fade can step aside. */
val LocalHomeContentAtEnd = staticCompositionLocalOf { mutableStateOf(false) }

/** Set by a list still parked at its top so the top fade can step aside. */
val LocalHomeContentAtTop = staticCompositionLocalOf { mutableStateOf(false) }

/**
 * Page header (DESIGN.md §2.2): the page title on the left (28 sp on
 * phones, 34 sp on rail layouts, 22 sp on short windows), Search and
 * Settings icon buttons on phones.
 */
@Composable
private fun HomeHeader(
    context: ViewContext,
    navigation: HomeNavigation,
    currentTab: HomePage,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val (titleStyle, paddings, minHeight) = when (navigation) {
        HomeNavigation.TABS -> Triple(
            BentoTypography.pageTitleCompact.copy(fontFamily = appFontFamily),
            PaddingValues(
                start = BentoSpacing.GUTTER_COMPACT.dp,
                end = 8.dp,
                top = 14.dp,
                bottom = 6.dp,
            ),
            BentoSizes.HEADER_MIN_HEIGHT_PHONE.dp,
        )

        HomeNavigation.RAIL_SHORT -> Triple(
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
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .padding(paddings),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Crossfade(
            label = "home-title",
            targetState = currentTab,
            modifier = Modifier.weight(1f),
        ) { tab ->
            Text(
                tab.label(context),
                style = titleStyle,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (navigation == HomeNavigation.TABS) {
                IconButton(
                    content = {
                        Icon(
                            Icons.Rounded.Search,
                            context.symphony.t.SearchYourMusic,
                            tint = colors.fg,
                        )
                    },
                    onClick = {
                        context.navController.navigate(SearchViewRoute(currentTab.kind?.name))
                    },
                )
                IconButton(
                    content = {
                        Icon(
                            Icons.Rounded.Tune,
                            context.symphony.t.Settings,
                            tint = colors.fg,
                        )
                    },
                    onClick = {
                        context.navController.navigate(SettingsViewRoute())
                    },
                )
            }
        }
    }
}

/**
 * Phone section tabs (DESIGN.md §2.2): 40 dp pills, radius 20, 8 dp apart,
 * horizontally scrolling; the selected pill uses accent / on-accent and
 * scrolls into view.
 */
@Composable
private fun HomeTabsRow(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val tabStyle = BentoTypography.body.copy(
        fontWeight = FontWeight.SemiBold,
        fontFamily = appFontFamily,
    )
    val scrollState = rememberScrollState()
    var viewportWidth by remember { mutableFloatStateOf(0f) }
    var tabCenters by remember { mutableStateOf(emptyMap<HomePage, Float>()) }
    var initialScroll by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
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
            sections.forEach { section ->
                val selected = section == currentTab
                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            val center = it.positionInParent().x + it.size.width / 2f
                            tabCenters = tabCenters + (section to center)
                        }
                        .height(BentoSizes.TAB.dp)
                        .clip(RoundedCornerShape((BentoSizes.TAB / 2).dp))
                        .background(
                            when {
                                selected -> colors.accent
                                else -> colors.surface
                            }
                        )
                        .clickable { onSelectSection(section) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        section.label(context),
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

    LaunchedEffect(currentTab, tabCenters) {
        val center = tabCenters[currentTab] ?: return@LaunchedEffect
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
 * The main column content; leaves the pages as they are until B3/B4.
 */
@Composable
private fun HomeContent(
    context: ViewContext,
    currentTab: HomePage,
    onOpenPlayer: () -> Unit,
    onNowPlayingTileVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        label = "home-content",
        targetState = currentTab,
        modifier = modifier,
        transitionSpec = {
            SlideTransition.slideUp.enterTransition()
                .togetherWith(ScaleTransition.scaleDown.exitTransition())
        },
    ) { page ->
        when (page) {
            HomePage.ForYou -> ForYouView(
                context = context,
                onOpenPlayer = onOpenPlayer,
                onNowPlayingTileVisibilityChanged = onNowPlayingTileVisibilityChanged,
            )
            HomePage.Songs -> SongsView(context)
            HomePage.Albums -> AlbumsView(context)
            HomePage.Artists -> ArtistsView(context)
            HomePage.AlbumArtists -> AlbumArtistsView(context)
            HomePage.Genres -> GenresView(context)
            HomePage.Browser -> BrowserView(context)
            HomePage.Folders -> FoldersView(context)
            HomePage.Playlists -> PlaylistsView(context)
            HomePage.Tree -> TreeView(context)
        }
    }
}

/**
 * Navigation rail with labels (DESIGN.md §2.1, medium to large windows):
 * 96 dp wide, a 56 dp tonal Search button on top, one icon-over-label item
 * per section and Settings pinned to the bottom.
 */
@Composable
private fun HomeRailColumnLabeled(
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
private fun HomeRailColumnShort(
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
private fun HomeRailColumnWide(
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
 * Albums, Artists, Genres, Playlists and Folders; ForYou and the legacy
 * Browser/Tree sections have no count.
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
