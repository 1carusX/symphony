// Bento design tokens: color roles (docs/design/bento/tokens.json).
//
// The palette tables hold the resolved hex values recorded in tokens.json for
// each scheme, mode and primary hue; hue-independent formulas repeat their single
// value. The HSL formulas are kept as comments next to each role.
// BentoColorRolesTest asserts every value against tokens.json.

package io.github.zyrouge.symphony.ui.theme.bento

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Theme mode selection (tokens.json modes).
 */
enum class BentoThemeMode {
    /** Follows the OS: light or dark. */
    SYSTEM,
    /** Light surfaces. */
    LIGHT,
    /** Dark surfaces. */
    DARK,
    /** Dark with a pure black background for OLED; surfaces slightly darker than dark. */
    BLACK,
}

/**
 * Resolved color mode; [BentoThemeMode.SYSTEM] becomes LIGHT or DARK based on the OS setting.
 */
enum class BentoColorMode {
    LIGHT,
    DARK,
    BLACK,
}

/**
 * Color scheme selection (tokens.json schemes).
 */
enum class BentoColorScheme {
    /** Default. Surfaces carry a little of the primary hue. */
    TINTED,
    /** Near-gray surfaces; color only on accent and tonal. */
    NEUTRAL,
    /** Stronger tint on surfaces and fuller selection colors. */
    VIVID,
    /** Black or white backgrounds, tiles outlined with a 1 dp line ring, highest text contrast. */
    CONTRAST,
}

/**
 * Primary hue selection (tokens.json primaryHues).
 */
enum class BentoPrimaryHue(val degrees: Float) {
    ROSE(345f),
    BLUE(215f),
    GREEN(150f),
    AMBER(38f),
    VIOLET(275f),
}

/**
 * Resolved color roles for one scheme, mode and primary hue.
 */
@Immutable
data class BentoColors(
    val bg: Color,
    val surface: Color,
    val raised: Color,
    val high: Color,
    val `float`: Color,
    val fg: Color,
    val sub: Color,
    val muted: Color,
    val line: Color,
    val track: Color,
    val accent: Color,
    val onAccent: Color,
    val onAccentSub: Color,
    val tonal: Color,
    val onTonal: Color,
    val tonalSub: Color,
    val edge: Color,
    val scrim: Color,
    val shadow: BentoShadow,
    val phoneSheetShadow: BentoShadowLayer,
)

/**
 * Two-layer drop shadow of floating surfaces.
 */
@Immutable
data class BentoShadow(val layers: List<BentoShadowLayer>)

/**
 * One shadow layer; offsets, blur and spread in dp.
 */
@Immutable
data class BentoShadowLayer(
    val offsetX: Float,
    val offsetY: Float,
    val blur: Float,
    val spread: Float,
    val color: Color,
)

/**
 * Resolved role values for one scheme and mode, indexed by [BentoPrimaryHue.ordinal].
 */
private class BentoSchemePalette(
    val bg: LongArray,
    val surface: LongArray,
    val raised: LongArray,
    val high: LongArray,
    val `float`: LongArray,
    val fg: LongArray,
    val sub: LongArray,
    val muted: LongArray,
    val line: LongArray,
    val track: LongArray,
    val accent: LongArray,
    val onAccent: LongArray,
    val onAccentSub: LongArray,
    val tonal: LongArray,
    val onTonal: LongArray,
    val tonalSub: LongArray,
    val edge: LongArray,
    val scrim: LongArray,
    val shadow: List<BentoShadowLayerTable>,
)

private class BentoShadowLayerTable(
    val offsetX: Float,
    val offsetY: Float,
    val blur: Float,
    val spread: Float,
    val color: LongArray,
)

/**
 * Resolves [BentoColors] from the token tables. The values come straight from
 * tokens.json, so resolving is a cheap lookup; call it once per selection change,
 * not per recomposition.
 */
object BentoColorRoles {
    fun resolve(
        scheme: BentoColorScheme,
        mode: BentoColorMode,
        hue: BentoPrimaryHue,
    ): BentoColors = resolve(scheme, mode) { values -> Color(values[hue.ordinal].toInt()) }

    /**
     * The roles at any hue, for wallpaper colors. Every hue-dependent token is
     * `hsl(h s l / a)` with the same s, l and a for all primary hues, so each
     * role keeps those and takes [hueDegrees]; hue-independent roles are
     * unchanged.
     */
    fun resolve(
        scheme: BentoColorScheme,
        mode: BentoColorMode,
        hueDegrees: Float,
    ): BentoColors = resolve(scheme, mode) { values -> rehueBentoRole(values, hueDegrees) }

    private fun resolve(
        scheme: BentoColorScheme,
        mode: BentoColorMode,
        role: (LongArray) -> Color,
    ): BentoColors {
        val palette = palettes.getValue(scheme).getValue(mode)
        return BentoColors(
            bg = role(palette.bg),
            surface = role(palette.surface),
            raised = role(palette.raised),
            high = role(palette.high),
            `float` = role(palette.`float`),
            fg = role(palette.fg),
            sub = role(palette.sub),
            muted = role(palette.muted),
            line = role(palette.line),
            track = role(palette.track),
            accent = role(palette.accent),
            onAccent = role(palette.onAccent),
            onAccentSub = role(palette.onAccentSub),
            tonal = role(palette.tonal),
            onTonal = role(palette.onTonal),
            tonalSub = role(palette.tonalSub),
            edge = role(palette.edge),
            scrim = role(palette.scrim),
            shadow = BentoShadow(
                palette.shadow.map { layer ->
                    BentoShadowLayer(
                        offsetX = layer.offsetX,
                        offsetY = layer.offsetY,
                        blur = layer.blur,
                        spread = layer.spread,
                        color = role(layer.color),
                    )
                },
            ),
            phoneSheetShadow = BentoShadowLayer(
                offsetX = 0f,
                offsetY = -6f,
                blur = 28f,
                spread = 0f,
                color = role(phoneSheetShadowColor),
            ),
        )
    }

    /** Bottom sheet shadow (tokens.json elevation.phoneSheet), shared by all schemes. */
    private val phoneSheetShadowColor = longArrayOf(0x590A0507L, 0x5905070AL, 0x59050A08L, 0x590A0805L, 0x5908050AL)

    private fun allHues(value: Long) = longArrayOf(value, value, value, value, value)

