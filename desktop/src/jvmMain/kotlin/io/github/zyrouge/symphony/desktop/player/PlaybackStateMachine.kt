package io.github.zyrouge.symphony.desktop.player

import java.nio.file.Path

class PlaybackStateMachine {
    var state: PlaybackState = PlaybackState.Idle
        private set

    fun accept(event: PlaybackEvent): PlaybackState {
        state = reduce(event)
        return state
    }

    private fun reduce(event: PlaybackEvent): PlaybackState = when (event) {
        is PlaybackEvent.LoadStarted -> PlaybackState.Loading(event.path)
        is PlaybackEvent.Failure -> PlaybackState.Failed(event.path ?: activePath(), event.reason)
        is PlaybackEvent.Stopped -> PlaybackState.Idle
        is PlaybackEvent.PlayStarted ->
            if (event.path == activePath()) PlaybackState.Playing(event.path) else state
        is PlaybackEvent.PauseRequested ->
            if (event.path == activePath() && state is PlaybackState.Playing) {
                PlaybackState.Paused(event.path)
            } else {
                state
            }
        is PlaybackEvent.ReachedEnd ->
            if (event.path == activePath() && state is PlaybackState.Playing) {
                PlaybackState.Ended(event.path)
            } else {
                state
            }
    }

    private fun activePath(): Path? = when (val current = state) {
        PlaybackState.Idle -> null
        is PlaybackState.Loading -> current.path
        is PlaybackState.Playing -> current.path
        is PlaybackState.Paused -> current.path
        is PlaybackState.Ended -> current.path
        is PlaybackState.Failed -> current.path
    }
}
