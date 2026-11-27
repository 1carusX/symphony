package io.github.zyrouge.symphony.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.zyrouge.symphony.desktop.ui.DesktopApp
import io.github.zyrouge.symphony.desktop.ui.DesktopSettingsStore
import io.github.zyrouge.symphony.services.locales.localeTag
import java.nio.file.Path
import java.util.Locale

fun main(args: Array<String>) {
    DesktopSettingsStore().load().language?.let { language ->
        Locale.setDefault(Locale.forLanguageTag(localeTag(language)))
    }
    val initialDirectory = (args.firstOrNull() ?: System.getenv("SYMPHONY_DESKTOP_LIBRARY"))
        ?.takeIf { it.isNotBlank() }
        ?.let { Path.of(it) }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Symphony Desktop",
            state = rememberWindowState(width = 980.dp, height = 660.dp),
        ) {
            DesktopApp(window, initialDirectory)
        }
    }
}
