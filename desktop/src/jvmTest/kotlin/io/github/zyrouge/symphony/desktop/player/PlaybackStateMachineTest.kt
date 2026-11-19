package io.github.zyrouge.symphony.desktop.player

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Path

class PlaybackStateMachineTest {
    private val first = Path.of("/music/first.mp3")
    private val second = Path.of("/music/second.mp3")

    @Test
    fun `load from idle enters loading`() {
        val machine = PlaybackStateMachine()
        assertEquals(PlaybackState.Loading(first), machine.accept(PlaybackEvent.LoadStarted(first)))
    }

    @Test
    fun `play after load enters playing`() {
        val machine = PlaybackStateMachine()
        machine.accept(PlaybackEvent.LoadStarted(first))
        assertEquals(PlaybackState.Playing(first), machine.accept(PlaybackEvent.PlayStarted(first)))
    }

    @Test
    fun `pause from playing enters paused`() {
        val machine = playingMachine()
        assertEquals(PlaybackState.Paused(first), machine.accept(PlaybackEvent.PauseRequested(first)))
    }

    @Test
    fun `pause while idle is ignored`() {
        val machine = PlaybackStateMachine()
        assertEquals(PlaybackState.Idle, machine.accept(PlaybackEvent.PauseRequested(first)))
    }

    @Test
    fun `pause while paused is ignored`() {
        val machine = playingMachine()
        machine.accept(PlaybackEvent.PauseRequested(first))
        val before = machine.state
        assertEquals(before, machine.accept(PlaybackEvent.PauseRequested(first)))
    }

    @Test
    fun `resume from paused enters playing`() {
        val machine = playingMachine()
        machine.accept(PlaybackEvent.PauseRequested(first))
        assertEquals(PlaybackState.Playing(first), machine.accept(PlaybackEvent.PlayStarted(first)))
    }

    @Test
    fun `end from playing enters ended`() {
        val machine = playingMachine()
        assertEquals(PlaybackState.Ended(first), machine.accept(PlaybackEvent.ReachedEnd(first)))
    }

    @Test
    fun `end while paused is ignored`() {
        val machine = playingMachine()
        machine.accept(PlaybackEvent.PauseRequested(first))
        assertEquals(PlaybackState.Paused(first), machine.accept(PlaybackEvent.ReachedEnd(first)))
    }

    @Test
    fun `end for a stale path is ignored`() {
        val machine = playingMachine()
        assertEquals(PlaybackState.Playing(first), machine.accept(PlaybackEvent.ReachedEnd(second)))
    }

    @Test
    fun `playing event for a stale path is ignored`() {
        val machine = playingMachine()
        assertEquals(PlaybackState.Playing(first), machine.accept(PlaybackEvent.PlayStarted(second)))
    }

    @Test
    fun `switching tracks while playing enters loading then playing`() {
        val machine = playingMachine()
        assertEquals(PlaybackState.Loading(second), machine.accept(PlaybackEvent.LoadStarted(second)))
        assertEquals(PlaybackState.Playing(second), machine.accept(PlaybackEvent.PlayStarted(second)))
    }

    @Test
    fun `failure records the failing path`() {
        val machine = playingMachine()
        val failure = PlaybackState.Failed(second, PlaybackFailure.UnsupportedFormat)
        assertEquals(failure, machine.accept(PlaybackEvent.Failure(second, PlaybackFailure.UnsupportedFormat)))
    }

    @Test
    fun `failure without a path keeps the active path`() {
        val machine = playingMachine()
        assertEquals(
            PlaybackState.Failed(first, PlaybackFailure.BackendUnavailable),
            machine.accept(PlaybackEvent.Failure(null, PlaybackFailure.BackendUnavailable)),
        )
    }

    @Test
    fun `failure replaces an earlier failure reason`() {
        val machine = PlaybackStateMachine()
        machine.accept(PlaybackEvent.Failure(first, PlaybackFailure.FileNotFound))
        assertEquals(
            PlaybackState.Failed(first, PlaybackFailure.PlaybackError),
            machine.accept(PlaybackEvent.Failure(first, PlaybackFailure.PlaybackError)),
        )
    }

    @Test
    fun `stop returns to idle from loading`() {
        val machine = PlaybackStateMachine()
        machine.accept(PlaybackEvent.LoadStarted(first))
        assertEquals(PlaybackState.Idle, machine.accept(PlaybackEvent.Stopped))
    }

    @Test
    fun `stop returns to idle from playing`() {
        val machine = playingMachine()
        assertEquals(PlaybackState.Idle, machine.accept(PlaybackEvent.Stopped))
    }

    @Test
    fun `stop returns to idle from ended`() {
        val machine = playingMachine()
        machine.accept(PlaybackEvent.ReachedEnd(first))
        assertEquals(PlaybackState.Idle, machine.accept(PlaybackEvent.Stopped))
    }

    private fun playingMachine(): PlaybackStateMachine = PlaybackStateMachine().apply {
        accept(PlaybackEvent.LoadStarted(first))
        accept(PlaybackEvent.PlayStarted(first))
    }
}
