package io.github.zyrouge.symphony.desktop.player

import io.github.zyrouge.symphony.services.groove.FileSystemScanner
import java.nio.file.Files
import java.nio.file.Path

class VlcjAudioPlayer(
    private val engineFactory: (AudioEngineListener) -> AudioEngine = { listener ->
        VlcjAudioEngine(listener)
    },
    private val onStateChanged: (PlaybackState) -> Unit = {},
) : AudioPlayer {
    private val machine = PlaybackStateMachine()
    private val lock = Any()
    private var engine: AudioEngine? = null
    private var currentPath: Path? = null

    private val listener = object : AudioEngineListener {
        override fun onPlaying() = publishForCurrent(PlaybackEvent::PlayStarted)
        override fun onPaused() = publishForCurrent(PlaybackEvent::PauseRequested)
        override fun onEnded() = publishForCurrent(PlaybackEvent::ReachedEnd)
        override fun onError() = publishForCurrent { path ->
            PlaybackEvent.Failure(path, PlaybackFailure.PlaybackError)
        }
    }

    override val state: PlaybackState
        get() = synchronized(lock) { machine.state }

    override fun play(path: Path) {
        val absolute = path.toAbsolutePath().normalize()
        if (!Files.exists(absolute)) {
            fail(absolute, PlaybackFailure.FileNotFound)
            return
        }
        if (!Files.isRegularFile(absolute) || !Files.isReadable(absolute)) {
            fail(absolute, PlaybackFailure.Unreadable)
            return
        }
        if (absolute.extensionLower() !in FileSystemScanner.audioExtensions) {
            fail(absolute, PlaybackFailure.UnsupportedFormat)
            return
        }

        synchronized(lock) {
            currentPath = absolute
            machine.accept(PlaybackEvent.LoadStarted(absolute))
        }
        notifyState()

        val active = synchronized(lock) { engine } ?: createEngine() ?: run {
            fail(absolute, PlaybackFailure.BackendUnavailable)
            return
        }

        val started = try {
            active.start(absolute)
        } catch (error: RuntimeException) {
            fail(absolute, PlaybackFailure.PlaybackError)
            return
        }
        if (!started) {
            fail(absolute, PlaybackFailure.PlaybackError)
        }
    }

    override fun pause() {
        val path = synchronized(lock) { currentPath } ?: return
        if (state !is PlaybackState.Playing) return
        synchronized(lock) { engine }?.pause()
        publish(PlaybackEvent.PauseRequested(path))
    }

    override fun resume() {
        val path = synchronized(lock) { currentPath } ?: return
        if (state !is PlaybackState.Paused) return
        synchronized(lock) { engine }?.resume()
        publish(PlaybackEvent.PlayStarted(path))
    }

    override fun stop() {
        synchronized(lock) { engine }?.stop()
        publish(PlaybackEvent.Stopped)
    }

    override fun seekTo(positionMs: Long) {
        synchronized(lock) { engine }?.seekTo(positionMs.coerceAtLeast(0L))
    }

    override fun setVolume(percent: Int) {
        synchronized(lock) { engine }?.setVolume(percent.coerceIn(0, 100))
    }

    override fun positionMs(): Long = synchronized(lock) { engine?.positionMs() } ?: 0L

    override fun durationMs(): Long = synchronized(lock) { engine?.durationMs() } ?: 0L

    override fun close() {
        val active = synchronized(lock) {
            val current = engine
            engine = null
            current
        }
        active?.release()
        publish(PlaybackEvent.Stopped)
    }

    private fun createEngine(): AudioEngine? {
        val created = try {
            engineFactory(listener)
        } catch (error: Throwable) {
            null
        }
        if (created != null) {
            synchronized(lock) { engine = created }
        }
        return created
    }

    private fun publish(event: PlaybackEvent) {
        synchronized(lock) { machine.accept(event) }
        notifyState()
    }

    private fun publishForCurrent(create: (Path) -> PlaybackEvent) {
        val path = synchronized(lock) { currentPath } ?: return
        publish(create(path))
    }

    private fun fail(path: Path?, reason: PlaybackFailure) {
        publish(PlaybackEvent.Failure(path, reason))
    }

    private fun notifyState() {
        onStateChanged(state)
    }

    private fun Path.extensionLower(): String =
        fileName.toString().substringAfterLast('.', "").lowercase()
}
