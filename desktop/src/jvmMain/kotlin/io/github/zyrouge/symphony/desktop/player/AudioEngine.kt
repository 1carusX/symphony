package io.github.zyrouge.symphony.desktop.player

import java.nio.file.Path

interface AudioEngineListener {
    fun onPlaying()
    fun onPaused()
    fun onEnded()
    fun onError()
}

interface AudioEngine {
    fun start(path: Path): Boolean
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setVolume(percent: Int)
    fun positionMs(): Long
    fun durationMs(): Long
    fun release()
}
