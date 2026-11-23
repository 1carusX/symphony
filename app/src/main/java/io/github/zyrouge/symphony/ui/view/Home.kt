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
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Folder
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import io.github.zyrouge.symphony.ui.view.player.LocalBentoPlayerHost
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
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.view.home.AlbumArtistsView
import io.github.zyrouge.symphony.ui.view.home.AlbumsView
import io.github.zyrouge.symphony.ui.view.home.ArtistsView
import io.github.zyrouge.symphony.ui.view.home.FoldersView
import io.github.zyrouge.symphony.ui.view.home.ForYouView
import io.github.zyrouge.symphony.ui.view.home.GenresView
import io.github.zyrouge.symphony.ui.view.home.PlaylistsView
import io.github.zyrouge.symphony.ui.view.home.SongsView
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/**
 * Home page sections. The order follows the bento design spec (DESIGN.md
 * §2.2: For you, Songs, Albums, Artists, Genres, Playlists, Folders); the
 * section the design kit does not cover (AlbumArtists) is appended and keeps
 * its previous icon. The former Browser and Tree sections live in Folders.
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
    val playerHost = LocalBentoPlayerHost.current
    var forYouNowPlayingVisible by remember { mutableStateOf(false) }
    val openPlayer: () -> Unit = {
        if (context.symphony.radio.queue.currentSongId != null) {
            playerHost.open = true
        }
    }
    // BaseView hosts the player; Home reports what changes the capsule and resets it on leave.
    SideEffect {
        playerHost.capsuleSuppressed = currentTab == HomePage.ForYou && forYouNowPlayingVisible
        playerHost.hasRail = navigation != HomeNavigation.TABS
    }
    DisposableEffect(Unit) {
        onDispose {
            playerHost.capsuleSuppressed = false
            playerHost.hasRail = false
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
                            .fillMaxHeight(),
                    )
            }
        }

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
            HomePage.Folders -> FoldersView(context)
            HomePage.Playlists -> PlaylistsView(context)
        }
    }
}
