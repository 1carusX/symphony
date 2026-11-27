package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.components.SettingsRootPage
import io.github.zyrouge.symphony.ui.components.SettingsShortcut
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.settings.ConsiderContributingTile
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.rememberGrooveFolderCount
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.view.settings.AppearanceSettingsDialog
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.view.settings.HomeTabsDialog
import io.github.zyrouge.symphony.ui.view.settings.MiniPlayerSettingsDialog
import io.github.zyrouge.symphony.ui.view.settings.NowPlayingSettingsDialog
import io.github.zyrouge.symphony.ui.view.settings.UpdatesDialog
import io.github.zyrouge.symphony.ui.view.settings.GrooveSettingsDialog
import io.github.zyrouge.symphony.ui.view.settings.PlayerSettingsDialog
import kotlinx.serialization.Serializable

@Serializable
data class SettingsViewRoute(val initialElement: String? = null) {
    companion object {
        const val ELEMENT_MEDIA_FOLDERS = "media_folders"
    }
}

/** Settings that open in a sheet over the settings page instead of a page of their own. */
private enum class SettingsDialog {
    APPEARANCE,
    LIBRARY,
    PLAYER,
    NOW_PLAYING,
    MINI_PLAYER,
}

@Composable
fun SettingsView(context: ViewContext, route: SettingsViewRoute) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val windowClass = rememberBentoWindowClass()
    val songCount by context.symphony.groove.song.count.collectAsState()
    val folderCount = rememberGrooveFolderCount(context)
    val homeTabs by context.symphony.settings.homeTabs.flow.collectAsState()
    val checkForUpdates by context.symphony.settings.checkForUpdates.flow.collectAsState()
    var showHomeTabsDialog by remember { mutableStateOf(false) }
    // "Add media folders" links arrive with the media folders element and open the Library sheet.
    var openDialog by remember {
        mutableStateOf(
            when (route.initialElement) {
                SettingsViewRoute.ELEMENT_MEDIA_FOLDERS -> SettingsDialog.LIBRARY
                else -> null
            },
        )
    }
    var showUpdatesDialog by remember { mutableStateOf(false) }
    val t = context.symphony.t

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding(),
    ) {
        SettingsRootPage(
            windowClass = windowClass,
            strings = t,
            shortcuts = listOf(
                SettingsShortcut(
                    key = "home",
                    icon = Icons.Rounded.AutoAwesome,
                    label = t.Home,
                    summary = t.XSectionsShown(homeTabs.size.toString()),
                    onClick = { showHomeTabsDialog = true },
                ),
                SettingsShortcut(
                    key = "player",
                    icon = Icons.Rounded.Headphones,
                    label = t.Player,
                    summary = t.PlayerSettingsSummary,
                    onClick = { openDialog = SettingsDialog.PLAYER },
                ),
                SettingsShortcut(
                    key = "nowp",
                    icon = Icons.Rounded.Lyrics,
                    label = t.NowPlaying,
                    summary = t.NowPlayingSettingsSummary,
                    onClick = { openDialog = SettingsDialog.NOW_PLAYING },
                ),
                SettingsShortcut(
                    key = "mini",
                    icon = Icons.Rounded.PlayCircle,
                    label = t.MiniPlayer,
                    summary = t.MiniPlayerSettingsSummary,
                    onClick = { openDialog = SettingsDialog.MINI_PLAYER },
                ),
                SettingsShortcut(
                    key = "folders",
                    icon = Icons.Rounded.Folder,
                    label = t.LibraryFolders,
                    summary = t.XFolders(folderCount.toString()),
                    onClick = { openDialog = SettingsDialog.LIBRARY },
                ),
                SettingsShortcut(
                    key = "updates",
                    icon = Icons.Rounded.Refresh,
                    label = t.Updates,
                    summary = if (checkForUpdates) t.ChecksAtLaunch else t.Off,
                    onClick = { showUpdatesDialog = true },
                ),
                SettingsShortcut(
                    key = "appearance",
                    icon = Icons.Rounded.Palette,
                    label = t.Appearance,
                    summary = t.AppearanceSettingsSummary,
                    onClick = { openDialog = SettingsDialog.APPEARANCE },
                ),
            ),
            onBack = { context.navController.popBackStack() },
            themeTile = { geometry -> SettingsThemeTile(context = context, geometry = geometry) },
            libraryTile = { geometry ->
                SettingsLibraryStatTile(
                    context = context,
                    geometry = geometry,
                    appFontFamily = appFontFamily,
                    songCount = songCount,
                    folderCount = folderCount,
                )
            },
            aboutTile = { geometry ->
                BentoTile(
                    tone = BentoTileTone.PLAIN,
                    sizeClass = geometry.sizeClass,
                ) {
                    SettingsAboutTileContent(context = context)
                }
            },
            contributing = { ConsiderContributingTile(context) },
            links = {
                SettingsLegacyLinksRow(context)
                Spacer(Modifier.bentoGestureBarPadding())
            },
        )
        if (showUpdatesDialog) {
            UpdatesDialog(
                context = context,
                onDismiss = { showUpdatesDialog = false },
            )
        }
        if (showHomeTabsDialog) {
            HomeTabsDialog(
                context = context,
                onDismiss = { showHomeTabsDialog = false },
            )
        }
        when (openDialog) {
            SettingsDialog.APPEARANCE -> AppearanceSettingsDialog(
                context = context,
                onDismissRequest = { openDialog = null },
            )

            SettingsDialog.LIBRARY -> GrooveSettingsDialog(
                context = context,
                onDismissRequest = { openDialog = null },
            )

            SettingsDialog.PLAYER -> PlayerSettingsDialog(
                context = context,
                onDismissRequest = { openDialog = null },
            )

            SettingsDialog.NOW_PLAYING -> NowPlayingSettingsDialog(
                context = context,
                onDismissRequest = { openDialog = null },
            )

            SettingsDialog.MINI_PLAYER -> MiniPlayerSettingsDialog(
                context = context,
                onDismissRequest = { openDialog = null },
            )

            null -> Unit
        }
    }
}
