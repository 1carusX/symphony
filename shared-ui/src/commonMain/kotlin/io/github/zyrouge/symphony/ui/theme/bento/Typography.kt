package io.github.zyrouge.symphony.ui.theme.bento

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Type styles (tokens.json typography).
 *
 * Font: Inter with the system UI font as fallback (applied where the styles
 * are consumed). Base line height is 1.35 unless a style records its own.
 * Negative tracking applies only at 18 sp and up. Durations, times, counts
 * and badges use tabular figures.
 */
object BentoTypography {
    /** Page title in the header on rail layouts. */
    val pageTitle = TextStyle(
        fontSize = 34.sp,
        lineHeight = (34 * 1.08).sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.02).em,
    )

    /** Page title on phones (top tabs). */
    val pageTitleCompact = TextStyle(
        fontSize = 28.sp,
        lineHeight = (28 * 1.08).sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.02).em,
    )

    /** Page title on short windows and sub-pages (Album). */
    val pageTitleShort = TextStyle(
        fontSize = 22.sp,
        lineHeight = (22 * 1.35).sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.01).em,
    )

    /** Stat value in large tiles. */
    val statLarge = TextStyle(
        fontSize = 40.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.02).em,
    )

    /** Stat value, album title, Now playing tile title over artwork. */
    val display = TextStyle(
        fontSize = 28.sp,
        lineHeight = (28 * 1.35).sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.02).em,
    )

    /** Player song title (medium level), Now playing tile title, large tile labels, lyrics lines. */
    val title = TextStyle(
        fontSize = 22.sp,
        lineHeight = (22 * 1.35).sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.01).em,
    )

    /** Grouped tile title (album, artist, genre, playlist, folder); player song title at the small level. */
    val groupTitle = TextStyle(
        fontSize = 18.sp,
        lineHeight = (18 * 1.35).sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.01).em,
    )

    /** Song row title, tile label, capsule title, menu header. Group rows use 15/500. */
    val rowTitle = TextStyle(
        fontSize = 15.sp,
        lineHeight = (15 * 1.35).sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** Artist line in the player and Now playing tile. Buttons, tabs and chips use 14/600. */
    val body = TextStyle(
        fontSize = 14.sp,
        lineHeight = (14 * 1.35).sp,
        fontWeight = FontWeight.Normal,
    )

    /** Artist and album lines, durations, tile sub-labels. Tile header labels, letter headers (700) and player extras use 13/600. */
    val secondary = TextStyle(
        fontSize = 13.sp,
        lineHeight = (13 * 1.35).sp,
        fontWeight = FontWeight.Normal,
    )

    /** Small labels, rail labels (500, 700 when selected), timer badge (700). */
    val label = TextStyle(
        fontSize = 12.sp,
        lineHeight = (12 * 1.35).sp,
        fontWeight = FontWeight.SemiBold,
    )

    /** A-Z scrubber letters only. */
    val index = TextStyle(
        fontSize = 10.5.sp,
        lineHeight = (10.5 * 1.35).sp,
        fontWeight = FontWeight.Bold,
    )
}
