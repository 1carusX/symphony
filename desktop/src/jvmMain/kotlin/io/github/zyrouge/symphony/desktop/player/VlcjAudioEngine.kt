package io.github.zyrouge.symphony.desktop.player

import com.sun.jna.Library
import com.sun.jna.Native
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import java.nio.file.Path

class VlcjAudioEngine(
    private val eventListener: AudioEngineListener,
) : AudioEngine {
    private val factory = createFactory()
    private val player: MediaPlayer = factory.mediaPlayers().newMediaPlayer()

    init {
        player.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
            override fun playing(mediaPlayer: MediaPlayer) = eventListener.onPlaying()
            override fun paused(mediaPlayer: MediaPlayer) = eventListener.onPaused()
            override fun finished(mediaPlayer: MediaPlayer) = eventListener.onEnded()
            override fun error(mediaPlayer: MediaPlayer) = eventListener.onError()
        })
    }

    override fun start(path: Path): Boolean = player.media().play(path.toString())

    override fun pause() {
        player.controls().pause()
    }

    override fun resume() {
        player.controls().play()
    }

    override fun stop() {
        player.controls().stop()
    }

    override fun seekTo(positionMs: Long) {
        player.controls().setTime(positionMs)
    }

    override fun setVolume(percent: Int) {
        player.audio().setVolume(percent)
    }

    override fun positionMs(): Long = player.status().time()

    override fun durationMs(): Long = player.status().length()

    override fun release() {
        player.release()
        factory.release()
    }

    private companion object {
        fun createFactory(): MediaPlayerFactory {
            val bundled = System.getProperty("symphony.vlc.path")?.takeIf { it.isNotBlank() }
                ?: System.getenv("SYMPHONY_VLC_PATH")?.takeIf { it.isNotBlank() }
            if (bundled == null) {
                return MediaPlayerFactory("--quiet")
            }
            System.setProperty("jna.library.path", bundled)
            LibC.setenv("VLC_PLUGIN_PATH", "$bundled/../vlc/plugins", 1)
            return MediaPlayerFactory("--quiet")
        }
    }

    private interface LibC : Library {
        fun setenv(name: String, value: String, overwrite: Int): Int

        companion object : LibC by Native.load("c", LibC::class.java)
    }
}
