package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.ui.contracts.UiLoopMode

/** The player's repeat/shuffle mode, as the transport button cycles it. */
enum class PlayerMode {
    REPEAT_OFF,
    REPEAT_ALL,
    REPEAT_ONE,
    SHUFFLE,
}

/** The play mode button cycles repeat all -> repeat one -> shuffle -> repeat all. */
fun nextPlayerMode(mode: PlayerMode): PlayerMode = when (mode) {
    PlayerMode.REPEAT_OFF -> PlayerMode.REPEAT_ALL
    PlayerMode.REPEAT_ALL -> PlayerMode.REPEAT_ONE
    PlayerMode.REPEAT_ONE -> PlayerMode.SHUFFLE
    PlayerMode.SHUFFLE -> PlayerMode.REPEAT_ALL
}

/** Resolves the mode the transport button shows from the playback state. */
fun resolvePlayerMode(loopMode: UiLoopMode, shuffle: Boolean): PlayerMode = when {
    shuffle -> PlayerMode.SHUFFLE
    loopMode == UiLoopMode.SONG -> PlayerMode.REPEAT_ONE
    loopMode == UiLoopMode.QUEUE -> PlayerMode.REPEAT_ALL
    else -> PlayerMode.REPEAT_OFF
}

/** Shuffle keeps repeating the queue, so playback does not stop after the last shuffled song. */
fun PlayerMode.isShuffle(): Boolean = this == PlayerMode.SHUFFLE

/** The queue loop mode the transport button maps this mode to. */
fun PlayerMode.toQueueLoopMode(): UiLoopMode = when (this) {
    PlayerMode.REPEAT_OFF -> UiLoopMode.NONE
    PlayerMode.REPEAT_ALL, PlayerMode.SHUFFLE -> UiLoopMode.QUEUE
    PlayerMode.REPEAT_ONE -> UiLoopMode.SONG
}