    private val tintedLight = BentoSchemePalette(
        bg = longArrayOf(0xFFFFF5F7L, 0xFFF5F9FFL, 0xFFF5FFFAL, 0xFFFFFBF5L, 0xFFFBF5FFL), // hsl(h 100% 98%)
        surface = longArrayOf(0xFFF7E9ECL, 0xFFE9EFF7L, 0xFFE9F7F0L, 0xFFF7F2E9L, 0xFFF1E9F7L), // hsl(h 45% 94%)
        raised = longArrayOf(0xFFEFDCE1L, 0xFFDCE4EFL, 0xFFDCEFE6L, 0xFFEFE8DCL, 0xFFE7DCEFL), // hsl(h 38% 90%)
        high = longArrayOf(0xFFEBD5DBL, 0xFFD5DFEBL, 0xFFD5EBE0L, 0xFFEBE3D5L, 0xFFE2D5EBL), // hsl(h 36% 88%)
        `float` = longArrayOf(0xFFFFFCFDL, 0xFFFCFEFFL, 0xFFFCFFFEL, 0xFFFFFEFCL, 0xFFFEFCFFL), // hsl(h 100% 99.5%)
        fg = longArrayOf(0xFF24191CL, 0xFF191E24L, 0xFF19241FL, 0xFF242019L, 0xFF201924L), // hsl(h 18% 12%)
        sub = longArrayOf(0xFF5D4C50L, 0xFF4C535DL, 0xFF4C5D54L, 0xFF5D564CL, 0xFF564C5DL), // hsl(h 10% 33%)
        muted = longArrayOf(0xFF7C6E72L, 0xFF6E747CL, 0xFF6E7C75L, 0xFF7C776EL, 0xFF766E7CL), // hsl(h 6% 46%)
        line = longArrayOf(0xFFD8CACEL, 0xFFCAD0D8L, 0xFFCAD8D1L, 0xFFD8D3CAL, 0xFFD2CAD8L), // hsl(h 15% 82%)
        track = longArrayOf(0xFFD9C9CDL, 0xFFC9D0D9L, 0xFFC9D9D1L, 0xFFD9D3C9L, 0xFFD2C9D9L), // hsl(h 18% 82%)
        accent = longArrayOf(0xFF9B314BL, 0xFF315D9BL, 0xFF319B66L, 0xFF9B7431L, 0xFF6F319BL), // hsl(h 52% 40%)
        onAccent = longArrayOf(0xFFFFFAFBL, 0xFFFAFCFFL, 0xFFFAFFFCL, 0xFFFFFDFAL, 0xFFFDFAFFL), // hsl(h 100% 99%)
        onAccentSub = longArrayOf(0xFFF7D4DDL, 0xFFD4E3F7L, 0xFFD4F7E6L, 0xFFF7EAD4L, 0xFFE8D4F7L), // hsl(h 70% 90%)
        tonal = longArrayOf(0xFFF9D2DCL, 0xFFD2E2F9L, 0xFFD2F9E6L, 0xFFF9EBD2L, 0xFFE9D2F9L), // hsl(h 75% 90%)
        onTonal = longArrayOf(0xFF43141FL, 0xFF142743L, 0xFF14432BL, 0xFF433214L, 0xFF2F1443L), // hsl(h 55% 17%)
        tonalSub = longArrayOf(0xFF76424FL, 0xFF425876L, 0xFF42765CL, 0xFF766342L, 0xFF604276L), // hsl(h 28% 36%)
        edge = longArrayOf(0x1A40262DL, 0x1A263140L, 0x1A264033L, 0x1A403626L, 0x1A352640L), // hsl(h 25% 20% / .1)
        scrim = longArrayOf(0x591F1417L, 0x5914191FL, 0x59141F1AL, 0x591F1B14L, 0x591A141FL), // hsl(h 20% 10% / .35)
        // 0 20px 48px -12px hsl(h 30% 20% / .32), 0 3px 10px hsl(h 30% 20% / .12)
        shadow = listOf(
            // hsl(h 30% 20% / .32)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -12f,
                color = longArrayOf(0x5242242BL, 0x52243042L, 0x52244233L, 0x52423724L, 0x52362442L),
            ),
            // hsl(h 30% 20% / .12)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 3f, blur = 10f, spread = 0f,
                color = longArrayOf(0x1F42242BL, 0x1F243042L, 0x1F244233L, 0x1F423724L, 0x1F362442L),
            ),
        ),
    )

    private val tintedDark = BentoSchemePalette(
        bg = longArrayOf(0xFF1B1315L, 0xFF13161BL, 0xFF131B17L, 0xFF1B1813L, 0xFF18131BL), // hsl(h 17% 9%)
        surface = longArrayOf(0xFF2B2124L, 0xFF21252BL, 0xFF212B26L, 0xFF2B2821L, 0xFF27212BL), // hsl(h 13% 15%)
        raised = longArrayOf(0xFF392D30L, 0xFF2D3239L, 0xFF2D3933L, 0xFF39342DL, 0xFF342D39L), // hsl(h 11% 20%)
        high = longArrayOf(0xFF43373AL, 0xFF373C43L, 0xFF37433DL, 0xFF433F37L, 0xFF3E3743L), // hsl(h 10% 24%)
        `float` = longArrayOf(0xFF3F3135L, 0xFF31373FL, 0xFF313F38L, 0xFF3F3A31L, 0xFF39313FL), // hsl(h 12% 22%)
        fg = longArrayOf(0xFFF2DEE3L, 0xFFDEE6F2L, 0xFFDEF2E8L, 0xFFF2EBDEL, 0xFFEADEF2L), // hsl(h 45% 91%)
        sub = longArrayOf(0xFFD2C1C5L, 0xFFC1C8D2L, 0xFFC1D2C9L, 0xFFD2CCC1L, 0xFFCBC1D2L), // hsl(h 16% 79%)
        muted = longArrayOf(0xFFA19195L, 0xFF9198A1L, 0xFF91A199L, 0xFFA19B91L, 0xFF9A91A1L), // hsl(h 8% 60%)
        line = longArrayOf(0xFF544549L, 0xFF454B54L, 0xFF45544CL, 0xFF544F45L, 0xFF4E4554L), // hsl(h 10% 30%)
        track = longArrayOf(0xFF5B484DL, 0xFF48505BL, 0xFF485B52L, 0xFF5B5448L, 0xFF53485BL), // hsl(h 12% 32%)
        accent = longArrayOf(0xFFFFB2C6L, 0xFFB2D2FFL, 0xFFB2FFD9L, 0xFFFFE3B2L, 0xFFDFB2FFL), // hsl(h 100% 85%)
        onAccent = longArrayOf(0xFF5F1124L, 0xFF11325FL, 0xFF115F38L, 0xFF5F4311L, 0xFF3F115FL), // hsl(h 70% 22%)
        onAccentSub = longArrayOf(0xFF7D2C40L, 0xFF2C4D7DL, 0xFF2C7D54L, 0xFF7D5F2CL, 0xFF5B2C7DL), // hsl(h 48% 33%)
        tonal = longArrayOf(0xFF5B3E45L, 0xFF3E4A5BL, 0xFF3E5B4DL, 0xFF5B503EL, 0xFF4F3E5BL), // hsl(h 19% 30%)
        onTonal = longArrayOf(0xFFFFDBE4L, 0xFFDBEAFFL, 0xFFDBFFEDL, 0xFFFFF2DBL, 0xFFF0DBFFL), // hsl(h 100% 93%)
        tonalSub = longArrayOf(0xFFEFC8D1L, 0xFFC8D8EFL, 0xFFC8EFDBL, 0xFFEFE1C8L, 0xFFDFC8EFL), // hsl(h 55% 86%)
        edge = longArrayOf(0x1FF3E2E7L, 0x1FE2E9F3L, 0x1FE2F3EBL, 0x1FF3EDE2L, 0x1FECE2F3L), // hsl(h 40% 92% / .12)
        scrim = longArrayOf(0x8C0A0507L, 0x8C05070AL, 0x8C050A08L, 0x8C0A0805L, 0x8C08050AL), // hsl(h 30% 3% / .55)
        // 0 20px 48px -10px hsl(h 40% 2% / .8), 0 4px 14px hsl(h 40% 2% / .45)
        shadow = listOf(
            // hsl(h 40% 2% / .8)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -10f,
                color = longArrayOf(0xCC070304L, 0xCC030507L, 0xCC030705L, 0xCC070603L, 0xCC050307L),
            ),
            // hsl(h 40% 2% / .45)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 4f, blur = 14f, spread = 0f,
                color = longArrayOf(0x73070304L, 0x73030507L, 0x73030705L, 0x73070603L, 0x73050307L),
            ),
        ),
    )

    private val tintedBlack = BentoSchemePalette(
        bg = allHues(0xFF000000L), // hsl(0 0% 0%)
        surface = longArrayOf(0xFF161314L, 0xFF131416L, 0xFF131614L, 0xFF161513L, 0xFF151316L), // hsl(h 8% 8%)
        raised = longArrayOf(0xFF231F20L, 0xFF1F2123L, 0xFF1F2321L, 0xFF23221FL, 0xFF221F23L), // hsl(h 7% 13%)
        high = longArrayOf(0xFF312B2CL, 0xFF2B2D31L, 0xFF2B312EL, 0xFF312F2BL, 0xFF2E2B31L), // hsl(h 7% 18%)
        `float` = longArrayOf(0xFF292425L, 0xFF242629L, 0xFF242926L, 0xFF292724L, 0xFF272429L), // hsl(h 7% 15%)
        fg = longArrayOf(0xFFF2DEE3L, 0xFFDEE6F2L, 0xFFDEF2E8L, 0xFFF2EBDEL, 0xFFEADEF2L), // hsl(h 45% 91%)
        sub = longArrayOf(0xFFD2C1C5L, 0xFFC1C8D2L, 0xFFC1D2C9L, 0xFFD2CCC1L, 0xFFCBC1D2L), // hsl(h 16% 79%)
        muted = longArrayOf(0xFFA19195L, 0xFF9198A1L, 0xFF91A199L, 0xFFA19B91L, 0xFF9A91A1L), // hsl(h 8% 60%)
        line = longArrayOf(0xFF544549L, 0xFF454B54L, 0xFF45544CL, 0xFF544F45L, 0xFF4E4554L), // hsl(h 10% 30%)
        track = longArrayOf(0xFF493C3FL, 0xFF3C4149L, 0xFF3C4942L, 0xFF49443CL, 0xFF433C49L), // hsl(h 10% 26%)
        accent = longArrayOf(0xFFFFB2C6L, 0xFFB2D2FFL, 0xFFB2FFD9L, 0xFFFFE3B2L, 0xFFDFB2FFL), // hsl(h 100% 85%)
        onAccent = longArrayOf(0xFF5F1124L, 0xFF11325FL, 0xFF115F38L, 0xFF5F4311L, 0xFF3F115FL), // hsl(h 70% 22%)
        onAccentSub = longArrayOf(0xFF7D2C40L, 0xFF2C4D7DL, 0xFF2C7D54L, 0xFF7D5F2CL, 0xFF5B2C7DL), // hsl(h 48% 33%)
        tonal = longArrayOf(0xFF5B3E45L, 0xFF3E4A5BL, 0xFF3E5B4DL, 0xFF5B503EL, 0xFF4F3E5BL), // hsl(h 19% 30%)
        onTonal = longArrayOf(0xFFFFDBE4L, 0xFFDBEAFFL, 0xFFDBFFEDL, 0xFFFFF2DBL, 0xFFF0DBFFL), // hsl(h 100% 93%)
        tonalSub = longArrayOf(0xFFEFC8D1L, 0xFFC8D8EFL, 0xFFC8EFDBL, 0xFFEFE1C8L, 0xFFDFC8EFL), // hsl(h 55% 86%)
        edge = longArrayOf(0x1FF3E2E7L, 0x1FE2E9F3L, 0x1FE2F3EBL, 0x1FF3EDE2L, 0x1FECE2F3L), // hsl(h 40% 92% / .12)
        scrim = longArrayOf(0x8C0A0507L, 0x8C05070AL, 0x8C050A08L, 0x8C0A0805L, 0x8C08050AL), // hsl(h 30% 3% / .55)
        // 0 20px 48px -10px hsl(h 40% 2% / .8), 0 4px 14px hsl(h 40% 2% / .45)
        shadow = listOf(
            // hsl(h 40% 2% / .8)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -10f,
                color = longArrayOf(0xCC070304L, 0xCC030507L, 0xCC030705L, 0xCC070603L, 0xCC050307L),
            ),
            // hsl(h 40% 2% / .45)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 4f, blur = 14f, spread = 0f,
                color = longArrayOf(0x73070304L, 0x73030507L, 0x73030705L, 0x73070603L, 0x73050307L),
            ),
        ),
    )

    private val neutralLight = BentoSchemePalette(
        bg = longArrayOf(0xFFFAF9FAL, 0xFFF9FAFAL, 0xFFF9FAFAL, 0xFFFAFAF9L, 0xFFFAF9FAL), // hsl(h 8% 98%)
        surface = longArrayOf(0xFFF2F0F1L, 0xFFF0F1F2L, 0xFFF0F2F1L, 0xFFF2F1F0L, 0xFFF1F0F2L), // hsl(h 6% 94.5%)
        raised = longArrayOf(0xFFE7E4E5L, 0xFFE4E5E7L, 0xFFE4E7E6L, 0xFFE7E6E4L, 0xFFE6E4E7L), // hsl(h 6% 90%)
        high = longArrayOf(0xFFE0DCDDL, 0xFFDCDEE0L, 0xFFDCE0DEL, 0xFFE0DEDCL, 0xFFDEDCE0L), // hsl(h 6% 87%)
        `float` = allHues(0xFFFFFFFFL), // hsl(0 0% 100%)
        fg = longArrayOf(0xFF1C1718L, 0xFF17191CL, 0xFF171C1AL, 0xFF1C1A17L, 0xFF1A171CL), // hsl(h 8% 10%)
        sub = longArrayOf(0xFF554E50L, 0xFF4E5155L, 0xFF4E5552L, 0xFF55524EL, 0xFF524E55L), // hsl(h 4% 32%)
        muted = longArrayOf(0xFF766F71L, 0xFF6F7276L, 0xFF6F7673L, 0xFF76746FL, 0xFF736F76L), // hsl(h 3% 45%)
        line = longArrayOf(0xFFD8D4D5L, 0xFFD4D6D8L, 0xFFD4D8D6L, 0xFFD8D7D4L, 0xFFD7D4D8L), // hsl(h 5% 84%)
        track = longArrayOf(0xFFD3CFD0L, 0xFFCFD1D3L, 0xFFCFD3D1L, 0xFFD3D2CFL, 0xFFD1CFD3L), // hsl(h 5% 82%)
        accent = longArrayOf(0xFFA6304EL, 0xFF3061A6L, 0xFF30A66BL, 0xFFA67B30L, 0xFF7530A6L), // hsl(h 55% 42%)
        onAccent = longArrayOf(0xFFFFFAFBL, 0xFFFAFCFFL, 0xFFFAFFFCL, 0xFFFFFDFAL, 0xFFFDFAFFL), // hsl(h 100% 99%)
        onAccentSub = longArrayOf(0xFFF5D6DEL, 0xFFD6E3F5L, 0xFFD6F5E6L, 0xFFF5EAD6L, 0xFFE8D6F5L), // hsl(h 60% 90%)
        tonal = longArrayOf(0xFFF0DBE0L, 0xFFDBE4F0L, 0xFFDBF0E6L, 0xFFF0E8DBL, 0xFFE7DBF0L), // hsl(h 40% 90%)
        onTonal = longArrayOf(0xFF451722L, 0xFF172A45L, 0xFF17452EL, 0xFF453417L, 0xFF321745L), // hsl(h 50% 18%)
        tonalSub = longArrayOf(0xFF6E4953L, 0xFF49596EL, 0xFF496E5CL, 0xFF6E6149L, 0xFF5F496EL), // hsl(h 20% 36%)
        edge = longArrayOf(0x1A3D292EL, 0x1A29313DL, 0x1A293D33L, 0x1A3D3629L, 0x1A35293DL), // hsl(h 20% 20% / .1)
        scrim = longArrayOf(0x591F1417L, 0x5914191FL, 0x59141F1AL, 0x591F1B14L, 0x591A141FL), // hsl(h 20% 10% / .35)
        // 0 20px 48px -12px hsl(h 30% 20% / .32), 0 3px 10px hsl(h 30% 20% / .12)
        shadow = listOf(
            // hsl(h 30% 20% / .32)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -12f,
                color = longArrayOf(0x5242242BL, 0x52243042L, 0x52244233L, 0x52423724L, 0x52362442L),
            ),
            // hsl(h 30% 20% / .12)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 3f, blur = 10f, spread = 0f,
                color = longArrayOf(0x1F42242BL, 0x1F243042L, 0x1F244233L, 0x1F423724L, 0x1F362442L),
            ),
        ),
    )

    private val neutralDark = BentoSchemePalette(
        bg = longArrayOf(0xFF151414L, 0xFF141415L, 0xFF141514L, 0xFF151514L, 0xFF151415L), // hsl(h 4% 8%)
        surface = longArrayOf(0xFF222020L, 0xFF202122L, 0xFF202221L, 0xFF222220L, 0xFF212022L), // hsl(h 4% 13%)
        raised = longArrayOf(0xFF302C2DL, 0xFF2C2E30L, 0xFF2C302EL, 0xFF302E2CL, 0xFF2E2C30L), // hsl(h 4% 18%)
        high = longArrayOf(0xFF3A3637L, 0xFF36383AL, 0xFF363A38L, 0xFF3A3936L, 0xFF38363AL), // hsl(h 4% 22%)
        `float` = longArrayOf(0xFF383334L, 0xFF333538L, 0xFF333836L, 0xFF383633L, 0xFF363338L), // hsl(h 4% 21%)
        fg = longArrayOf(0xFFEFECECL, 0xFFECEDEFL, 0xFFECEFEDL, 0xFFEFEEECL, 0xFFEDECEFL), // hsl(h 8% 93%)
        sub = longArrayOf(0xFFBFBABBL, 0xFFBABCBFL, 0xFFBABFBDL, 0xFFBFBDBAL, 0xFFBDBABFL), // hsl(h 4% 74%)
        muted = longArrayOf(0xFF928B8DL, 0xFF8B8E92L, 0xFF8B928FL, 0xFF92908BL, 0xFF8F8B92L), // hsl(h 3% 56%)
        line = longArrayOf(0xFF484243L, 0xFF424448L, 0xFF424845L, 0xFF484642L, 0xFF454248L), // hsl(h 4% 27%)
        track = longArrayOf(0xFF4A4546L, 0xFF45474AL, 0xFF454A47L, 0xFF4A4845L, 0xFF48454AL), // hsl(h 4% 28%)
        accent = longArrayOf(0xFFFA9EB5L, 0xFF9EC4FAL, 0xFF9EFACCL, 0xFFFAD89EL, 0xFFD49EFAL), // hsl(h 90% 80%)
        onAccent = longArrayOf(0xFF491220L, 0xFF122949L, 0xFF12492EL, 0xFF493512L, 0xFF321249L), // hsl(h 60% 18%)
        onAccentSub = longArrayOf(0xFF6F2A3BL, 0xFF2A476FL, 0xFF2A6F4DL, 0xFF6F562AL, 0xFF522A6FL), // hsl(h 45% 30%)
        tonal = longArrayOf(0xFF4C393EL, 0xFF39414CL, 0xFF394C42L, 0xFF4C4539L, 0xFF44394CL), // hsl(h 14% 26%)
        onTonal = longArrayOf(0xFFFBDAE2L, 0xFFDAE8FBL, 0xFFDAFBEBL, 0xFFFBEFDAL, 0xFFEDDAFBL), // hsl(h 80% 92%)
        tonalSub = longArrayOf(0xFFDBBDC4L, 0xFFBDC9DBL, 0xFFBDDBCCL, 0xFFDBD0BDL, 0xFFCFBDDBL), // hsl(h 30% 80%)
        edge = allHues(0x1FFFFFFFL), // hsl(0 0% 100% / .12)
        scrim = longArrayOf(0x8C0A0507L, 0x8C05070AL, 0x8C050A08L, 0x8C0A0805L, 0x8C08050AL), // hsl(h 30% 3% / .55)
        // 0 20px 48px -10px hsl(h 40% 2% / .8), 0 4px 14px hsl(h 40% 2% / .45)
        shadow = listOf(
            // hsl(h 40% 2% / .8)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -10f,
                color = longArrayOf(0xCC070304L, 0xCC030507L, 0xCC030705L, 0xCC070603L, 0xCC050307L),
            ),
            // hsl(h 40% 2% / .45)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 4f, blur = 14f, spread = 0f,
                color = longArrayOf(0x73070304L, 0x73030507L, 0x73030705L, 0x73070603L, 0x73050307L),
            ),
        ),
    )

    private val neutralBlack = BentoSchemePalette(
        bg = allHues(0xFF000000L), // hsl(0 0% 0%)
        surface = longArrayOf(0xFF161314L, 0xFF131416L, 0xFF131614L, 0xFF161513L, 0xFF151316L), // hsl(h 8% 8%)
        raised = longArrayOf(0xFF231F20L, 0xFF1F2123L, 0xFF1F2321L, 0xFF23221FL, 0xFF221F23L), // hsl(h 7% 13%)
        high = longArrayOf(0xFF312B2CL, 0xFF2B2D31L, 0xFF2B312EL, 0xFF312F2BL, 0xFF2E2B31L), // hsl(h 7% 18%)
        `float` = longArrayOf(0xFF292425L, 0xFF242629L, 0xFF242926L, 0xFF292724L, 0xFF272429L), // hsl(h 7% 15%)
        fg = longArrayOf(0xFFEFECECL, 0xFFECEDEFL, 0xFFECEFEDL, 0xFFEFEEECL, 0xFFEDECEFL), // hsl(h 8% 93%)
        sub = longArrayOf(0xFFBFBABBL, 0xFFBABCBFL, 0xFFBABFBDL, 0xFFBFBDBAL, 0xFFBDBABFL), // hsl(h 4% 74%)
        muted = longArrayOf(0xFF928B8DL, 0xFF8B8E92L, 0xFF8B928FL, 0xFF92908BL, 0xFF8F8B92L), // hsl(h 3% 56%)
        line = longArrayOf(0xFF484243L, 0xFF424448L, 0xFF424845L, 0xFF484642L, 0xFF454248L), // hsl(h 4% 27%)
        track = longArrayOf(0xFF4A4546L, 0xFF45474AL, 0xFF454A47L, 0xFF4A4845L, 0xFF48454AL), // hsl(h 4% 28%)
        accent = longArrayOf(0xFFFA9EB5L, 0xFF9EC4FAL, 0xFF9EFACCL, 0xFFFAD89EL, 0xFFD49EFAL), // hsl(h 90% 80%)
        onAccent = longArrayOf(0xFF491220L, 0xFF122949L, 0xFF12492EL, 0xFF493512L, 0xFF321249L), // hsl(h 60% 18%)
        onAccentSub = longArrayOf(0xFF6F2A3BL, 0xFF2A476FL, 0xFF2A6F4DL, 0xFF6F562AL, 0xFF522A6FL), // hsl(h 45% 30%)
        tonal = longArrayOf(0xFF4C393EL, 0xFF39414CL, 0xFF394C42L, 0xFF4C4539L, 0xFF44394CL), // hsl(h 14% 26%)
        onTonal = longArrayOf(0xFFFBDAE2L, 0xFFDAE8FBL, 0xFFDAFBEBL, 0xFFFBEFDAL, 0xFFEDDAFBL), // hsl(h 80% 92%)
        tonalSub = longArrayOf(0xFFDBBDC4L, 0xFFBDC9DBL, 0xFFBDDBCCL, 0xFFDBD0BDL, 0xFFCFBDDBL), // hsl(h 30% 80%)
        edge = allHues(0x1FFFFFFFL), // hsl(0 0% 100% / .12)
        scrim = longArrayOf(0x8C0A0507L, 0x8C05070AL, 0x8C050A08L, 0x8C0A0805L, 0x8C08050AL), // hsl(h 30% 3% / .55)
        // 0 20px 48px -10px hsl(h 40% 2% / .8), 0 4px 14px hsl(h 40% 2% / .45)
        shadow = listOf(
            // hsl(h 40% 2% / .8)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -10f,
                color = longArrayOf(0xCC070304L, 0xCC030507L, 0xCC030705L, 0xCC070603L, 0xCC050307L),
            ),
            // hsl(h 40% 2% / .45)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 4f, blur = 14f, spread = 0f,
                color = longArrayOf(0x73070304L, 0x73030507L, 0x73030705L, 0x73070603L, 0x73050307L),
            ),
        ),
    )

    private val vividLight = BentoSchemePalette(
        bg = longArrayOf(0xFFFFF0F4L, 0xFFF0F6FFL, 0xFFF0FFF7L, 0xFFFFF9F0L, 0xFFF9F0FFL), // hsl(h 100% 97%)
        surface = longArrayOf(0xFFF9DCE3L, 0xFFDCE8F9L, 0xFFDCF9EBL, 0xFFF9EEDCL, 0xFFEDDCF9L), // hsl(h 70% 92%)
        raised = longArrayOf(0xFFF2CAD4L, 0xFFCADBF2L, 0xFFCAF2DEL, 0xFFF2E3CAL, 0xFFE1CAF2L), // hsl(h 60% 87%)
        high = longArrayOf(0xFFEDC0CBL, 0xFFC0D2EDL, 0xFFC0EDD6L, 0xFFEDDCC0L, 0xFFDAC0EDL), // hsl(h 55% 84%)
        `float` = longArrayOf(0xFFFFFAFBL, 0xFFFAFCFFL, 0xFFFAFFFCL, 0xFFFFFDFAL, 0xFFFDFAFFL), // hsl(h 100% 99%)
        fg = longArrayOf(0xFF2B1218L, 0xFF121D2BL, 0xFF122B1FL, 0xFF2B2212L, 0xFF21122BL), // hsl(h 40% 12%)
        sub = longArrayOf(0xFF5C3D45L, 0xFF3D4A5CL, 0xFF3D5C4DL, 0xFF5C513DL, 0xFF4F3D5CL), // hsl(h 20% 30%)
        muted = longArrayOf(0xFF7B656BL, 0xFF656E7BL, 0xFF657B70L, 0xFF7B7365L, 0xFF72657BL), // hsl(h 10% 44%)
        line = longArrayOf(0xFFDBBDC4L, 0xFFBDC9DBL, 0xFFBDDBCCL, 0xFFDBD0BDL, 0xFFCFBDDBL), // hsl(h 30% 80%)
        track = longArrayOf(0xFFDBBDC4L, 0xFFBDC9DBL, 0xFFBDDBCCL, 0xFFDBD0BDL, 0xFFCFBDDBL), // hsl(h 30% 80%)
        accent = longArrayOf(0xFFB12548L, 0xFF255FB1L, 0xFF25B16BL, 0xFFB17E25L, 0xFF7725B1L), // hsl(h 65% 42%)
        onAccent = longArrayOf(0xFFFFFAFBL, 0xFFFAFCFFL, 0xFFFAFFFCL, 0xFFFFFDFAL, 0xFFFDFAFFL), // hsl(h 100% 99%)
        onAccentSub = longArrayOf(0xFFFAD1DBL, 0xFFD1E2FAL, 0xFFD1FAE6L, 0xFFFAEBD1L, 0xFFE9D1FAL), // hsl(h 80% 90%)
        tonal = longArrayOf(0xFFFBB6C8L, 0xFFB6D3FBL, 0xFFB6FBD9L, 0xFFFBE2B6L, 0xFFDEB6FBL), // hsl(h 90% 85%)
        onTonal = longArrayOf(0xFF4E0E1EL, 0xFF0E294EL, 0xFF0E4E2EL, 0xFF4E360EL, 0xFF330E4EL), // hsl(h 70% 18%)
        tonalSub = longArrayOf(0xFF793445L, 0xFF345179L, 0xFF347957L, 0xFF796034L, 0xFF5C3479L), // hsl(h 40% 34%)
        edge = longArrayOf(0x1A3D292EL, 0x1A29313DL, 0x1A293D33L, 0x1A3D3629L, 0x1A35293DL), // hsl(h 20% 20% / .1)
        scrim = longArrayOf(0x591F1417L, 0x5914191FL, 0x59141F1AL, 0x591F1B14L, 0x591A141FL), // hsl(h 20% 10% / .35)
        // 0 20px 48px -12px hsl(h 30% 20% / .32), 0 3px 10px hsl(h 30% 20% / .12)
        shadow = listOf(
            // hsl(h 30% 20% / .32)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -12f,
                color = longArrayOf(0x5242242BL, 0x52243042L, 0x52244233L, 0x52423724L, 0x52362442L),
            ),
            // hsl(h 30% 20% / .12)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 3f, blur = 10f, spread = 0f,
                color = longArrayOf(0x1F42242BL, 0x1F243042L, 0x1F244233L, 0x1F423724L, 0x1F362442L),
            ),
        ),
    )

    private val vividDark = BentoSchemePalette(
        bg = longArrayOf(0xFF1E1014L, 0xFF10161EL, 0xFF101E17L, 0xFF1E1910L, 0xFF18101EL), // hsl(h 30% 9%)
        surface = longArrayOf(0xFF301D21L, 0xFF1D2530L, 0xFF1D3026L, 0xFF30291DL, 0xFF281D30L), // hsl(h 25% 15%)
        raised = longArrayOf(0xFF412A30L, 0xFF2A3441L, 0xFF2A4136L, 0xFF41392AL, 0xFF382A41L), // hsl(h 22% 21%)
        high = longArrayOf(0xFF4C3339L, 0xFF333E4CL, 0xFF334C40L, 0xFF4C4333L, 0xFF42334CL), // hsl(h 20% 25%)
        `float` = longArrayOf(0xFF4B3036L, 0xFF303B4BL, 0xFF304B3DL, 0xFF4B4130L, 0xFF3F304BL), // hsl(h 22% 24%)
        fg = longArrayOf(0xFFF8E2E8L, 0xFFE2EBF8L, 0xFFE2F8EDL, 0xFFF8F0E2L, 0xFFEFE2F8L), // hsl(h 60% 93%)
        sub = longArrayOf(0xFFDBBDC4L, 0xFFBDC9DBL, 0xFFBDDBCCL, 0xFFDBD0BDL, 0xFFCFBDDBL), // hsl(h 30% 80%)
        muted = longArrayOf(0xFFAD9097L, 0xFF909CADL, 0xFF90AD9EL, 0xFFADA290L, 0xFFA190ADL), // hsl(h 15% 62%)
        line = longArrayOf(0xFF5A3F46L, 0xFF3F4A5AL, 0xFF3F5A4DL, 0xFF5A503FL, 0xFF4F3F5AL), // hsl(h 18% 30%)
        track = longArrayOf(0xFF624149L, 0xFF414F62L, 0xFF416252L, 0xFF625641L, 0xFF544162L), // hsl(h 20% 32%)
        accent = longArrayOf(0xFFFFA3BAL, 0xFFA3C9FFL, 0xFFA3FFD1L, 0xFFFFDDA3L, 0xFFD9A3FFL), // hsl(h 100% 82%)
        onAccent = longArrayOf(0xFF5C0A1FL, 0xFF0A2C5CL, 0xFF0A5C33L, 0xFF5C3E0AL, 0xFF3A0A5CL), // hsl(h 80% 20%)
        onAccentSub = longArrayOf(0xFF7E253BL, 0xFF254A7EL, 0xFF257E52L, 0xFF7E5E25L, 0xFF59257EL), // hsl(h 55% 32%)
        tonal = longArrayOf(0xFF7B3244L, 0xFF32517BL, 0xFF327B57L, 0xFF7B6032L, 0xFF5D327BL), // hsl(h 42% 34%)
        onTonal = longArrayOf(0xFFFFE0E8L, 0xFFE0EDFFL, 0xFFE0FFF0L, 0xFFFFF4E0L, 0xFFF2E0FFL), // hsl(h 100% 94%)
        tonalSub = longArrayOf(0xFFF4C2CFL, 0xFFC2D7F4L, 0xFFC2F4DBL, 0xFFF4E2C2L, 0xFFDFC2F4L), // hsl(h 70% 86%)
        edge = allHues(0x1FFFFFFFL), // hsl(0 0% 100% / .12)
        scrim = longArrayOf(0x8C0A0507L, 0x8C05070AL, 0x8C050A08L, 0x8C0A0805L, 0x8C08050AL), // hsl(h 30% 3% / .55)
        // 0 20px 48px -10px hsl(h 40% 2% / .8), 0 4px 14px hsl(h 40% 2% / .45)
        shadow = listOf(
            // hsl(h 40% 2% / .8)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -10f,
                color = longArrayOf(0xCC070304L, 0xCC030507L, 0xCC030705L, 0xCC070603L, 0xCC050307L),
            ),
            // hsl(h 40% 2% / .45)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 4f, blur = 14f, spread = 0f,
                color = longArrayOf(0x73070304L, 0x73030507L, 0x73030705L, 0x73070603L, 0x73050307L),
            ),
        ),
    )

    private val vividBlack = BentoSchemePalette(
        bg = allHues(0xFF000000L), // hsl(0 0% 0%)
        surface = longArrayOf(0xFF161314L, 0xFF131416L, 0xFF131614L, 0xFF161513L, 0xFF151316L), // hsl(h 8% 8%)
        raised = longArrayOf(0xFF231F20L, 0xFF1F2123L, 0xFF1F2321L, 0xFF23221FL, 0xFF221F23L), // hsl(h 7% 13%)
        high = longArrayOf(0xFF312B2CL, 0xFF2B2D31L, 0xFF2B312EL, 0xFF312F2BL, 0xFF2E2B31L), // hsl(h 7% 18%)
        `float` = longArrayOf(0xFF292425L, 0xFF242629L, 0xFF242926L, 0xFF292724L, 0xFF272429L), // hsl(h 7% 15%)
        fg = longArrayOf(0xFFF8E2E8L, 0xFFE2EBF8L, 0xFFE2F8EDL, 0xFFF8F0E2L, 0xFFEFE2F8L), // hsl(h 60% 93%)
        sub = longArrayOf(0xFFDBBDC4L, 0xFFBDC9DBL, 0xFFBDDBCCL, 0xFFDBD0BDL, 0xFFCFBDDBL), // hsl(h 30% 80%)
        muted = longArrayOf(0xFFAD9097L, 0xFF909CADL, 0xFF90AD9EL, 0xFFADA290L, 0xFFA190ADL), // hsl(h 15% 62%)
        line = longArrayOf(0xFF5A3F46L, 0xFF3F4A5AL, 0xFF3F5A4DL, 0xFF5A503FL, 0xFF4F3F5AL), // hsl(h 18% 30%)
        track = longArrayOf(0xFF624149L, 0xFF414F62L, 0xFF416252L, 0xFF625641L, 0xFF544162L), // hsl(h 20% 32%)
        accent = longArrayOf(0xFFFFA3BAL, 0xFFA3C9FFL, 0xFFA3FFD1L, 0xFFFFDDA3L, 0xFFD9A3FFL), // hsl(h 100% 82%)
        onAccent = longArrayOf(0xFF5C0A1FL, 0xFF0A2C5CL, 0xFF0A5C33L, 0xFF5C3E0AL, 0xFF3A0A5CL), // hsl(h 80% 20%)
        onAccentSub = longArrayOf(0xFF7E253BL, 0xFF254A7EL, 0xFF257E52L, 0xFF7E5E25L, 0xFF59257EL), // hsl(h 55% 32%)
        tonal = longArrayOf(0xFF7B3244L, 0xFF32517BL, 0xFF327B57L, 0xFF7B6032L, 0xFF5D327BL), // hsl(h 42% 34%)
        onTonal = longArrayOf(0xFFFFE0E8L, 0xFFE0EDFFL, 0xFFE0FFF0L, 0xFFFFF4E0L, 0xFFF2E0FFL), // hsl(h 100% 94%)
        tonalSub = longArrayOf(0xFFF4C2CFL, 0xFFC2D7F4L, 0xFFC2F4DBL, 0xFFF4E2C2L, 0xFFDFC2F4L), // hsl(h 70% 86%)
        edge = allHues(0x1FFFFFFFL), // hsl(0 0% 100% / .12)
        scrim = longArrayOf(0x8C0A0507L, 0x8C05070AL, 0x8C050A08L, 0x8C0A0805L, 0x8C08050AL), // hsl(h 30% 3% / .55)
        // 0 20px 48px -10px hsl(h 40% 2% / .8), 0 4px 14px hsl(h 40% 2% / .45)
        shadow = listOf(
            // hsl(h 40% 2% / .8)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -10f,
                color = longArrayOf(0xCC070304L, 0xCC030507L, 0xCC030705L, 0xCC070603L, 0xCC050307L),
            ),
            // hsl(h 40% 2% / .45)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 4f, blur = 14f, spread = 0f,
                color = longArrayOf(0x73070304L, 0x73030507L, 0x73030705L, 0x73070603L, 0x73050307L),
            ),
        ),
    )

    private val contrastLight = BentoSchemePalette(
        bg = allHues(0xFFFFFFFFL), // hsl(0 0% 100%)
        surface = longArrayOf(0xFFF6F4F4L, 0xFFF4F5F6L, 0xFFF4F6F5L, 0xFFF6F5F4L, 0xFFF5F4F6L), // hsl(h 10% 96%)
        raised = longArrayOf(0xFFEAE6E7L, 0xFFE6E8EAL, 0xFFE6EAE8L, 0xFFEAE9E6L, 0xFFE8E6EAL), // hsl(h 10% 91%)
        high = longArrayOf(0xFFE3DDDFL, 0xFFDDE0E3L, 0xFFDDE3E0L, 0xFFE3E1DDL, 0xFFE1DDE3L), // hsl(h 10% 88%)
        `float` = allHues(0xFFFFFFFFL), // hsl(0 0% 100%)
        fg = allHues(0xFF000000L), // hsl(0 0% 0%)
        sub = allHues(0xFF262626L), // hsl(0 0% 15%)
        muted = allHues(0xFF525252L), // hsl(0 0% 32%)
        line = allHues(0xFF8C8C8CL), // hsl(0 0% 55%)
        track = allHues(0xFFB2B2B2L), // hsl(0 0% 70%)
        accent = longArrayOf(0xFF8A0F2EL, 0xFF0F428AL, 0xFF0F8A4DL, 0xFF8A5D0FL, 0xFF570F8AL), // hsl(h 80% 30%)
        onAccent = allHues(0xFFFFFFFFL), // hsl(0 0% 100%)
        onAccentSub = longArrayOf(0xFFF3CED7L, 0xFFCEDDF3L, 0xFFCEF3E0L, 0xFFF3E5CEL, 0xFFE3CEF3L), // hsl(h 60% 88%)
        tonal = longArrayOf(0xFFEDC9D2L, 0xFFC9D8EDL, 0xFFC9EDDBL, 0xFFEDE0C9L, 0xFFDEC9EDL), // hsl(h 50% 86%)
        onTonal = longArrayOf(0xFF370612L, 0xFF061B37L, 0xFF06371FL, 0xFF372506L, 0xFF230637L), // hsl(h 80% 12%)
        tonalSub = longArrayOf(0xFF592633L, 0xFF263B59L, 0xFF265940L, 0xFF594726L, 0xFF442659L), // hsl(h 40% 25%)
        edge = allHues(0x59000000L), // hsl(0 0% 0% / .35)
        scrim = longArrayOf(0x591F1417L, 0x5914191FL, 0x59141F1AL, 0x591F1B14L, 0x591A141FL), // hsl(h 20% 10% / .35)
        // 0 20px 48px -12px hsl(h 30% 20% / .32), 0 3px 10px hsl(h 30% 20% / .12)
        shadow = listOf(
            // hsl(h 30% 20% / .32)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -12f,
                color = longArrayOf(0x5242242BL, 0x52243042L, 0x52244233L, 0x52423724L, 0x52362442L),
            ),
            // hsl(h 30% 20% / .12)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 3f, blur = 10f, spread = 0f,
                color = longArrayOf(0x1F42242BL, 0x1F243042L, 0x1F244233L, 0x1F423724L, 0x1F362442L),
            ),
        ),
    )

    private val contrastDark = BentoSchemePalette(
        bg = allHues(0xFF000000L), // hsl(0 0% 0%)
        surface = longArrayOf(0xFF181616L, 0xFF161718L, 0xFF161817L, 0xFF181716L, 0xFF171618L), // hsl(h 6% 9%)
        raised = longArrayOf(0xFF2B2628L, 0xFF26282BL, 0xFF262B29L, 0xFF2B2926L, 0xFF29262BL), // hsl(h 6% 16%)
        high = longArrayOf(0xFF363031L, 0xFF303236L, 0xFF303633L, 0xFF363430L, 0xFF343036L), // hsl(h 6% 20%)
        `float` = longArrayOf(0xFF262223L, 0xFF222326L, 0xFF222624L, 0xFF262422L, 0xFF242226L), // hsl(h 6% 14%)
        fg = allHues(0xFFFFFFFFL), // hsl(0 0% 100%)
        sub = allHues(0xFFE0E0E0L), // hsl(0 0% 88%)
        muted = allHues(0xFFB8B8B8L), // hsl(0 0% 72%)
        line = allHues(0xFF6B6B6BL), // hsl(0 0% 42%)
        track = allHues(0xFF666666L), // hsl(0 0% 40%)
        accent = longArrayOf(0xFFFF99B2L, 0xFF99C3FFL, 0xFF99FFCCL, 0xFFFFDA99L, 0xFFD499FFL), // hsl(h 100% 80%)
        onAccent = allHues(0xFF000000L), // hsl(0 0% 0%)
        onAccentSub = longArrayOf(0xFF471F29L, 0xFF1F3047L, 0xFF1F4733L, 0xFF47381FL, 0xFF361F47L), // hsl(h 40% 20%)
        tonal = longArrayOf(0xFF5D323DL, 0xFF32445DL, 0xFF325D47L, 0xFF5D4D32L, 0xFF4B325DL), // hsl(h 30% 28%)
        onTonal = allHues(0xFFFFFFFFL), // hsl(0 0% 100%)
        tonalSub = longArrayOf(0xFFF0DBE0L, 0xFFDBE4F0L, 0xFFDBF0E6L, 0xFFF0E8DBL, 0xFFE7DBF0L), // hsl(h 40% 90%)
        edge = allHues(0x66FFFFFFL), // hsl(0 0% 100% / .4)
        scrim = longArrayOf(0x8C0A0507L, 0x8C05070AL, 0x8C050A08L, 0x8C0A0805L, 0x8C08050AL), // hsl(h 30% 3% / .55)
        // 0 20px 48px -10px hsl(h 40% 2% / .8), 0 4px 14px hsl(h 40% 2% / .45)
        shadow = listOf(
            // hsl(h 40% 2% / .8)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -10f,
                color = longArrayOf(0xCC070304L, 0xCC030507L, 0xCC030705L, 0xCC070603L, 0xCC050307L),
            ),
            // hsl(h 40% 2% / .45)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 4f, blur = 14f, spread = 0f,
                color = longArrayOf(0x73070304L, 0x73030507L, 0x73030705L, 0x73070603L, 0x73050307L),
            ),
        ),
    )

    private val contrastBlack = BentoSchemePalette(
        bg = allHues(0xFF000000L), // hsl(0 0% 0%)
        surface = longArrayOf(0xFF161314L, 0xFF131416L, 0xFF131614L, 0xFF161513L, 0xFF151316L), // hsl(h 8% 8%)
        raised = longArrayOf(0xFF231F20L, 0xFF1F2123L, 0xFF1F2321L, 0xFF23221FL, 0xFF221F23L), // hsl(h 7% 13%)
        high = longArrayOf(0xFF312B2CL, 0xFF2B2D31L, 0xFF2B312EL, 0xFF312F2BL, 0xFF2E2B31L), // hsl(h 7% 18%)
        `float` = longArrayOf(0xFF292425L, 0xFF242629L, 0xFF242926L, 0xFF292724L, 0xFF272429L), // hsl(h 7% 15%)
        fg = allHues(0xFFFFFFFFL), // hsl(0 0% 100%)
        sub = allHues(0xFFE0E0E0L), // hsl(0 0% 88%)
        muted = allHues(0xFFB8B8B8L), // hsl(0 0% 72%)
        line = allHues(0xFF6B6B6BL), // hsl(0 0% 42%)
        track = allHues(0xFF666666L), // hsl(0 0% 40%)
        accent = longArrayOf(0xFFFF99B2L, 0xFF99C3FFL, 0xFF99FFCCL, 0xFFFFDA99L, 0xFFD499FFL), // hsl(h 100% 80%)
        onAccent = allHues(0xFF000000L), // hsl(0 0% 0%)
        onAccentSub = longArrayOf(0xFF471F29L, 0xFF1F3047L, 0xFF1F4733L, 0xFF47381FL, 0xFF361F47L), // hsl(h 40% 20%)
        tonal = longArrayOf(0xFF5D323DL, 0xFF32445DL, 0xFF325D47L, 0xFF5D4D32L, 0xFF4B325DL), // hsl(h 30% 28%)
        onTonal = allHues(0xFFFFFFFFL), // hsl(0 0% 100%)
        tonalSub = longArrayOf(0xFFF0DBE0L, 0xFFDBE4F0L, 0xFFDBF0E6L, 0xFFF0E8DBL, 0xFFE7DBF0L), // hsl(h 40% 90%)
        edge = allHues(0x66FFFFFFL), // hsl(0 0% 100% / .4)
        scrim = longArrayOf(0x8C0A0507L, 0x8C05070AL, 0x8C050A08L, 0x8C0A0805L, 0x8C08050AL), // hsl(h 30% 3% / .55)
        // 0 20px 48px -10px hsl(h 40% 2% / .8), 0 4px 14px hsl(h 40% 2% / .45)
        shadow = listOf(
            // hsl(h 40% 2% / .8)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 20f, blur = 48f, spread = -10f,
                color = longArrayOf(0xCC070304L, 0xCC030507L, 0xCC030705L, 0xCC070603L, 0xCC050307L),
            ),
            // hsl(h 40% 2% / .45)
            BentoShadowLayerTable(
                offsetX = 0f, offsetY = 4f, blur = 14f, spread = 0f,
                color = longArrayOf(0x73070304L, 0x73030507L, 0x73030705L, 0x73070603L, 0x73050307L),
            ),
        ),
    )

    private val palettes: Map<BentoColorScheme, Map<BentoColorMode, BentoSchemePalette>> = mapOf(
        BentoColorScheme.TINTED to mapOf(
            BentoColorMode.LIGHT to tintedLight,
            BentoColorMode.DARK to tintedDark,
            BentoColorMode.BLACK to tintedBlack,
        ),
        BentoColorScheme.NEUTRAL to mapOf(
            BentoColorMode.LIGHT to neutralLight,
            BentoColorMode.DARK to neutralDark,
            BentoColorMode.BLACK to neutralBlack,
        ),
        BentoColorScheme.VIVID to mapOf(
            BentoColorMode.LIGHT to vividLight,
            BentoColorMode.DARK to vividDark,
            BentoColorMode.BLACK to vividBlack,
        ),
        BentoColorScheme.CONTRAST to mapOf(
            BentoColorMode.LIGHT to contrastLight,
            BentoColorMode.DARK to contrastDark,
            BentoColorMode.BLACK to contrastBlack,
        ),
    )
}

