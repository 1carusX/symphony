package io.github.zyrouge.symphony.ui.theme.bento

/**
 * Size tokens (tokens.json size). All values in dp.
 */
object BentoSizes {
    const val ICON_BUTTON = 48
    const val ICON = 24
    const val TOUCH_TARGET_MIN = 44

    /** Capsule (mini player). */
    const val CAPSULE_HEIGHT = 64
    const val CAPSULE_HEIGHT_SHORT = 56
    const val CAPSULE_MAX_WIDTH = 560
    const val CAPSULE_ART = 48
    const val CAPSULE_PLAY = 48
    const val CAPSULE_BOTTOM_INSET_PHONE = 20
    const val CAPSULE_BOTTOM_INSET_OTHER = 16
    const val CAPSULE_BOTTOM_INSET_SHORT = 10

    /** Play button sizes. */
    const val PLAY_BUTTON_SM = 44
    const val PLAY_BUTTON_MD = 56
    const val PLAY_BUTTON_LG = 76
    const val PLAY_BUTTON_XL = 92

    /** Previous/next transport buttons. */
    const val PREV_NEXT_BUTTON = 56
    const val PREV_NEXT_ICON = 32

    /** Navigation rail widths and items. */
    const val RAIL_SHORT = 80
    const val RAIL_WIDTH = 96
    const val RAIL_WIDE = 240
    const val RAIL_FAB = 56
    const val RAIL_ITEM = 76

    /** Page header heights. */
    const val HEADER_MIN_HEIGHT = 80
    const val HEADER_MIN_HEIGHT_PHONE = 66
    const val HEADER_MIN_HEIGHT_SHORT = 52

    const val TAB = 40
    const val SEARCH_FIELD = 52
    const val ACTION_TILE = 72
    const val SMART_FILTER = 64
    const val SMART_FILTER_SIDE_COLUMN = 56
    const val GENRE_CHIP = 36
    const val SONG_ROW = 60
    const val SONG_ROW_COLUMNS = 56
    const val LETTER_HEADER = 34
    const val GROUP_ROW = 44
    const val TILE_LIST_ROW = 48
    const val QUEUE_ROW = 56
    const val MENU_ITEM = 48

    /** Floating player window on wide screens. */
    const val FLOATING_PLAYER_WIDTH = 400
    const val FLOATING_PLAYER_INSET = 20
    const val FLOATING_PLAYER_MEDIUM_MAX_HEIGHT = 720
    const val FLOATING_PLAYER_LANDSCAPE_MEDIUM_WIDTH = 680
    const val FLOATING_PLAYER_LANDSCAPE_INSET = 8

    /**
     * Phone player sheet. Top inset: max(TOP_INSET_MIN, TOP_INSET_HEIGHT_RATIO
     * of window height). Small level height: min(SMALL_HEIGHT_MAX, window
     * height - top inset). Medium level height: content height plus
     * MEDIUM_CONTENT_PADDING (the cover is a full-width square).
     */
    const val PHONE_SHEET_TOP_INSET_MIN = 16
    const val PHONE_SHEET_TOP_INSET_HEIGHT_RATIO = 0.06f
    const val PHONE_SHEET_SMALL_HEIGHT_MAX = 300
    const val PHONE_SHEET_MEDIUM_CONTENT_PADDING = 20

    const val MENU_WIDTH = 288
    const val SIDE_COLUMN = 320

    /** A-Z scrubber. */
    const val AZ_SCRUBBER_WIDTH = 24
    const val AZ_SCRUBBER_MAX_HEIGHT = 520

    /** Seek bar. */
    const val SEEK_HEIGHT = 30
    const val SEEK_TRACK = 4
    const val SEEK_THUMB_MIN = 4
    const val SEEK_THUMB_MAX = 22
}
