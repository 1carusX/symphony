package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.components.BentoCallToActionPill
import io.github.zyrouge.symphony.ui.components.BentoGrid
import io.github.zyrouge.symphony.ui.components.BentoShortcutTile
import io.github.zyrouge.symphony.ui.components.BentoPageHeader
import io.github.zyrouge.symphony.ui.components.BentoStatContent
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTilePlacement
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.bentoStatValueStyle
import io.github.zyrouge.symphony.ui.components.bentoTilePaddingDp
import io.github.zyrouge.symphony.ui.components.settings.ConsiderContributingTile
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.rememberGrooveFolderCount
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorMode
import io.github.zyrouge.symphony.ui.theme.bento.BentoColors
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorRoles
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorMode
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.view.settings.AppearanceSettingsViewRoute
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.view.settings.HomeTabsDialog
import io.github.zyrouge.symphony.ui.view.settings.UpdatesDialog
import io.github.zyrouge.symphony.ui.view.settings.GrooveSettingsViewRoute
import io.github.zyrouge.symphony.ui.view.settings.PlayerSettingsViewRoute
import io.github.zyrouge.symphony.utils.ActivityUtils
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.roundToInt

@Serializable
data class SettingsViewRoute(val initialElement: String? = null) {
    companion object {
        const val ELEMENT_MEDIA_FOLDERS = "media_folders"
    }
}

/**
 * Settings placement tables (DESIGN.md, Appendix A, "settings"). The kit has
 * no tile for the existing Appearance page (language, font and scaling), so
 * its link splits the About slot instead of being dropped.
 */
private fun settingsPlacements(gridClass: BentoGridClass) = when (gridClass) {
    BentoGridClass.C4 -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 4),
        BentoTilePlacement("lib", 1, 5, 4, 1),
        BentoTilePlacement("home", 1, 6, 2, 1),
        BentoTilePlacement("player", 3, 6, 2, 1),
        BentoTilePlacement("folders", 1, 7, 2, 1),
        BentoTilePlacement("updates", 3, 7, 2, 1),
        BentoTilePlacement("about", 1, 8, 2, 1),
        BentoTilePlacement("appearance", 3, 8, 2, 1),
    )

    BentoGridClass.C6 -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 3),
        BentoTilePlacement("lib", 5, 1, 2, 1),
        BentoTilePlacement("home", 5, 2, 2, 1),
        BentoTilePlacement("player", 5, 3, 2, 1),
        BentoTilePlacement("folders", 1, 4, 2, 1),
        BentoTilePlacement("updates", 3, 4, 2, 1),
        BentoTilePlacement("about", 5, 4, 2, 1),
        BentoTilePlacement("appearance", 1, 5, 6, 1),
    )

    BentoGridClass.C8, BentoGridClass.SHORT -> listOf(
        BentoTilePlacement("theme", 1, 1, 4, 3),
        BentoTilePlacement("lib", 5, 1, 4, 1),
        BentoTilePlacement("home", 5, 2, 2, 1),
        BentoTilePlacement("player", 7, 2, 2, 1),
        BentoTilePlacement("folders", 5, 3, 4, 1),
        BentoTilePlacement("updates", 1, 4, 2, 1),
        BentoTilePlacement("about", 3, 4, 2, 1),
        BentoTilePlacement("appearance", 5, 4, 4, 1),
    )

    BentoGridClass.C12 -> listOf(
        BentoTilePlacement("theme", 1, 1, 5, 3),
        BentoTilePlacement("lib", 6, 1, 7, 1),
        BentoTilePlacement("home", 6, 2, 3, 1),
        BentoTilePlacement("player", 9, 2, 4, 1),
        BentoTilePlacement("folders", 6, 3, 3, 1),
        BentoTilePlacement("updates", 9, 3, 4, 1),
        BentoTilePlacement("about", 1, 4, 6, 1),
        BentoTilePlacement("appearance", 7, 4, 6, 1),
    )
}

@Composable
fun SettingsView(context: ViewContext, route: SettingsViewRoute) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val windowClass = rememberBentoWindowClass()
    val songCount by context.symphony.groove.song.count.collectAsState()
    val folderCount = rememberGrooveFolderCount(context)
    var showHomeTabsDialog by remember { mutableStateOf(false) }
    var showUpdatesDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .statusBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(context = context, windowClass = windowClass)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                ConsiderContributingTile(context)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val gridClass = rememberBentoGridClass(maxWidth.value.roundToInt())
                    BentoGrid(
                        gridClass = gridClass,
                        placements = settingsPlacements(gridClass),
                        modifier = Modifier.fillMaxWidth(),
                    ) { tileId, _, geometry ->
                        when (tileId) {
                            "theme" -> SettingsThemeTile(
                                context = context,
                                geometry = geometry,
                            )

                            "lib" -> SettingsLibraryStatTile(
                                context = context,
                                geometry = geometry,
                                appFontFamily = appFontFamily,
                                songCount = songCount,
                                folderCount = folderCount,
                            )

                            "home" -> BentoShortcutTile(
                                geometry = geometry,
                                icon = Icons.Rounded.AutoAwesome,
                                label = context.symphony.t.Home,
                                sub = "For you and 6 more sections",
                                onClick = { showHomeTabsDialog = true },
                            )

                            "player" -> BentoShortcutTile(
                                geometry = geometry,
                                icon = Icons.Rounded.Speed,
                                label = context.symphony.t.Player,
                                sub = "Speed, sleep timer",
                                onClick = {
                                    context.navController.navigate(PlayerSettingsViewRoute)
                                },
                            )

                            "folders" -> BentoShortcutTile(
                                geometry = geometry,
                                icon = Icons.Rounded.Folder,
                                label = "Library folders",
                                sub = "Choose what to scan",
                                onClick = {
                                    context.navController.navigate(
                                        GrooveSettingsViewRoute(
                                            route.initialElement
                                                ?: SettingsViewRoute.ELEMENT_MEDIA_FOLDERS,
                                        ),
                                    )
                                },
                            )

                            "updates" -> BentoShortcutTile(
                                geometry = geometry,
                                icon = Icons.Rounded.Refresh,
                                label = context.symphony.t.Updates,
                                sub = "Check for new versions",
                                onClick = { showUpdatesDialog = true },
                            )

                            "appearance" -> BentoShortcutTile(
                                geometry = geometry,
                                icon = Icons.Rounded.Palette,
                                label = context.symphony.t.Appearance,
                                sub = "Language, font and scaling",
                                onClick = {
                                    context.navController.navigate(AppearanceSettingsViewRoute)
                                },
                            )

                            "about" -> BentoTile(
                                tone = BentoTileTone.TONAL,
                                sizeClass = geometry.sizeClass,
                            ) {
                                SettingsAboutTileContent(context = context)
                            }
                        }
                    }
                }
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
                Spacer(Modifier.height(16.dp))
                SettingsLegacyLinksRow(context)
                Spacer(Modifier.bentoGestureBarPadding())
            }
        }
    }
}

/**
 * The settings header: a back button and the page title, sized per window
 * class.
 */
@Composable
private fun SettingsHeader(context: ViewContext, windowClass: BentoWindowClass) {
    BentoPageHeader(
        title = context.symphony.t.Settings,
        windowClass = windowClass,
        onBack = { context.navController.popBackStack() },
    )
}

