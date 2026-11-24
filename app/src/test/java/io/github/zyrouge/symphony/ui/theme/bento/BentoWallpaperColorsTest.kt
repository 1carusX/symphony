package io.github.zyrouge.symphony.ui.theme.bento

import androidx.compose.ui.graphics.Color
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.abs

class BentoWallpaperColorsTest {
    /**
     * Resolving at a primary hue's degrees must land on that hue's token
     * values, which shows every hue-dependent role is plain hsl(h s l / a).
     */
    @Test
    fun `any-hue palettes reproduce every primary hue`() {
        for (scheme in BentoColorScheme.entries) {
            for (mode in BentoColorMode.entries) {
                for (hue in BentoPrimaryHue.entries) {
                    val expected = roles(BentoColorRoles.resolve(scheme, mode, hue))
                    val actual = roles(BentoColorRoles.resolve(scheme, mode, hue.degrees))
                    expected.keys.forEach { role ->
                        assertClose(expected.getValue(role), actual.getValue(role), "$scheme $mode $hue $role")
                    }
                }
            }
        }
    }

    @Test
    fun `hue independent roles stay put at any hue`() {
        val rose = BentoColorRoles.resolve(BentoColorScheme.NEUTRAL, BentoColorMode.BLACK, BentoPrimaryHue.ROSE)
        val teal = BentoColorRoles.resolve(BentoColorScheme.NEUTRAL, BentoColorMode.BLACK, 180f)
        assertEquals(rose.bg, teal.bg)
    }

    @Test
    fun `hsl reads back hue, saturation and lightness`() {
        val (h, s, l) = bentoHsl(Color.hsl(215f, 0.52f, 0.40f))
        assertEquals(215f, h, 0.5f)
        assertEquals(0.52f, s, 0.01f)
        assertEquals(0.40f, l, 0.01f)
    }

    private fun roles(colors: BentoColors) = mapOf(
        "bg" to colors.bg,
        "surface" to colors.surface,
        "raised" to colors.raised,
        "high" to colors.high,
        "float" to colors.`float`,
        "fg" to colors.fg,
        "sub" to colors.sub,
        "muted" to colors.muted,
        "line" to colors.line,
        "track" to colors.track,
        "accent" to colors.accent,
        "onAccent" to colors.onAccent,
        "onAccentSub" to colors.onAccentSub,
        "tonal" to colors.tonal,
        "onTonal" to colors.onTonal,
        "tonalSub" to colors.tonalSub,
        "edge" to colors.edge,
        "scrim" to colors.scrim,
        "shadow0" to colors.shadow.layers[0].color,
        "shadow1" to colors.shadow.layers[1].color,
        "phoneSheet" to colors.phoneSheetShadow.color,
    )

    private fun assertClose(expected: Color, actual: Color, label: String) {
        // 8-bit token values round-trip through HSL within a couple of steps.
        val tolerance = 2.5f / 255f
        listOf(
            expected.red to actual.red,
            expected.green to actual.green,
            expected.blue to actual.blue,
            expected.alpha to actual.alpha,
        ).forEach { (e, a) ->
            assertTrue(abs(e - a) <= tolerance, "$label: expected $expected, got $actual")
        }
    }
}
