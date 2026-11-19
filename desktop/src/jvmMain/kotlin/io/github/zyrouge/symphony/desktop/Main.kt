package io.github.zyrouge.symphony.desktop

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.zyrouge.symphony.desktop.ui.DesktopApp
import java.nio.file.Path

fun main(args: Array<String>) {
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
