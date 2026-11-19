package io.github.zyrouge.symphony.desktop.player

import java.nio.file.Path

fun main(args: Array<String>) {
    val file = Path.of(args[0]).toAbsolutePath().normalize()
    val player = VlcjAudioPlayer(onStateChanged = { state -> println("state=$state") })
    try {
        player.play(file)
        repeat(200) {
            Thread.sleep(100)
            val state = player.state
            if (state is PlaybackState.Ended || state is PlaybackState.Failed) {
                println("final state=$state positionMs=${player.positionMs()} durationMs=${player.durationMs()}")
                return
            }
            if (it % 10 == 0) {
                println("tick positionMs=${player.positionMs()} durationMs=${player.durationMs()} state=$state")
            }
        }
        player.pause()
        println("paused state=${player.state}")
        Thread.sleep(500)
        player.resume()
        println("resumed state=${player.state}")
        Thread.sleep(1000)
        player.stop()
        println("stopped state=${player.state}")
    } finally {
        player.close()
    }
}
