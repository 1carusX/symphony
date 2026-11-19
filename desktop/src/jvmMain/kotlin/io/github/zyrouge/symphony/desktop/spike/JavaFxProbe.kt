package io.github.zyrouge.symphony.desktop.spike

import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val dir = File(args[0])
    val files = dir.listFiles()!!.filter { it.isFile }.sortedBy { it.name }

    Platform.startup {
        probeNext(files, 0)
    }
}

private fun probeNext(files: List<File>, index: Int) {
    if (index >= files.size) {
        Platform.exit()
        return
    }
    val file = files[index]
    val done = CountDownLatch(1)
    var player: MediaPlayer? = null
    try {
        val media = Media(file.toURI().toString())
        player = MediaPlayer(media)
        player.setOnReady {
            val durationMs = media.duration.toMillis().toLong()
            println("OK file=${file.name} durationMs=$durationMs")
            done.countDown()
        }
        player.setOnError {
            println("FAIL file=${file.name} error=${player.error?.message}")
            done.countDown()
        }
        media.errorProperty().addListener { _, _, error ->
            if (error != null) {
                println("FAIL file=${file.name} mediaError=${error.message}")
                done.countDown()
            }
        }
    } catch (error: Exception) {
        println("FAIL file=${file.name} error=${error.javaClass.simpleName}: ${error.message}")
        probeNext(files, index + 1)
        return
    }

    val playerRef = player
    Thread {
        done.await(15, TimeUnit.SECONDS)
        Platform.runLater {
            playerRef.dispose()
            probeNext(files, index + 1)
        }
    }.apply { isDaemon = true }.start()
}
