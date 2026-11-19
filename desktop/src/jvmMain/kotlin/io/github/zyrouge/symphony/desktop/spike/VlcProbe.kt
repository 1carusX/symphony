package io.github.zyrouge.symphony.desktop.spike

import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.base.MediaPlayer
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val dir = File(args[0])
    val files = dir.listFiles()!!.filter { it.isFile }.sortedBy { it.name }
    val discovered = NativeDiscovery().discover()
    println("libvlc discovered=$discovered")
    val factory = MediaPlayerFactory("--quiet")
    val player: MediaPlayer = factory.mediaPlayers().newMediaPlayer()
    try {
        for (file in files) {
            try {
                val started = player.media().play(file.absolutePath)
                val deadline = System.currentTimeMillis() + 8_000
                var maxTime = 0L
                var playingSeen = false
                while (System.currentTimeMillis() < deadline) {
                    Thread.sleep(50)
                    val time = player.status().time()
                    if (time > maxTime) maxTime = time
                    if (player.status().isPlaying) playingSeen = true
                    if (maxTime > 200) break
                }
                val length = player.status().length()
                if (maxTime > 200) {
                    println("OK file=${file.name} lengthMs=$length maxTimeMs=$maxTime playing=$playingSeen")
                } else {
                    println(
                        "FAIL file=${file.name} lengthMs=$length maxTimeMs=$maxTime " +
                            "playing=$playingSeen started=$started",
                    )
                }
                player.controls().stop()
                Thread.sleep(300)
            } catch (error: Exception) {
                println("FAIL file=${file.name} error=${error.javaClass.simpleName}: ${error.message}")
            }
        }
    } finally {
        player.release()
        factory.release()
    }
    exitProcess(0)
}
