package io.github.zyrouge.symphony.ui.theme.bento

import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.enums.enumEntries

class BentoColorRolesTest {

    @ParameterizedTest(name = "[{index}] scheme={0} mode={1} hue={2}")
    @MethodSource("colorCombinations")
    fun `resolve matches the hex values recorded in the design tokens`(
        scheme: BentoColorScheme,
        mode: BentoColorMode,
        hue: BentoPrimaryHue,
    ) {
        val colors = BentoColorRoles.resolve(scheme, mode, hue)
        val modeNode = schemeNode(scheme, mode)

        assertEquals(expectedArgb(modeNode["bg"]!!.jsonObject, hue), colors.bg.toArgb())
        assertEquals(expectedArgb(modeNode["surface"]!!.jsonObject, hue), colors.surface.toArgb())
        assertEquals(expectedArgb(modeNode["raised"]!!.jsonObject, hue), colors.raised.toArgb())
        assertEquals(expectedArgb(modeNode["high"]!!.jsonObject, hue), colors.high.toArgb())
        assertEquals(expectedArgb(modeNode["float"]!!.jsonObject, hue), colors.`float`.toArgb())
        assertEquals(expectedArgb(modeNode["fg"]!!.jsonObject, hue), colors.fg.toArgb())
        assertEquals(expectedArgb(modeNode["sub"]!!.jsonObject, hue), colors.sub.toArgb())
        assertEquals(expectedArgb(modeNode["muted"]!!.jsonObject, hue), colors.muted.toArgb())
        assertEquals(expectedArgb(modeNode["line"]!!.jsonObject, hue), colors.line.toArgb())
        assertEquals(expectedArgb(modeNode["track"]!!.jsonObject, hue), colors.track.toArgb())
        assertEquals(expectedArgb(modeNode["accent"]!!.jsonObject, hue), colors.accent.toArgb())
        assertEquals(expectedArgb(modeNode["on-accent"]!!.jsonObject, hue), colors.onAccent.toArgb())
        assertEquals(expectedArgb(modeNode["on-accent-sub"]!!.jsonObject, hue), colors.onAccentSub.toArgb())
        assertEquals(expectedArgb(modeNode["tonal"]!!.jsonObject, hue), colors.tonal.toArgb())
        assertEquals(expectedArgb(modeNode["on-tonal"]!!.jsonObject, hue), colors.onTonal.toArgb())
        assertEquals(expectedArgb(modeNode["tonal-sub"]!!.jsonObject, hue), colors.tonalSub.toArgb())
        assertEquals(expectedArgb(modeNode["edge"]!!.jsonObject, hue), colors.edge.toArgb())
        assertEquals(expectedArgb(modeNode["scrim"]!!.jsonObject, hue), colors.scrim.toArgb())

        val layersNode = modeNode["shadow"]!!.jsonObject.jsonObject["layers"]!!.jsonArray
        assertEquals(layersNode.size, colors.shadow.layers.size)
        colors.shadow.layers.forEachIndexed { index, layer ->
            val layerNode = layersNode[index].jsonObject
            assertEquals(layerNode["x"]!!.jsonPrimitive.content.toFloat(), layer.offsetX)
            assertEquals(layerNode["y"]!!.jsonPrimitive.content.toFloat(), layer.offsetY)
            assertEquals(layerNode["blur"]!!.jsonPrimitive.content.toFloat(), layer.blur)
            assertEquals(layerNode["spread"]!!.jsonPrimitive.content.toFloat(), layer.spread)
            assertEquals(
                expectedArgb(layerNode["color"]!!.jsonObject, hue),
                layer.color.toArgb(),
            )
        }
    }

    @Test
    fun `phone sheet shadow matches the elevation token`() {
        val scrimNode = schemeNode(BentoColorScheme.TINTED, BentoColorMode.DARK)["scrim"]!!.jsonObject
        for (hue in enumEntries<BentoPrimaryHue>()) {
            val colors = BentoColorRoles.resolve(BentoColorScheme.TINTED, BentoColorMode.DARK, hue)
            // Same HSL as the dark scrim with alpha 0.35 (0x59) instead of 0.55.
            val rgb = expectedArgb(scrimNode, hue) and 0xFFFFFF
            assertEquals((0x59 shl 24) or rgb, colors.phoneSheetShadow.color.toArgb())
            assertEquals(0f, colors.phoneSheetShadow.offsetX)
            assertEquals(-6f, colors.phoneSheetShadow.offsetY)
            assertEquals(28f, colors.phoneSheetShadow.blur)
            assertEquals(0f, colors.phoneSheetShadow.spread)
        }
    }

    private fun schemeNode(scheme: BentoColorScheme, mode: BentoColorMode): JsonObject {
        val colorNode = tokens["color"]!!.jsonObject
        return colorNode[scheme.name.lowercase()]!!.jsonObject[mode.name.lowercase()]!!.jsonObject
    }

    private fun expectedArgb(roleNode: JsonObject, hue: BentoPrimaryHue): Int {
        val hexNode = roleNode["hex"]!!
        val hex = (if (hexNode is JsonObject) hexNode[hue.name.lowercase()]!! else hexNode)
            .jsonPrimitive.content
        return parseCssHexArgb(hex)
    }

    private fun parseCssHexArgb(hex: String): Int {
        val value = hex.removePrefix("#")
        return when (value.length) {
            6 -> ((0xFFL shl 24) or value.toLong(16)).toInt()
            8 -> {
                val rgb = value.take(6).toLong(16)
                val alpha = value.substring(6).toLong(16)
                ((alpha shl 24) or rgb).toInt()
            }
            else -> error("unexpected hex value: $hex")
        }
    }

    private val tokens: JsonObject by lazy {
        val stream = javaClass.classLoader.getResourceAsStream("bento-tokens.json")
            ?: error("bento-tokens.json is missing from the test resources")
        Json.parseToJsonElement(stream.readBytes().decodeToString()).jsonObject
    }

    companion object {
        @JvmStatic
        fun colorCombinations(): Stream<Arguments> =
            enumEntries<BentoColorScheme>().flatMap { scheme ->
                enumEntries<BentoColorMode>().flatMap { mode ->
                    enumEntries<BentoPrimaryHue>().map { hue ->
                        Arguments.of(scheme, mode, hue)
                    }
                }
            }.stream()
    }
}
