package io.github.zyrouge.symphony.ui.components

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BentoInsetsTest {
    @Test
    fun `android 9 with nothing reported gets the fallback`() {
        assertEquals(BentoLegacyGestureFallback, resolveBentoSystemBottomInset(0.dp, 28))
    }

    @Test
    fun `android 9 keeps a reported navigation bar`() {
        assertEquals(48.dp, resolveBentoSystemBottomInset(48.dp, 28))
    }

    @Test
    fun `android 10 and later trust a reported zero`() {
        assertEquals(0.dp, resolveBentoSystemBottomInset(0.dp, 29))
        assertEquals(0.dp, resolveBentoSystemBottomInset(0.dp, 35))
    }

    @Test
    fun `reported values pass through on recent versions`() {
        assertEquals(16.dp, resolveBentoSystemBottomInset(16.dp, 35))
    }
}
