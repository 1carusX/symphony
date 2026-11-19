package io.github.zyrouge.symphony.desktop.player

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class VlcjAudioPlayerTest {
    @TempDir
    lateinit var tempDir: Path

    private val fixture: Path = repoRoot().resolve("app/src/test/resources/audio.mp3")

    @Test
    fun `missing file fails without touching the engine`() {
        val harness = Harness()
        harness.player.play(tempDir.resolve("absent.mp3"))
        assertEquals(PlaybackState.Failed(tempDir.resolve("absent.mp3"), PlaybackFailure.FileNotFound), harness.last)
        assertEquals(0, harness.engineCreations)
    }

    @Test
    fun `directory fails as unreadable`() {
        val harness = Harness()
        harness.player.play(tempDir)
        assertEquals(PlaybackState.Failed(tempDir, PlaybackFailure.Unreadable), harness.last)
        assertEquals(0, harness.engineCreations)
    }

    @Test
    fun `unknown extension fails as unsupported`() {
        val file = Files.createFile(tempDir.resolve("notes.txt"))
        val harness = Harness()
        harness.player.play(file)
        assertEquals(PlaybackState.Failed(file, PlaybackFailure.UnsupportedFormat), harness.last)
        assertEquals(0, harness.engineCreations)
    }

    @Test
    fun `valid file goes through loading and reaches playing`() {
        val harness = Harness()
        harness.player.play(fixture)
        assertEquals(PlaybackState.Loading(fixture), harness.last)
        assertEquals(listOf(fixture), harness.engine.started)

        harness.engine.listener.onPlaying()
        assertEquals(PlaybackState.Playing(fixture), harness.last)
    }

    @Test
    fun `engine refusing to start fails the playback`() {
        val harness = Harness(engine = FakeAudioEngine(startResult = false))
        harness.player.play(fixture)
        assertEquals(PlaybackState.Failed(fixture, PlaybackFailure.PlaybackError), harness.last)
    }

    @Test
    fun `engine start throwing fails the playback`() {
        val harness = Harness(engine = FakeAudioEngine(throwOnStart = true))
        harness.player.play(fixture)
        assertEquals(PlaybackState.Failed(fixture, PlaybackFailure.PlaybackError), harness.last)
    }

    @Test
    fun `missing native library fails as backend unavailable`() {
        val states = mutableListOf<PlaybackState>()
        val player = VlcjAudioPlayer(
            engineFactory = { throw UnsatisfiedLinkError("libvlc missing") },
            onStateChanged = { states += it },
        )
        player.play(fixture)
        assertEquals(PlaybackState.Failed(fixture, PlaybackFailure.BackendUnavailable), states.last())
    }

    @Test
    fun `engine error event fails the playback`() {
        val harness = Harness()
        harness.player.play(fixture)
        harness.engine.listener.onPlaying()
        harness.engine.listener.onError()
        assertEquals(PlaybackState.Failed(fixture, PlaybackFailure.PlaybackError), harness.last)
    }

    @Test
    fun `engine end event reaches ended`() {
        val harness = Harness()
        harness.player.play(fixture)
        harness.engine.listener.onPlaying()
        harness.engine.listener.onEnded()
        assertEquals(PlaybackState.Ended(fixture), harness.last)
    }

    @Test
    fun `pause and resume drive the engine once each`() {
        val harness = Harness()
        harness.player.play(fixture)
        harness.engine.listener.onPlaying()

        harness.player.pause()
        assertEquals(PlaybackState.Paused(fixture), harness.last)
        assertEquals(1, harness.engine.pauseCount)

        harness.player.resume()
        assertEquals(PlaybackState.Playing(fixture), harness.last)
        assertEquals(1, harness.engine.resumeCount)
    }

    @Test
    fun `pause while idle is a no-op`() {
        val harness = Harness()
        harness.player.pause()
        assertEquals(PlaybackState.Idle, harness.last)
        assertEquals(0, harness.engine.pauseCount)
    }

    @Test
    fun `stop returns to idle and stops the engine`() {
        val harness = Harness()
        harness.player.play(fixture)
        harness.engine.listener.onPlaying()
        harness.player.stop()
        assertEquals(PlaybackState.Idle, harness.last)
        assertEquals(1, harness.engine.stopCount)
    }

    @Test
    fun `seek and volume are clamped before reaching the engine`() {
        val harness = Harness()
        harness.player.play(fixture)
        harness.player.seekTo(-40)
        harness.player.setVolume(180)
        assertEquals(0, harness.engine.lastSeek)
        assertEquals(100, harness.engine.lastVolume)
    }

    @Test
    fun `close releases the engine and returns to idle`() {
        val harness = Harness()
        harness.player.play(fixture)
        harness.engine.listener.onPlaying()
        harness.player.close()
        assertTrue(harness.engine.released)
        assertEquals(PlaybackState.Idle, harness.last)
    }

    private class Harness(engine: FakeAudioEngine = FakeAudioEngine()) {
        val engine = engine
        val states = mutableListOf<PlaybackState>()
        var engineCreations = 0
        val player = VlcjAudioPlayer(
            engineFactory = { listener ->
                engineCreations += 1
                engine.listener = listener
                engine
            },
            onStateChanged = { states += it },
        )
        val last: PlaybackState
            get() = states.lastOrNull() ?: PlaybackState.Idle
    }
}

internal class FakeAudioEngine(
    private val startResult: Boolean = true,
    private val throwOnStart: Boolean = false,
) : AudioEngine {
    lateinit var listener: AudioEngineListener
    val started = mutableListOf<Path>()
    var pauseCount = 0
    var resumeCount = 0
    var stopCount = 0
    var lastSeek = -1L
    var lastVolume = -1
    var released = false

    override fun start(path: Path): Boolean {
        if (throwOnStart) error("engine refused")
        started.add(path)
        return startResult
    }

    override fun pause() {
        pauseCount += 1
    }

    override fun resume() {
        resumeCount += 1
    }

    override fun stop() {
        stopCount += 1
    }

    override fun seekTo(positionMs: Long) {
        lastSeek = positionMs
    }

    override fun setVolume(percent: Int) {
        lastVolume = percent
    }

    override fun positionMs(): Long = 0L

    override fun durationMs(): Long = 0L

    override fun release() {
        released = true
    }
}

internal fun repoRoot(): Path {
    var current = Path.of("").toAbsolutePath().normalize()
    while (true) {
        if (Files.exists(current.resolve("settings.gradle.kts"))) return current
        current = current.parent ?: error("repository root not found from ${Path.of("").toAbsolutePath()}")
    }
}
