package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoColors
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii

/**
 * Tile tones (DESIGN.md 4.1): the fill and text colors of a tile.
 */
enum class BentoTileTone {
    /** `surface` fill, `fg` and `sub` text. */
    PLAIN,

    /** `raised` fill, `fg` and `sub` text. */
    RAISED,

    /** `accent` fill, `on-accent` and `on-accent-sub` text. */
    ACCENT,

    /** `tonal` fill, `on-tonal` and `tonal-sub` text. */
    TONAL,
}

/**
 * Text colors of the enclosing [BentoTile], so nested content matches its tone.
 * Provided by [BentoTheme] with the
 * plain tone's colors and re-provided by every tile with its own tone.
 */
@Immutable
data class BentoTileContentColors(
    val fg: Color,
    val sub: Color,
)

val LocalBentoTileContentColors = compositionLocalOf<BentoTileContentColors> {
    error("BentoTileContentColors is not provided; wrap content in BentoTheme")
}

/**
 * Text colors a tile of [tone] uses for its content. Tiles read this instead of
 * [LocalBentoTileContentColors] when they compute colors before composing their
 * own [BentoTile], so the tone stays correct at any nesting depth.
 */
fun bentoToneContentColors(tone: BentoTileTone, colors: BentoColors) = when (tone) {
    BentoTileTone.PLAIN, BentoTileTone.RAISED ->
        BentoTileContentColors(colors.fg, colors.sub)

    BentoTileTone.ACCENT -> BentoTileContentColors(colors.onAccent, colors.onAccentSub)
    BentoTileTone.TONAL -> BentoTileContentColors(colors.onTonal, colors.tonalSub)
}

fun bentoTileRadius(sizeClass: BentoTileSizeClass) = when (sizeClass) {
    BentoTileSizeClass.S, BentoTileSizeClass.XS -> BentoRadii.TILE_SMALL.dp
    else -> BentoRadii.TILE.dp
}
