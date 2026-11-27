package io.github.zyrouge.symphony.desktop.ui

import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import java.nio.file.Files
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DesktopSettingsStoreTest {
    @Test
    fun `settings round trip through the store`() {
        val directory = Files.createTempDirectory("symphony-settings")
        try {
            val store = DesktopSettingsStore(directory.resolve("settings.properties"))
            val settings = DesktopSettings(
                themeMode = BentoThemeMode.DARK,
                colorScheme = BentoColorScheme.CONTRAST,
                primaryHue = BentoPrimaryHue.BLUE,
                fontScale = 1.15f,
                contentScale = 1.3f,
                language = "ja",
            )

            store.save(settings)

            assertEquals(settings, store.load())
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `invalid values fall back to the defaults`() {
        val directory = Files.createTempDirectory("symphony-settings")
        try {
            val file = directory.resolve("settings.properties")
            Files.writeString(
                file,
                """
                themeMode=NOT_A_MODE
                colorScheme=VIVID
                primaryHue=NOT_A_HUE
                """.trimIndent(),
            )
            val settings = DesktopSettingsStore(file).load()
            assertEquals(BentoThemeMode.SYSTEM, settings.themeMode)
            assertEquals(BentoColorScheme.VIVID, settings.colorScheme)
            assertEquals(BentoPrimaryHue.ROSE, settings.primaryHue)
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
