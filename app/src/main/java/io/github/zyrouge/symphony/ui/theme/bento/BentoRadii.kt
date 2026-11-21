package io.github.zyrouge.symphony.ui.theme.bento

/**
 * Corner radius tokens (tokens.json radius). All values in dp. Nested corners
 * follow the recorded rule: inner radius = outer radius - padding.
 */
object BentoRadii {
    const val TILE = 24
    const val TILE_SMALL = 20
    const val TILE_HOVER = 30
    const val ROW = 16
    const val MENU = 24
    const val MENU_SHEET = 28
    const val FLOATING_PLAYER = 28
    const val PHONE_SHEET_TOP = 30
    const val PLAYER_COVER = 20
    const val CAPSULE = 32
    const val SEARCH_FIELD = 26
    const val CHIP = 18
    const val ICON_WELL_40 = 12
    const val ICON_WELL_48 = 14
    const val ICON_WELL_56 = 16
    const val SONG_ART_44 = 12
    const val GROUP_COVER_64 = 16
    const val QUEUE_ART_40 = 12
    const val TILE_LIST_ART_36 = 10
    const val GROUP_ROW_ART_30 = 8

    /**
     * Play button radius as a fraction of its size: rounded square while
     * paused, circle while playing.
     */
    const val PLAY_BUTTON_PAUSED_RATIO = 0.32f
    const val PLAY_BUTTON_PLAYING_RATIO = 0.5f
}
