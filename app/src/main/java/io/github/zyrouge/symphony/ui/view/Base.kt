package io.github.zyrouge.symphony.ui.view

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.rememberGrooveVersion
import io.github.zyrouge.symphony.ui.theme.bento.bentoWallpaperHue
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.view.player.BentoPlayerHost
import io.github.zyrouge.symphony.ui.view.player.BentoPlayerHostState
import io.github.zyrouge.symphony.ui.view.player.LocalBentoCapsuleSpace
import io.github.zyrouge.symphony.ui.view.player.LocalBentoPlayerHost
import io.github.zyrouge.symphony.ui.view.player.bentoCapsuleSpace
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.zyrouge.symphony.MainActivity
import io.github.zyrouge.symphony.Symphony
import io.github.zyrouge.symphony.ui.theme.bento.BentoTheme
import io.github.zyrouge.symphony.ui.helpers.ScaleTransition
import io.github.zyrouge.symphony.ui.helpers.SlideTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.SymphonyTheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.view.settings.AppearanceSettingsView
import io.github.zyrouge.symphony.ui.view.settings.AppearanceSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.GrooveSettingsView
import io.github.zyrouge.symphony.ui.view.settings.GrooveSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.MiniPlayerSettingsView
import io.github.zyrouge.symphony.ui.view.settings.MiniPlayerSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.NowPlayingSettingsView
import io.github.zyrouge.symphony.ui.view.settings.NowPlayingSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.PlayerSettingsView
import io.github.zyrouge.symphony.ui.view.settings.PlayerSettingsViewRoute
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.serializer

@Composable
fun BaseView(symphony: Symphony, activity: MainActivity) {
    val navController = rememberNavController()
    val context = remember {
        ViewContext(
            symphony = symphony,
            activity = activity,
            navController = navController,
        )
    }

    SymphonyTheme(context) {
        val bentoThemeMode by context.symphony.settings.bentoThemeMode.flow.collectAsState()
        val bentoColorScheme by context.symphony.settings.bentoColorScheme.flow.collectAsState()
        val bentoPrimaryHue by context.symphony.settings.bentoPrimaryHue.flow.collectAsState()
        val bentoWallpaperColors by context.symphony.settings.bentoWallpaperColors.flow.collectAsState()
        // A wallpaper change updates the configuration, which re-reads the hue.
        val configuration = LocalConfiguration.current
        val wallpaperHue = remember(bentoWallpaperColors, configuration) {
            if (bentoWallpaperColors) bentoWallpaperHue(context.activity) else null
        }
        BentoTheme(
            mode = bentoThemeMode,
            scheme = bentoColorScheme,
            hue = bentoPrimaryHue,
            hueDegrees = wallpaperHue,
        ) {
            val windowClass = rememberBentoWindowClass()
            val playerHost = rememberSaveable(saver = BentoPlayerHostState.Saver) {
                BentoPlayerHostState()
            }
            val queue by context.symphony.radio.observatory.queue.collectAsState()
            val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
            val hasSong = queue.getOrNull(queueIndex) != null
            // The space follows "a song is loaded", not the capsule's visibility, so
            // pages do not jump when the player opens or the For you tile hides it.
            val capsuleSpace by animateDpAsState(
                targetValue = if (hasSong) bentoCapsuleSpace(windowClass) else 0.dp,
                animationSpec = spring(dampingRatio = 0.8f, stiffness = 380f),
                label = "capsule-space",
            )
            val grooveVersion by rememberGrooveVersion(context.symphony)
            CompositionLocalProvider(
                LocalBentoPlayerHost provides playerHost,
                LocalBentoCapsuleSpace provides capsuleSpace,
                LocalGrooveVersion provides grooveVersion,
            ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LocalBentoColors.current.bg),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = HomeViewRoute,
                ) {
                    baseComposable<HomeViewRoute> {
                        HomeView(context)
                    }
                    baseComposable<ArtistViewRoute> {
                        ArtistView(context, it.toRoute())
                    }
                    baseComposable<AlbumViewRoute> {
                        AlbumView(context, it.toRoute())
                    }
                    baseComposable<SearchViewRoute> {
                        SearchView(context, it.toRoute())
                    }
                    baseComposable<AlbumArtistViewRoute> {
                        AlbumArtistView(context, it.toRoute())
                    }
                    baseComposable<GenreViewRoute> {
                        GenreView(context, it.toRoute())
                    }
                    baseComposable<PlaylistViewRoute> {
                        PlaylistView(context, it.toRoute())
                    }
                    baseComposable<SettingsViewRoute> {
                        SettingsView(context, it.toRoute())
                    }
                    baseComposable<AppearanceSettingsViewRoute> {
                        AppearanceSettingsView(context)
                    }
                    baseComposable<GrooveSettingsViewRoute> {
                        GrooveSettingsView(context, it.toRoute())
                    }
                    baseComposable<PlayerSettingsViewRoute> {
                        PlayerSettingsView(context)
                    }
                    baseComposable<NowPlayingSettingsViewRoute> {
                        NowPlayingSettingsView(context)
                    }
                    baseComposable<MiniPlayerSettingsViewRoute> {
                        MiniPlayerSettingsView(context)
                    }
                }

                // Above the navigation graph so every page keeps the capsule and the player.
                BentoPlayerHost(
                    context = context,
                    windowClass = windowClass,
                    open = playerHost.open,
                    capsuleSuppressed = playerHost.capsuleSuppressed,
                    hasRail = playerHost.hasRail,
                    onOpenChange = { open -> playerHost.open = open },
                )
            }
            }
        }
    }
}

private inline fun <reified T : Any> NavGraphBuilder.baseComposable(
    noinline content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit),
) {
    composable<T>(
        popEnterTransition = {
            when {
                isInitialRoute<SearchViewRoute>() -> ScaleTransition.scaleUp.enterTransition()
                else -> SlideTransition.slideRight.enterTransition()
            }
        },
        popExitTransition = {
            when {
                isInitialRoute<SearchViewRoute>() -> SlideTransition.slideUp.exitTransition()
                else -> SlideTransition.slideRight.exitTransition()
            }
        },
        enterTransition = {
            when {
                isTargetRoute<SearchViewRoute>() -> SlideTransition.slideDown.enterTransition()
                else -> SlideTransition.slideLeft.enterTransition()
            }
        },
        exitTransition = {
            when {
                isTargetRoute<SearchViewRoute>() -> ScaleTransition.scaleDown.exitTransition()
                else -> SlideTransition.slideLeft.exitTransition()
            }
        },
    ) {
        content(it)
    }
}

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> NavDestination.isRoute() =
    route?.contains(serializer<T>().descriptor.serialName) == true

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> AnimatedContentTransitionScope<NavBackStackEntry>.isInitialRoute() =
    initialState.destination.isRoute<T>()

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> AnimatedContentTransitionScope<NavBackStackEntry>.isTargetRoute() =
    targetState.destination.isRoute<T>()