/**
 * One role at [hueDegrees]: a hue-independent role (the same value for every
 * primary hue) is returned as is; otherwise the saturation, lightness and
 * alpha of its first hue are kept and the hue replaced.
 */
internal fun rehueBentoRole(values: LongArray, hueDegrees: Float): Color {
    val base = Color(values[0].toInt())
    if (values.all { it == values[0] }) return base
    val (_, saturation, lightness) = bentoHsl(base)
    return Color.hsl(
        hue = ((hueDegrees % 360f) + 360f) % 360f,
        saturation = saturation.coerceIn(0f, 1f),
        lightness = lightness.coerceIn(0f, 1f),
        alpha = base.alpha,
    )
}

/** Hue in degrees, saturation and lightness in 0..1 of an sRGB [color]. */
fun bentoHsl(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val lightness = (max + min) / 2f
    val delta = max - min
    if (delta == 0f) return floatArrayOf(0f, 0f, lightness)
    val saturation = delta / (1f - kotlin.math.abs(2f * lightness - 1f))
    val hue = when (max) {
        r -> 60f * (((g - b) / delta) % 6f)
        g -> 60f * ((b - r) / delta + 2f)
        else -> 60f * ((r - g) / delta + 4f)
    }
    return floatArrayOf((hue + 360f) % 360f, saturation, lightness)
}
