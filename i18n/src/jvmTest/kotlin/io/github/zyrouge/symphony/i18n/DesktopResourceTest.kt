package io.github.zyrouge.symphony.i18n

import kotlin.test.Test
import kotlin.test.assertTrue

class DesktopResourceTest {
    @Test
    fun `the jvm jar carries the locale chunks`() {
        val loader = javaClass.classLoader
        val root = loader.getResourceAsStream("composeResources/io.github.zyrouge.symphony.i18n.generated/values/strings.commonMain.cvr")
            ?: loader.getResourceAsStream("composeResources/values/strings.commonMain.cvr")
        assertTrue(root != null, "no default locale chunk on the jvm classpath")
        val chinese = loader.getResourceAsStream("composeResources/io.github.zyrouge.symphony.i18n.generated/values-zh-rCN/strings.commonMain.cvr")
        assertTrue(chinese != null, "no chinese chunk on the jvm classpath")
    }
}
