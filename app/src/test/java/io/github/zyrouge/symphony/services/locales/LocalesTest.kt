package io.github.zyrouge.symphony.services.locales

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LocalesTest {
    @Test
    fun `chinese uses the region of its resource qualifier`() {
        assertEquals("zh-CN", localeTag("zh-Hans"))
    }

    @Test
    fun `codes without a region qualifier stay as they are`() {
        assertEquals("de", localeTag("de"))
        assertEquals("ryu", localeTag("ryu"))
    }

    @Test
    fun `unknown codes pass through`() {
        assertEquals("xx", localeTag("xx"))
    }

    @Test
    fun `every declared locale resolves to a tag the platform accepts`() {
        symphonyLocales.forEach { info ->
            val tag = localeTag(info.code)
            assertEquals(tag, java.util.Locale.forLanguageTag(tag).toLanguageTag())
        }
    }

    @Test
    fun `a bare system language finds the declared locale`() {
        assertEquals("中文 (简体)", localeNativeName("zh"))
        assertEquals("Deutsch", localeNativeName("de"))
    }
}
