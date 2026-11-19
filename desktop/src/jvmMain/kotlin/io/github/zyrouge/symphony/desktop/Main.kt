package io.github.zyrouge.symphony.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Symphony Desktop",
    ) {
    }
}
