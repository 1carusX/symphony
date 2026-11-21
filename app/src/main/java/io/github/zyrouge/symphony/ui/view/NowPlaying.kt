package io.github.zyrouge.symphony.ui.view

/**
 * Legacy stored setting types. The bento player has a fixed transport and
 * always replaces its stage with lyrics, so these values remain readable by
 * Settings but no longer affect the UI.
 */
enum class NowPlayingControlsLayout {
    CompactLeft,
    CompactRight,
    Traditional,
}

enum class NowPlayingLyricsLayout {
    ReplaceArtwork,
    SeparatePage,
}
