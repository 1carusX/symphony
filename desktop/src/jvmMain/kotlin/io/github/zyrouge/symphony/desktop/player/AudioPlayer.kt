package io.github.zyrouge.symphony.desktop.player

import java.nio.file.Path

interface AudioPlayer {
    val state: PlaybackState

    fun play(path: Path)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setVolume(percent: Int)
    fun positionMs(): Long
    fun durationMs(): Long
    fun close()
}
