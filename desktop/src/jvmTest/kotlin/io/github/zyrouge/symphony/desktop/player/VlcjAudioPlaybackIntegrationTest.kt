package io.github.zyrouge.symphony.desktop.player

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery
import java.nio.file.Path
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class VlcjAudioPlaybackIntegrationTest {
    private val fixture: Path = repoRoot().resolve("app/src/test/resources/audio.mp3")

    @Test
    fun `plays a fixture to the end`() {
        assumeTrue(libvlcAvailable(), "libvlc is not installed")
        val states = LinkedBlockingQueue<PlaybackState>()
        val player = VlcjAudioPlayer(onStateChanged = { states.put(it) })
        try {
            player.play(fixture)
            assertTrue(
                awaitState(states, PlaybackState.Playing(fixture), 20_000),
                "never reached Playing, state=${player.state}",
            )
            assertTrue(player.durationMs() > 0, "engine reported no duration")
            assertTrue(
                awaitState(states, PlaybackState.Ended(fixture), 60_000),
                "never reached Ended, state=${player.state}",
            )
        } finally {
            player.close()
        }
    }

    @Test
    fun `pause and resume keep the loaded track`() {
        assumeTrue(libvlcAvailable(), "libvlc is not installed")
        val states = LinkedBlockingQueue<PlaybackState>()
        val player = VlcjAudioPlayer(onStateChanged = { states.put(it) })
        try {
            player.play(fixture)
            assertTrue(
                awaitState(states, PlaybackState.Playing(fixture), 20_000),
                "never reached Playing, state=${player.state}",
            )
            player.pause()
            assertTrue(
                awaitState(states, PlaybackState.Paused(fixture), 20_000),
                "never reached Paused, state=${player.state}",
            )
            player.resume()
            assertTrue(
                awaitState(states, PlaybackState.Playing(fixture), 20_000),
                "never resumed, state=${player.state}",
            )
            player.stop()
            assertTrue(
                awaitState(states, PlaybackState.Idle, 20_000),
                "never stopped, state=${player.state}",
            )
        } finally {
            player.close()
        }
    }

    private fun awaitState(
        states: LinkedBlockingQueue<PlaybackState>,
        expected: PlaybackState,
        timeoutMs: Long,
    ): Boolean {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (true) {
            val remaining = deadline - System.currentTimeMillis()
            if (remaining <= 0) return false
            val seen = states.poll(remaining, TimeUnit.MILLISECONDS) ?: return false
            if (seen == expected) return true
        }
    }
}

private fun libvlcAvailable(): Boolean = try {
    NativeDiscovery().discover()
} catch (error: Throwable) {
    false
}
