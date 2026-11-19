package io.github.zyrouge.symphony.desktop.ui

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.InputEvent
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.system.exitProcess

private const val LIBRARY_MARKER = "t5-library.marker"
private const val PLAYING_MARKER = "t5-playing.marker"
private const val EXIT_SIGNAL = "t5-exit.signal"
private const val LIBRARY_CAPTURED = "t5-library.done"
private const val PLAYING_CAPTURED = "t5-playing.done"

fun main(args: Array<String>) {
    val directory = Path.of(args[0])
    val evidence = Path.of(args[1])
    Files.createDirectories(evidence)

    fun marker(name: String, bounds: Rectangle) {
        Files.writeString(evidence.resolve(name), "${bounds.x},${bounds.y},${bounds.width},${bounds.height}")
    }

    fun await(name: String, timeoutMs: Long = 30_000): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (evidence.resolve(name).exists()) return true
            Thread.sleep(100)
        }
        return false
    }

    Thread {
        try {
            Thread.sleep(9_000)
            val window = java.awt.Window.getWindows().firstOrNull { it.isVisible } ?: return@Thread
            val bounds = Rectangle(window.x, window.y, window.width, window.height)
            println("window at ${bounds.x},${bounds.y} ${bounds.width}x${bounds.height}")
            runCatching { window.isAlwaysOnTop = true }

            marker(LIBRARY_MARKER, bounds)
            if (!await(LIBRARY_CAPTURED, 60_000)) {
                println("library capture never acknowledged")
                return@Thread
            }

            val robot = Robot()
            robot.mouseMove(bounds.x + bounds.width / 3, bounds.y + 160)
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
            robot.mouseMove(bounds.x + bounds.width / 2, bounds.y + bounds.height / 3)
            println("clicked first row at ${bounds.x + bounds.width / 3},${bounds.y + 160}")

            Thread.sleep(1_500)
            marker(PLAYING_MARKER, bounds)
            if (!await(PLAYING_CAPTURED, 60_000)) {
                println("playing capture never acknowledged")
                return@Thread
            }
            println("evidence captured")
            await(EXIT_SIGNAL, 60_000)
        } catch (error: Exception) {
            println("ui smoke failed: $error")
        } finally {
            exitProcess(0)
        }
    }.apply { isDaemon = true }.start()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Symphony Desktop",
            state = rememberWindowState(width = 980.dp, height = 660.dp),
        ) {
            DesktopApp(window, directory)
        }
    }
}
