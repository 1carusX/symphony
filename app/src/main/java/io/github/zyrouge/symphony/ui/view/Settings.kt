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

/**
 * The Theme tile (DESIGN.md 5.6): the theme mode, color scheme and primary
 * color pickers, wired to the persisted bento theme settings so a tap
 * re-themes the app immediately.
 */
@Composable
private fun SettingsThemeTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
) {
    val mode by context.symphony.settings.bentoThemeMode.flow.collectAsState()
    val scheme by context.symphony.settings.bentoColorScheme.flow.collectAsState()
    val hue by context.symphony.settings.bentoPrimaryHue.flow.collectAsState()
    val colorMode = LocalBentoColorMode.current

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsTileLabel(text = context.symphony.t.Theme)
            SettingsThemeModeOptions(
                context = context,
                hue = hue,
                selected = mode,
                fourColumns = geometry.width >= 520f,
                onSelect = { context.symphony.settings.bentoThemeMode.setValue(it) },
            )
            SettingsTileLabel(text = "Color scheme")
            SettingsSchemeOptions(
                context = context,
                colorMode = colorMode,
                hue = hue,
                selected = scheme,
                onSelect = { context.symphony.settings.bentoColorScheme.setValue(it) },
            )
            SettingsTileLabel(text = context.symphony.t.PrimaryColor)
            Row(
                modifier = Modifier.selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BentoPrimaryHue.entries.forEach { hueOption ->
                    SettingsHueSwatch(
                        hue = hueOption,
                        selected = hue == hueOption,
                        onSelect = { context.symphony.settings.bentoPrimaryHue.setValue(hueOption) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsTileLabel(text: String) {
    Text(
        text,
        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
        color = LocalBentoTileContentColors.current.sub,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Theme mode options (DESIGN.md 5.6): System, Light, Dark, Black; 46 dp
 * raised rows in a 2 x 2 grid, or 4 x 1 when the tile is at least 520 dp
 * wide; the selected one turns accent. A 20 dp preview swatch sits before
 * the label.
 */
@Composable
private fun SettingsThemeModeOptions(
    context: ViewContext,
    hue: BentoPrimaryHue,
    selected: BentoThemeMode,
    fourColumns: Boolean,
    onSelect: (BentoThemeMode) -> Unit,
) {
    val options = BentoThemeMode.entries
    val rows: List<List<BentoThemeMode>> = when {
        fourColumns -> listOf(options)
        else -> options.chunked(2)
    }
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    SettingsThemeModeOption(
                        context = context,
                        option = option,
                        hue = hue,
                        selected = option == selected,
                        modifier = Modifier.weight(1f),
                        onSelect = { onSelect(option) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsThemeModeOption(
    context: ViewContext,
    option: BentoThemeMode,
    hue: BentoPrimaryHue,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val fg = when {
        selected -> colors.onAccent
        else -> colors.fg
    }
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(shape)
            .background(when {
                selected -> colors.accent
                else -> colors.raised
            })
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SettingsThemeModeSwatch(mode = option, hue = hue)
        Text(
            option.label(context),
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun BentoThemeMode.label(context: ViewContext) = when (this) {
    BentoThemeMode.SYSTEM -> context.symphony.t.System
    BentoThemeMode.LIGHT -> context.symphony.t.Light
    BentoThemeMode.DARK -> context.symphony.t.Dark
    BentoThemeMode.BLACK -> context.symphony.t.Black
}

/**
 * A 20 dp mode preview circle with a 2 dp `line` inset ring; System is half
 * light, half dark.
 */
@Composable
private fun SettingsThemeModeSwatch(mode: BentoThemeMode, hue: BentoPrimaryHue) {
    val colors = LocalBentoColors.current
    val light = hslColor(hue.degrees, 0.5f, 0.96f)
    val dark = hslColor(hue.degrees, 0.12f, 0.16f)
    Canvas(modifier = Modifier.size(20.dp)) {
        val radius = size.width / 2f
        val fill = when (mode) {
            BentoThemeMode.SYSTEM -> dark
            BentoThemeMode.LIGHT -> light
            BentoThemeMode.DARK -> dark
            BentoThemeMode.BLACK -> Color.Black
        }
        drawCircle(fill, radius = radius)
        if (mode == BentoThemeMode.SYSTEM) {
            val half = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(radius, radius)
                lineTo(0f, size.height)
                close()
            }
            drawPath(half, light)
        }
        drawCircle(
            colors.line,
            radius = radius - 1.dp.toPx(),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

/**
 * Color scheme options (DESIGN.md 5.6): Tinted, Neutral, Vivid, Contrast;
 * raised columns with a small (background, surface, accent) preview for the
 * current mode; the selected one turns accent.
 */
@Composable
private fun SettingsSchemeOptions(
    context: ViewContext,
    colorMode: BentoColorMode,
    hue: BentoPrimaryHue,
    selected: BentoColorScheme,
    onSelect: (BentoColorScheme) -> Unit,
) {
    val options = BentoColorScheme.entries
    Row(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val preview = remember(option, colorMode, hue) {
                BentoColorRoles.resolve(option, colorMode, hue)
            }
            SettingsSchemeOption(
                context = context,
                option = option,
                preview = preview,
                selected = option == selected,
                modifier = Modifier.weight(1f),
                onSelect = { onSelect(option) },
            )
        }
    }
}

@Composable
private fun SettingsSchemeOption(
    context: ViewContext,
    option: BentoColorScheme,
    preview: BentoColors,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val fg = when {
        selected -> colors.onAccent
        else -> colors.fg
    }
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(when {
                selected -> colors.accent
                else -> colors.raised
            })
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SettingsSchemePreview(preview = preview)
        Text(
            option.label(context),
            style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun BentoColorScheme.label(context: ViewContext) = when (this) {
    BentoColorScheme.TINTED -> "Tinted"
    BentoColorScheme.NEUTRAL -> "Neutral"
    BentoColorScheme.VIVID -> "Vivid"
    BentoColorScheme.CONTRAST -> "Contrast"
}

/**
 * A scheme preview (DESIGN.md 5.6): the scheme's background with a surface
 * bar and an accent bar inside a `line` ring.
 */
@Composable
private fun SettingsSchemePreview(preview: BentoColors) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .height(28.dp)
            .widthIn(max = 64.dp)
            .fillMaxWidth()
            .border(1.dp, colors.line, RoundedCornerShape(8.dp))
            .background(preview.bg, RoundedCornerShape(8.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 7.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(46f)
                    .fillMaxSize()
                    .background(preview.surface, RoundedCornerShape(4.dp)),
            )
            Box(
                modifier = Modifier
                    .weight(22f)
                    .fillMaxSize()
                    .background(preview.accent, RoundedCornerShape(4.dp)),
            )
        }
    }
}

/**
 * A 36 dp primary color swatch (DESIGN.md 5.6); the selected one carries a
 * bg + fg ring and a check.
 */
@Composable
private fun SettingsHueSwatch(
    hue: BentoPrimaryHue,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val fill = hslColor(hue.degrees, 0.7f, 0.62f)
    Box(
        modifier = Modifier
            .size(36.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(36.dp)) {
            val radius = size.width / 2f
            if (selected) {
                drawCircle(colors.fg, radius = radius + 5.dp.toPx())
                drawCircle(colors.bg, radius = radius + 3.dp.toPx())
            }
            drawCircle(fill, radius = radius)
        }
        if (selected) {
            Icon(
                Icons.Rounded.Check,
                hue.name,
                tint = hslColor(hue.degrees, 0.6f, 0.14f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * The About tile: app name and version, matching the app info block of the
 * previous settings home.
 */
@Composable
private fun SettingsAboutTileContent(context: ViewContext) {
    val tileColors = LocalBentoTileContentColors.current
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(R.drawable.ic_launcher_foreground, null)
        }
        Column {
            Text(
                AppMeta.appName,
                style = BentoTypography.rowTitle,
                color = tileColors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                AppMeta.version,
                style = BentoTypography.secondary,
                color = tileColors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            AppMeta.latestVersion?.takeIf { AppMeta.version != it }?.let {
                Text(
                    context.symphony.t.NewVersionAvailableX(it),
                    style = BentoTypography.secondary,
                    color = tileColors.sub,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * The Library stat tile (DESIGN.md 5.6): song and folder counts with a
 * Rescan pill.
 */
@Composable
private fun SettingsLibraryStatTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    appFontFamily: FontFamily?,
    songCount: Int,
    folderCount: Int,
) {
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Box(modifier = Modifier.fillMaxSize()) {
            BentoStatContent(
                label = "Library",
                value = context.symphony.t.XSongs(songCount.toString()),
                valueStyle = bentoStatValueStyle(geometry.sizeClass, appFontFamily),
                sub = "in " + context.symphony.t.XFolders(folderCount.toString()),
                geometry = geometry,
            )
            BentoCallToActionPill(
                label = context.symphony.t.Rescan,
                wide = geometry.width >= 320f,
                icon = Icons.Rounded.Refresh,
                tilePadding = bentoTilePaddingDp(geometry.sizeClass),
                onClick = rescanLibrary(context),
            )
        }
    }
}

private fun rescanLibrary(context: ViewContext): () -> Unit = {
    context.symphony.radio.stop()
    context.symphony.groove.fetch(
        Groove.FetchOptions(resetInMemoryCache = true),
    )
}

/**
 * The community links of the previous settings home, kept under the grid
 * because the kit does not design them.
 */
@Composable
private fun SettingsLegacyLinksRow(context: ViewContext) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp),
    ) {
        LinkChip(
            context,
            icon = Icons.Rounded.BugReport,
            label = context.symphony.t.ReportAnIssue,
            url = AppMeta.githubIssuesUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.Github,
            url = AppMeta.githubRepositoryUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.Discord,
            url = AppMeta.discordUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.Reddit,
            url = AppMeta.redditUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.PlayStore,
            url = AppMeta.playStoreUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.FDroid,
            url = AppMeta.fdroidUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.IzzyOnDroid,
            url = AppMeta.izzyOnDroidUrl,
        )
    }
}

@Composable
private fun LinkChip(context: ViewContext, icon: ImageVector? = null, label: String, url: String) {
    Row(
        modifier = Modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(4.dp),
            )
            .clip(RoundedCornerShape(4.dp))
            .clickable {
                ActivityUtils.startBrowserActivity(context.activity, url.toUri())
            }
            .padding(6.dp, 4.dp),
    ) {
        icon?.let {
            Icon(
                it,
                null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Resolves an HSL color (the design kit's color notation) to a Compose
 * color; hue in degrees, saturation and lightness in 0..1.
 */
private fun hslColor(hue: Float, saturation: Float, lightness: Float): Color {
    val c = (1f - abs(2f * lightness - 1f)) * saturation
    val hp = ((hue % 360f) + 360f) % 360f / 60f
    val x = c * (1f - abs(hp % 2f - 1f))
    val (r, g, b) = when {
        hp < 1f -> Triple(c, x, 0f)
        hp < 2f -> Triple(x, c, 0f)
        hp < 3f -> Triple(0f, c, x)
        hp < 4f -> Triple(0f, x, c)
        hp < 5f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = lightness - c / 2f
    return Color(red = r + m, green = g + m, blue = b + m)
}
