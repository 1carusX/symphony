package io.github.zyrouge.symphony.desktop.player

import java.nio.file.Path

sealed interface PlaybackState {
    data object Idle : PlaybackState
    data class Loading(val path: Path) : PlaybackState
    data class Playing(val path: Path) : PlaybackState
    data class Paused(val path: Path) : PlaybackState
    data class Ended(val path: Path) : PlaybackState
    data class Failed(val path: Path?, val reason: PlaybackFailure) : PlaybackState
}

enum class PlaybackFailure {
    FileNotFound,
    Unreadable,
    UnsupportedFormat,
    BackendUnavailable,
    PlaybackError,
}

sealed interface PlaybackEvent {
    data class LoadStarted(val path: Path) : PlaybackEvent
    data class PlayStarted(val path: Path) : PlaybackEvent
    data class PauseRequested(val path: Path) : PlaybackEvent
    data class ReachedEnd(val path: Path) : PlaybackEvent
    data class Failure(val path: Path?, val reason: PlaybackFailure) : PlaybackEvent
    data object Stopped : PlaybackEvent
}
