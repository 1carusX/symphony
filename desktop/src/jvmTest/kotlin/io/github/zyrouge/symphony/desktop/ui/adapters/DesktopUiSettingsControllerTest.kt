package io.github.zyrouge.symphony.desktop.ui.adapters

import io.github.zyrouge.symphony.desktop.ui.DesktopSettings
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoPrimaryHue
import io.github.zyrouge.symphony.ui.theme.bento.BentoThemeMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DesktopUiSettingsControllerTest {
    @Test
    fun `every change is persisted`() {
        val saved = mutableListOf<DesktopSettings>()
        val controller = DesktopUiSettingsController(onChanged = { saved += it })

        controller.setThemeMode(BentoThemeMode.DARK)
        controller.setColorScheme(BentoColorScheme.VIVID)
        controller.setPrimaryHue(BentoPrimaryHue.GREEN)
        controller.setFontScale(1.15f)
        controller.setContentScale(1.3f)
        controller.setLanguage("ja")

        assertEquals(6, saved.size)
        assertEquals(
            DesktopSettings(
                themeMode = BentoThemeMode.DARK,
                colorScheme = BentoColorScheme.VIVID,
                primaryHue = BentoPrimaryHue.GREEN,
                fontScale = 1.15f,
                contentScale = 1.3f,
                language = "ja",
            ),
            saved.last(),
        )
    }
}
