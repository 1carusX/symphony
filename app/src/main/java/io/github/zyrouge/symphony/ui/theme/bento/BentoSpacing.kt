package io.github.zyrouge.symphony.ui.theme.bento

/**
 * Spacing tokens (tokens.json spacing). All values in dp.
 */
object BentoSpacing {
    /** Page side padding on compact windows (window width < 600). */
    const val GUTTER_COMPACT = 16

    /** Page side padding on medium and wider windows. */
    const val GUTTER_OTHER = 24

    /** Gap between tiles on compact windows. */
    const val GAP_COMPACT = 12

    /** Gap between tiles on medium and wider windows. */
    const val GAP_OTHER = 16

    const val TILE_PADDING = 16
    const val TILE_PADDING_SMALL = 12
    const val TILE_PADDING_EXTRA_SMALL = 8

    /**
     * Content inside list tiles starts 16 dp from the tile edge:
     * 8 of tile padding plus 8 of row padding.
     */
    const val LIST_TILE_CONTENT_INSET = 16

    const val PLAYER_CONTENT_INSET = 20
}
