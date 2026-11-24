package io.github.zyrouge.symphony.ui.view

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.BentoAdaptiveRow
import io.github.zyrouge.symphony.ui.components.BentoSwitch
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.bentoTooNarrowForText
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorMode
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorRoles
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoColors
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorMode
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.bentoWallpaperHue
import kotlin.math.abs

/**
 * The Theme tile (DESIGN.md 5.6): the theme mode, color scheme and primary
 * color pickers, wired to the persisted bento theme settings so a tap
 * re-themes the app immediately.
 */
@Composable
internal fun SettingsThemeTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
) {
    val mode by context.symphony.settings.bentoThemeMode.flow.collectAsState()
    val scheme by context.symphony.settings.bentoColorScheme.flow.collectAsState()
    val hue by context.symphony.settings.bentoPrimaryHue.flow.collectAsState()
    val wallpaperColors by context.symphony.settings.bentoWallpaperColors.flow.collectAsState()
    val configuration = LocalConfiguration.current
    val wallpaperHue = remember(wallpaperColors, configuration) {
        if (wallpaperColors) bentoWallpaperHue(context.activity) else null
    }
    val colorMode = LocalBentoColorMode.current
    // A large font or content scale needs fewer options a row (the page also makes this tile taller).
    val crowded = bentoTooNarrowForText(geometry.width, LocalDensity.current.fontScale)

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SettingsTileLabel(text = context.symphony.t.Theme)
            SettingsThemeModeOptions(
                context = context,
                hueDegrees = wallpaperHue ?: hue.degrees,
                selected = mode,
                columns = when {
                    geometry.width >= 520f -> 4
                    crowded -> 1
                    else -> 2
                },
                onSelect = { context.symphony.settings.bentoThemeMode.setValue(it) },
            )
            SettingsTileLabel(text = context.symphony.t.ColorScheme)
            SettingsSchemeOptions(
                context = context,
                colorMode = colorMode,
                hue = hue,
                wallpaperHue = wallpaperHue,
                selected = scheme,
                perRow = if (crowded) 2 else 4,
                onSelect = { context.symphony.settings.bentoColorScheme.setValue(it) },
            )
            // The Wallpaper colors switch moves under the label when it crowds it.
            BentoAdaptiveRow(
                modifier = Modifier.fillMaxWidth(),
                gap = 8.dp,
                leading = null,
                trailing = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    {
                        SettingsWallpaperColorsSwitch(
                            checked = wallpaperColors,
                            onCheckedChange = {
                                context.symphony.settings.bentoWallpaperColors.setValue(it)
                            },
                        )
                    }
                } else {
                    null
                },
            ) {
                SettingsTileLabel(text = context.symphony.t.PrimaryColor)
            }
            FlowRow(
                modifier = Modifier
                    .selectableGroup()
                    // Wallpaper colors override the swatches; picking one turns them off.
                    .alpha(if (wallpaperHue != null) 0.4f else 1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BentoPrimaryHue.entries.forEach { hueOption ->
                    SettingsHueSwatch(
                        hue = hueOption,
                        selected = wallpaperHue == null && hue == hueOption,
                        onSelect = {
                            context.symphony.settings.bentoWallpaperColors.setValue(false)
                            context.symphony.settings.bentoPrimaryHue.setValue(hueOption)
                        },
                    )
                }
            }
        }
    }
}

/**
 * "Wallpaper colors" (prototype v16 `materialYou`, Android 12+): a compact
 * label and switch on the Primary color line, so the tile keeps its height.
 */
@Composable
private fun SettingsWallpaperColorsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            LocalStrings.current.WallpaperColors,
            style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
            color = LocalBentoTileContentColors.current.sub,
            maxLines = 1,
        )
        BentoSwitch(checked = checked, onCheckedChange = onCheckedChange)
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
    hueDegrees: Float,
    selected: BentoThemeMode,
    columns: Int,
    onSelect: (BentoThemeMode) -> Unit,
) {
    val rows = BentoThemeMode.entries.chunked(columns)
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
                        hueDegrees = hueDegrees,
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
    hueDegrees: Float,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val contrast = LocalBentoColorScheme.current == BentoColorScheme.CONTRAST
    val fg = when {
        selected -> colors.onAccent
        else -> colors.fg
    }
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(shape)
            .then(if (contrast) Modifier.border(1.dp, colors.line, shape) else Modifier)
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
        SettingsThemeModeSwatch(mode = option, hueDegrees = hueDegrees)
        Text(
            option.label(context),
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A 20 dp mode preview circle with a 2 dp `line` inset ring; System is half
 * light, half dark.
 */
@Composable
private fun SettingsThemeModeSwatch(mode: BentoThemeMode, hueDegrees: Float) {
    val colors = LocalBentoColors.current
    val light = hslColor(hueDegrees, 0.5f, 0.96f)
    val dark = hslColor(hueDegrees, 0.12f, 0.16f)
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
    wallpaperHue: Float?,
    selected: BentoColorScheme,
    perRow: Int,
    onSelect: (BentoColorScheme) -> Unit,
) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BentoColorScheme.entries.chunked(perRow).forEach { rowOptions ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowOptions.forEach { option ->
                    val preview = remember(option, colorMode, hue, wallpaperHue) {
                        when (wallpaperHue) {
                            null -> BentoColorRoles.resolve(option, colorMode, hue)
                            else -> BentoColorRoles.resolve(option, colorMode, wallpaperHue)
                        }
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
    val contrast = LocalBentoColorScheme.current == BentoColorScheme.CONTRAST
    val fg = when {
        selected -> colors.onAccent
        else -> colors.fg
    }
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .then(if (contrast) Modifier.border(1.dp, colors.line, shape) else Modifier)
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

private fun BentoThemeMode.label(context: ViewContext) = when (this) {
    BentoThemeMode.SYSTEM -> context.symphony.t.System
    BentoThemeMode.LIGHT -> context.symphony.t.Light
    BentoThemeMode.DARK -> context.symphony.t.Dark
    BentoThemeMode.BLACK -> context.symphony.t.Black
}

private fun BentoColorScheme.label(context: ViewContext) = when (this) {
    BentoColorScheme.TINTED -> context.symphony.t.Tinted
    BentoColorScheme.NEUTRAL -> context.symphony.t.Neutral
    BentoColorScheme.VIVID -> context.symphony.t.Vivid
    BentoColorScheme.CONTRAST -> context.symphony.t.Contrast
}
